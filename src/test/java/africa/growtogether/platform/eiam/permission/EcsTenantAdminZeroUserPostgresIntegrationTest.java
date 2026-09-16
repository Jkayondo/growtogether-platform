package africa.growtogether.platform.eiam.permission;

import static org.assertj.core.api.Assertions.assertThat;

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

@Testcontainers(disabledWithoutDocker = true)
class EcsTenantAdminZeroUserPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("growtogether_ecs_zero_user_test")
                    .withUsername("growtogether")
                    .withPassword("growtogether");

    private static JdbcTemplate jdbc;
    private static UUID tenantId;

    @BeforeAll
    static void migrateDatabase() {

        Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion("267"))
                .load()
                .migrate();

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());

        jdbc = new JdbcTemplate(dataSource);

        tenantId =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_tenant
                        WHERE code = 'GT-SCHOOL'
                        """,
                        UUID.class
                );

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

        Integer tenantAdminBefore =
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

        assertThat(usersBefore).isZero();
        assertThat(tenantAdminBefore).isZero();

        Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @Test
    void zeroUserTenantReceivesTenantAdminFoundationWithoutManufacturingIdentity() {

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

        Integer tenantAdminRoles =
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

        Integer tenantAdminAssignments =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_role ur
                        JOIN eiam_role r
                          ON r.id = ur.role_id
                         AND r.tenant_id = ur.tenant_id
                        WHERE ur.tenant_id = ?
                          AND r.code = 'TENANT_ADMIN'
                          AND ur.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId
                );

        Integer coreAuthorityAssignments =
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
                            'eiam.users.create',
                            'eiam.users.read',
                            'eiam.users.update',
                            'eiam.users.activate',
                            'eiam.users.suspend',
                            'eiam.users.deactivate',
                            'eiam.roles.create',
                            'eiam.roles.read',
                            'eiam.roles.update',
                            'eiam.roles.delete',
                            'eiam.user-roles.assign',
                            'eiam.user-roles.read',
                            'eiam.permissions.create',
                            'eiam.permissions.read',
                            'eiam.permissions.update',
                            'eiam.permissions.delete',
                            'eiam.role-permissions.assign',
                            'eiam.role-permissions.read',
                            'platform.tenants.read',
                            'platform.tenants.manage'
                          )
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(usersAfter).isZero();
        assertThat(tenantAdminRoles).isEqualTo(1);
        assertThat(tenantAdminAssignments).isZero();
        assertThat(coreAuthorityAssignments).isEqualTo(20);
    }
}
