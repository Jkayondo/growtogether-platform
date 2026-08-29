package africa.growtogether.platform.eiam.permission;

import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class IntegrationAdminAuthorizationPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_eip_authorization_test"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    private static JdbcTemplate jdbc;

    @BeforeAll
    static void migrateDatabase() {

        Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .locations(
                        "classpath:db/migration"
                )
                .target(
                        MigrationVersion.fromVersion("160")
                )
                .load()
                .migrate();

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setUrl(
                postgres.getJdbcUrl()
        );

        dataSource.setUsername(
                postgres.getUsername()
        );

        dataSource.setPassword(
                postgres.getPassword()
        );

        jdbc =
                new JdbcTemplate(
                        dataSource
                );

        UUID tenantId =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_tenant
                        WHERE code = 'GT-SCHOOL'
                        """,
                        UUID.class
                );

        UUID schoolAdminRoleId =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'SCHOOL_ADMIN'
                        """,
                        UUID.class,
                        tenantId
                );

        UUID userId =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO eiam_user_account (
                    id,
                    tenant_id,
                    username,
                    email,
                    display_name,
                    password_hash,
                    account_status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status,
                    failed_login_attempts
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE',
                    0
                )
                """,
                userId,
                tenantId,
                "v161-integration-admin",
                "v161-integration-admin@growtogether.africa",
                "V161 Integration Administrator",
                "test-password-hash"
        );

        jdbc.update(
                """
                INSERT INTO eiam_user_role (
                    id,
                    tenant_id,
                    user_id,
                    role_id,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?, ?, ?,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                userId,
                schoolAdminRoleId
        );

        Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .locations(
                        "classpath:db/migration"
                )
                .load()
                .migrate();
    }

    @Test
    void definesAllEipAuthorities() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission p
                        JOIN eiam_tenant t
                          ON t.id = p.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND p.code LIKE 'integration.%'
                          AND p.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(25);
    }

    @Test
    void createsDedicatedIntegrationAdministratorRole() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role r
                        JOIN eiam_tenant t
                          ON t.id = r.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'INTEGRATION_ADMIN'
                          AND r.system_role = TRUE
                          AND r.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(1);
    }

    @Test
    void integrationAdministratorReceivesOnlyInfrastructureAllowList() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role_permission rp
                        JOIN eiam_role r
                          ON r.id = rp.role_id
                         AND r.tenant_id = rp.tenant_id
                        JOIN eiam_permission p
                          ON p.id = rp.permission_id
                         AND p.tenant_id = rp.tenant_id
                        JOIN eiam_tenant t
                          ON t.id = rp.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'INTEGRATION_ADMIN'
                          AND p.code LIKE 'integration.%'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(17);
    }

    @Test
    void financialExecutionAuthoritiesRemainUnassigned() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role_permission rp
                        JOIN eiam_role r
                          ON r.id = rp.role_id
                         AND r.tenant_id = rp.tenant_id
                        JOIN eiam_permission p
                          ON p.id = rp.permission_id
                         AND p.tenant_id = rp.tenant_id
                        JOIN eiam_tenant t
                          ON t.id = rp.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'INTEGRATION_ADMIN'
                          AND p.code IN (
                              'integration.payment.create',
                              'integration.payment.execute',
                              'integration.payment.manage',
                              'integration.payment.read',
                              'integration.payment.reverse',
                              'integration.settlement.manage',
                              'integration.reconciliation.manage',
                              'integration.dispute.manage'
                          )
                        """,
                        Integer.class
                );

        assertThat(count)
                .isZero();
    }

    @Test
    void schoolAdministratorDoesNotGainIntegrationAuthorities() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role_permission rp
                        JOIN eiam_role r
                          ON r.id = rp.role_id
                         AND r.tenant_id = rp.tenant_id
                        JOIN eiam_permission p
                          ON p.id = rp.permission_id
                         AND p.tenant_id = rp.tenant_id
                        JOIN eiam_tenant t
                          ON t.id = rp.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'SCHOOL_ADMIN'
                          AND p.code LIKE 'integration.%'
                        """,
                        Integer.class
                );

        assertThat(count)
                .isZero();
    }

    @Test
    void soleActiveSchoolAdministratorReceivesIntegrationAdministratorRole() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_account u
                        JOIN eiam_tenant t
                          ON t.id = u.tenant_id
                        JOIN eiam_user_role ur
                          ON ur.tenant_id = u.tenant_id
                         AND ur.user_id = u.id
                         AND ur.status = 'ACTIVE'
                        JOIN eiam_role r
                          ON r.tenant_id = ur.tenant_id
                         AND r.id = ur.role_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND u.username = 'v161-integration-admin'
                          AND r.code = 'INTEGRATION_ADMIN'
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(1);
    }

    @Test
    void migration161IsRecordedSuccessfully() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '161'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(1);
    }
}
