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
class EcsTenantAdminAuthorizationPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_ecs_auth_test"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    private static JdbcTemplate jdbc;

    private static UUID tenantId;
    private static UUID candidateUserId;

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
                        MigrationVersion.fromVersion(
                                "267"
                        )
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

        tenantId =
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
                          AND status = 'ACTIVE'
                        """,
                        UUID.class,
                        tenantId
                );

        UUID integrationAdminRoleId =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'INTEGRATION_ADMIN'
                          AND status = 'ACTIVE'
                        """,
                        UUID.class,
                        tenantId
                );

        candidateUserId =
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
                candidateUserId,
                tenantId,
                "historical-platform-admin",
                "historical-platform-admin@growtogether.africa",
                "Historical Platform Administrator",
                "test-password-hash"
        );

        assignRole(
                candidateUserId,
                schoolAdminRoleId
        );

        assignRole(
                candidateUserId,
                integrationAdminRoleId
        );

        Integer existingTenantAdmin =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'TENANT_ADMIN'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(existingTenantAdmin)
                .isZero();

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

    private static void assignRole(
            UUID userId,
            UUID roleId
    ) {
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
                roleId
        );
    }

    @Test
    void definesAllEnterpriseConfigurationAuthorities() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND code IN (
                            'platform.configuration.definition.manage',
                            'platform.configuration.manage',
                            'platform.configuration.read',
                            'platform.configuration.history.read',
                            'platform.configuration.rollback',
                            'platform.configuration.secret.read'
                          )
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(count)
                .isEqualTo(6);
    }

    @Test
    void createsGovernedTenantAdministratorRole() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'TENANT_ADMIN'
                          AND system_role = TRUE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(count)
                .isEqualTo(1);
    }

    @Test
    void tenantAdministratorReceivesOnlyGovernedConfigurationAuthorities() {

        Integer normalAuthorities =
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
                        WHERE rp.tenant_id = ?
                          AND r.code = 'TENANT_ADMIN'
                          AND p.code IN (
                            'platform.configuration.manage',
                            'platform.configuration.read',
                            'platform.configuration.history.read',
                            'platform.configuration.rollback'
                          )
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(normalAuthorities)
                .isEqualTo(4);

        Integer sensitiveAuthorities =
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
                        WHERE rp.tenant_id = ?
                          AND r.code = 'TENANT_ADMIN'
                          AND p.code IN (
                            'platform.configuration.definition.manage',
                            'platform.configuration.secret.read'
                          )
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(sensitiveAuthorities)
                .isZero();
    }

    @Test
    void recoversOnlyUnambiguousHistoricalAdministrator() {

        Integer assignment =
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
                        candidateUserId
                );

        assertThat(assignment)
                .isEqualTo(1);
    }
}
