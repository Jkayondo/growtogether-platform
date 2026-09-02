package africa.growtogether.platform.eiam.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.tenant.ProvisionTenantCommand;
import africa.growtogether.platform.eiam.tenant.TenantProvisioningService;
import africa.growtogether.platform.eiam.tenant.TenantView;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class AuthenticationFailurePersistencePostgresIntegrationTest {

    private static final String PASSWORD =
            "Strong-Test-Password-2026!";

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_auth_failure_test"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

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
    private TenantProvisioningService tenants;

    @Autowired
    private AuthenticationService authentication;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void failedLoginAttemptSurvivesAuthenticationRollback() {
        TenantView tenant =
                provision(
                        "GT-AUTH-FAIL-ONE",
                        "auth-failure-one"
                );

        useTenant(
                tenant.tenantId(),
                "auth-failure-one"
        );

        assertThatThrownBy(
                () -> authentication.login(
                        new LoginCommand(
                                "auth-failure-one",
                                "Wrong-Password-2026!"
                        )
                )
        )
                .isInstanceOf(
                        AuthenticationException.class
                )
                .hasMessage(
                        "Invalid username or password."
                );

        Integer failedAttempts =
                jdbc.queryForObject(
                        """
                        SELECT failed_login_attempts
                        FROM eiam_user_account
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        tenant.administratorUserId()
                );

        assertThat(failedAttempts)
                .isEqualTo(1);
    }

    @Test
    void configuredFailureThresholdPersistsAccountLockout() {
        TenantView tenant =
                provision(
                        "GT-AUTH-FAIL-LOCK",
                        "auth-failure-lock"
                );

        useTenant(
                tenant.tenantId(),
                "auth-failure-lock"
        );

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThatThrownBy(
                    () -> authentication.login(
                            new LoginCommand(
                                    "auth-failure-lock",
                                    "Wrong-Password-2026!"
                            )
                    )
            )
                    .isInstanceOf(
                            AuthenticationException.class
                    );
        }

        SecurityState state =
                jdbc.queryForObject(
                        """
                        SELECT
                            failed_login_attempts,
                            locked_until
                        FROM eiam_user_account
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        (rs, rowNum) ->
                                new SecurityState(
                                        rs.getInt(
                                                "failed_login_attempts"
                                        ),
                                        toInstant(
                                                rs.getTimestamp(
                                                        "locked_until"
                                                )
                                        )
                                ),
                        tenant.tenantId(),
                        tenant.administratorUserId()
                );

        assertThat(state)
                .isNotNull();

        assertThat(state.failedAttempts())
                .isZero();

        assertThat(state.lockedUntil())
                .isNotNull()
                .isAfter(
                        Instant.now()
                );

        assertThatThrownBy(
                () -> authentication.login(
                        new LoginCommand(
                                "auth-failure-lock",
                                PASSWORD
                        )
                )
        )
                .isInstanceOf(
                        AuthenticationException.class
                )
                .hasMessage(
                        "Account is not available for authentication."
                );
    }

    private TenantView provision(
            String code,
            String username
    ) {
        return tenants.provision(
                new ProvisionTenantCommand(
                        code + "-ORG",
                        code + " Organisation",
                        code,
                        code + " Tenant",
                        username + "@growtogether.africa",
                        username,
                        PASSWORD,
                        "Authentication Security Administrator"
                )
        );
    }

    private void useTenant(
            UUID tenantId,
            String correlationId
    ) {
        RequestContextHolder.set(
                new RequestContext(
                        correlationId,
                        tenantId.toString()
                )
        );
    }

    private static Instant toInstant(
            Timestamp timestamp
    ) {
        return timestamp == null
                ? null
                : timestamp.toInstant();
    }

    private record SecurityState(
            int failedAttempts,
            Instant lockedUntil
    ) {}
}
