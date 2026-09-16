package africa.growtogether.platform.eiam.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class FirstAdminBootstrapPostgresIntegrationTest {

    private static final String RAW_TOKEN =
            "controlled-first-admin-bootstrap-2026";

    private static final String ADMIN_PASSWORD =
            "Strong-Pilot-Administrator-Password-2026";

    private static final String TOKEN_HASH =
            "{bcrypt}$2a$10$LvZpZHQDyNpAuI2prZDLy.koHFcn3qHAtQJzZ0sVlgdG5vG7t5I8m";

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName(
                            "growtogether_first_admin_bootstrap_test"
                    )
                    .withUsername("growtogether")
                    .withPassword("growtogether");

    @DynamicPropertySource
    static void properties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.flyway.enabled",
                () -> "true"
        );

        registry.add(
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordService passwords;

    @Autowired
    private africa.growtogether.platform.eiam.tenant.TenantRepository tenants;

    @Autowired
    private africa.growtogether.platform.eiam.user.UserAccountRepository users;

    @Autowired
    private africa.growtogether.platform.eiam.role.RoleRepository roles;

    @Autowired
    private africa.growtogether.platform.eiam.role.UserRoleRepository userRoles;

    @Autowired
    private africa.growtogether.platform.eiam.audit.AuditEventService audit;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Autowired
    private africa.growtogether.platform.eiam.auth.AuthenticationService authentication;

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void createsExactlyOneAuditedTenantAdministratorAndRejectsReuse() {

        UUID tenantId =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_tenant
                        WHERE code = 'GT-SCHOOL'
                        """,
                        UUID.class
                );

        assertThat(tenantId)
                .isNotNull();

        Integer usersBefore =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_account
                        WHERE tenant_id = ?
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(usersBefore)
                .isZero();

        Integer tenantAdminRoles =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'TENANT_ADMIN'
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(tenantAdminRoles)
                .isEqualTo(1);

        useTenant(tenantId);

        String encodedToken =
                passwords.hash(RAW_TOKEN);

        FirstAdminBootstrapService service =
                new FirstAdminBootstrapService(
                        new FirstAdminBootstrapProperties(
                                true,
                                tenantId.toString(),
                                encodedToken
                        ),
                        tenants,
                        users,
                        roles,
                        userRoles,
                        passwords,
                        audit
                );

        org.springframework.transaction.support.TransactionTemplate transactions =
                new org.springframework.transaction.support.TransactionTemplate(
                        transactionManager
                );

        FirstAdminBootstrapView result =
                transactions.execute(status ->
                        service.bootstrap(
                                RAW_TOKEN,
                                command()
                        )
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.tenantId())
                .isEqualTo(tenantId);

        Integer usersAfter =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_account
                        WHERE tenant_id = ?
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(usersAfter)
                .isEqualTo(1);

        String accountStatus =
                jdbc.queryForObject(
                        """
                        SELECT account_status
                        FROM eiam_user_account
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        String.class,
                        tenantId,
                        result.administratorUserId()
                );

        assertThat(accountStatus)
                .isEqualTo("ACTIVE");

        Integer roleAssignment =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_role ur
                        JOIN eiam_role r
                          ON r.id = ur.role_id
                         AND r.tenant_id = ur.tenant_id
                        WHERE ur.tenant_id = ?
                          AND ur.user_id = ?
                          AND r.code = 'TENANT_ADMIN'
                          AND ur.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId,
                        result.administratorUserId()
                );

        assertThat(roleAssignment)
                .isEqualTo(1);

        Integer auditCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_audit_events
                        WHERE tenant_id = ?
                          AND event_type = 'FIRST_ADMIN_BOOTSTRAP'
                          AND outcome = 'SUCCESS'
                          AND severity = 'HIGH'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(auditCount)
                .isEqualTo(1);

        africa.growtogether.platform.eiam.auth.LoginResponse login =
                authentication.login(
                        new africa.growtogether.platform.eiam.auth.LoginCommand(
                                "pilot-admin",
                                ADMIN_PASSWORD
                        )
                );

        assertThat(login)
                .isNotNull();

        assertThat(login.mfaRequired())
                .isFalse();

        assertThat(login.tokens())
                .isNotNull();

        assertThat(login.tokens().accessToken())
                .isNotBlank();

        assertThat(login.tokens().refreshToken())
                .isNotBlank();

        assertThat(login.tokens().userId())
                .isEqualTo(
                        result.administratorUserId()
                );

        assertThat(login.tokens().tenantId())
                .isEqualTo(
                        tenantId
                );

        assertThat(login.tokens().username())
                .isEqualTo(
                        "pilot-admin"
                );

        assertThat(login.tokens().roles())
                .containsExactly(
                        "TENANT_ADMIN"
                );

        assertThat(login.tokens().sessionId())
                .isNotNull();

        Integer activeSessions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_session
                        WHERE tenant_id = ?
                          AND user_id = ?
                          AND revoked_at IS NULL
                        """,
                        Integer.class,
                        tenantId,
                        result.administratorUserId()
                );

        assertThat(activeSessions)
                .isEqualTo(1);

        assertThatThrownBy(
                () -> transactions.execute(status ->
                        service.bootstrap(
                                RAW_TOKEN,
                                command()
                        )
                )
        )
                .isInstanceOf(
                        AccessDeniedException.class
                )
                .hasMessage(
                        "First administrator bootstrap is unavailable."
                );

        Integer usersAfterSecondAttempt =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_account
                        WHERE tenant_id = ?
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(usersAfterSecondAttempt)
                .isEqualTo(1);
    }

    private static FirstAdminBootstrapCommand command() {
        return new FirstAdminBootstrapCommand(
                "pilot-admin@growtogether.africa",
                "pilot-admin",
                ADMIN_PASSWORD,
                "Pilot Tenant Administrator"
        );
    }

    private static void useTenant(UUID tenantId) {
        RequestContextHolder.set(
                new RequestContext(
                        "first-admin-postgres-test",
                        tenantId.toString()
                )
        );
    }
}
