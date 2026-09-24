package africa.growtogether.platform.school.leadership;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;

import org.junit.jupiter.api.Test;

import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeadershipOverviewPermissionExistingTenantPostgresIntegrationTest {

    private static final String PERMISSION_CODE =
            "school.leadership.overview.read";

    private static final String PERMISSION_NAME =
            "Read School Leadership Overview";

    private static final String PERMISSION_DESCRIPTION =
            "Allows an authorised school leadership user to read the tenant-scoped GT School Leadership overview and its permitted aggregate indicators.";

    private static final String PERMISSION_MODULE =
            "SCHOOL_LEADERSHIP";

    @Test
    void v282BackfillsExistingTenantsWithoutRoleGrants()
            throws Exception {

        try (PostgreSQLContainer<?> postgres =
                     new PostgreSQLContainer<>(
                             "postgres:17-alpine"
                     )) {

            postgres.start();

            Flyway through281 =
                    flyway(
                            postgres,
                            "281"
                    );

            var baseline =
                    through281.migrate();

            assertTrue(
                    baseline.migrationsExecuted > 0
            );

            UUID tenantA =
                    UUID.randomUUID();

            UUID tenantB =
                    UUID.randomUUID();

            try (Connection connection =
                         postgres.createConnection("")) {

                assertEquals(
                        1L,
                        scalar(
                                connection,
                                """
                                SELECT COUNT(*)
                                FROM flyway_schema_history
                                WHERE version = '281'
                                  AND success = TRUE
                                """
                        )
                );

                assertTrue(
                        scalar(
                                connection,
                                """
                                SELECT COUNT(*)
                                FROM eiam_tenant
                                """
                        ) > 0L,
                        "V281 baseline must contain at least one tenant."
                );

                insertTenantClone(
                        connection,
                        tenantA,
                        "LD37-EXISTING-A",
                        "LD37 Existing Tenant A"
                );

                insertTenantClone(
                        connection,
                        tenantB,
                        "LD37-EXISTING-B",
                        "LD37 Existing Tenant B"
                );

                insertTestRole(
                        connection,
                        tenantA,
                        "LD37_TEST_ROLE_A"
                );

                insertTestRole(
                        connection,
                        tenantB,
                        "LD37_TEST_ROLE_B"
                );

                assertEquals(
                        0L,
                        permissionCount(
                                connection,
                                tenantA
                        )
                );

                assertEquals(
                        0L,
                        permissionCount(
                                connection,
                                tenantB
                        )
                );

                assertEquals(
                        0L,
                        roleGrantCount(
                                connection,
                                tenantA
                        )
                );

                assertEquals(
                        0L,
                        roleGrantCount(
                                connection,
                                tenantB
                        )
                );
            }

            Flyway through282 =
                    flyway(
                            postgres,
                            "282"
                    );

            var upgrade =
                    through282.migrate();

            assertEquals(
                    1,
                    upgrade.migrationsExecuted,
                    "Exactly V282 must execute after V281."
            );

            String permissionIdA;
            String permissionIdB;

            try (Connection connection =
                         postgres.createConnection("")) {

                assertEquals(
                        1L,
                        scalar(
                                connection,
                                """
                                SELECT COUNT(*)
                                FROM flyway_schema_history
                                WHERE version = '282'
                                  AND success = TRUE
                                """
                        )
                );

                assertEquals(
                        1L,
                        permissionCount(
                                connection,
                                tenantA
                        )
                );

                assertEquals(
                        1L,
                        permissionCount(
                                connection,
                                tenantB
                        )
                );

                permissionIdA =
                        assertPermissionContract(
                                connection,
                                tenantA
                        );

                permissionIdB =
                        assertPermissionContract(
                                connection,
                                tenantB
                        );

                assertNotEquals(
                        permissionIdA,
                        permissionIdB,
                        "Each tenant must receive a distinct permission id."
                );

                assertEquals(
                        0L,
                        roleGrantCount(
                                connection,
                                tenantA
                        ),
                        "V282 must create no role grant."
                );

                assertEquals(
                        0L,
                        roleGrantCount(
                                connection,
                                tenantB
                        ),
                        "V282 must create no role grant."
                );

                assertEquals(
                        2L,
                        scalarPrepared(
                                connection,
                                """
                                SELECT COUNT(DISTINCT id)
                                FROM eiam_permission
                                WHERE tenant_id IN (?, ?)
                                  AND code = ?
                                  AND status = 'ACTIVE'
                                """,
                                tenantA,
                                tenantB,
                                PERMISSION_CODE
                        )
                );
            }

            var secondMigrate =
                    through282.migrate();

            assertEquals(
                    0,
                    secondMigrate.migrationsExecuted,
                    "Second V282-targeted migration must be a no-op."
            );

            try (Connection connection =
                         postgres.createConnection("")) {

                assertEquals(
                        1L,
                        permissionCount(
                                connection,
                                tenantA
                        )
                );

                assertEquals(
                        1L,
                        permissionCount(
                                connection,
                                tenantB
                        )
                );

                assertEquals(
                        1L,
                        scalar(
                                connection,
                                """
                                SELECT COUNT(*)
                                FROM flyway_schema_history
                                WHERE version = '282'
                                  AND success = TRUE
                                """
                        )
                );
            }
        }
    }

    private static Flyway flyway(
            PostgreSQLContainer<?> postgres,
            String target
    ) {

        return Flyway.configure()
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
                                target
                        )
                )
                .load();
    }

    private static void insertTenantClone(
            Connection connection,
            UUID tenantId,
            String code,
            String name
    ) throws Exception {

        String sql =
                """
                INSERT INTO eiam_tenant (
                    id,
                    organization_id,
                    code,
                    name,
                    status,
                    created_at,
                    version
                )
                SELECT
                    ?,
                    organization_id,
                    ?,
                    ?,
                    status,
                    CURRENT_TIMESTAMP,
                    0
                FROM eiam_tenant
                ORDER BY created_at
                LIMIT 1
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setObject(
                    1,
                    tenantId
            );

            statement.setString(
                    2,
                    code
            );

            statement.setString(
                    3,
                    name
            );

            assertEquals(
                    1,
                    statement.executeUpdate()
            );
        }
    }

    private static void insertTestRole(
            Connection connection,
            UUID tenantId,
            String code
    ) throws Exception {

        String sql =
                """
                INSERT INTO eiam_role (
                    id,
                    tenant_id,
                    code,
                    name,
                    description,
                    system_role,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    FALSE,
                    CURRENT_TIMESTAMP,
                    'LD37-V282-TEST',
                    CURRENT_TIMESTAMP,
                    'LD37-V282-TEST',
                    0,
                    'ACTIVE'
                )
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setObject(
                    1,
                    UUID.randomUUID()
            );

            statement.setObject(
                    2,
                    tenantId
            );

            statement.setString(
                    3,
                    code
            );

            statement.setString(
                    4,
                    code
            );

            statement.setString(
                    5,
                    "LD37 existing-tenant migration proof role."
            );

            assertEquals(
                    1,
                    statement.executeUpdate()
            );
        }
    }

    private static long permissionCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalarPrepared(
                connection,
                """
                SELECT COUNT(*)
                FROM eiam_permission
                WHERE tenant_id = ?
                  AND code = ?
                """,
                tenantId,
                PERMISSION_CODE
        );
    }

    private static long roleGrantCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalarPrepared(
                connection,
                """
                SELECT COUNT(*)
                FROM eiam_role_permission rp
                JOIN eiam_permission p
                  ON p.tenant_id = rp.tenant_id
                 AND p.id = rp.permission_id
                WHERE rp.tenant_id = ?
                  AND p.code = ?
                """,
                tenantId,
                PERMISSION_CODE
        );
    }

    private static String assertPermissionContract(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        String sql =
                """
                SELECT
                    id::text,
                    code,
                    name,
                    description,
                    module,
                    system_permission,
                    status
                FROM eiam_permission
                WHERE tenant_id = ?
                  AND code = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setObject(
                    1,
                    tenantId
            );

            statement.setString(
                    2,
                    PERMISSION_CODE
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                assertTrue(
                        rs.next(),
                        "Leadership permission row must exist."
                );

                String id =
                        rs.getString(
                                1
                        );

                assertEquals(
                        PERMISSION_CODE,
                        rs.getString(2)
                );

                assertEquals(
                        PERMISSION_NAME,
                        rs.getString(3)
                );

                assertEquals(
                        PERMISSION_DESCRIPTION,
                        rs.getString(4)
                );

                assertEquals(
                        PERMISSION_MODULE,
                        rs.getString(5)
                );

                assertFalse(
                        rs.getBoolean(6),
                        "Leadership overview read must not be a system permission."
                );

                assertEquals(
                        "ACTIVE",
                        rs.getString(7)
                );

                assertFalse(
                        rs.next(),
                        "Exactly one Leadership permission row is allowed."
                );

                return id;
            }
        }
    }

    private static long scalar(
            Connection connection,
            String sql
    ) throws Exception {

        try (Statement statement =
                     connection.createStatement();
             ResultSet rs =
                     statement.executeQuery(sql)) {

            assertTrue(
                    rs.next()
            );

            return rs.getLong(
                    1
            );
        }
    }

    private static long scalarPrepared(
            Connection connection,
            String sql,
            Object... parameters
    ) throws Exception {

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (
                    int i = 0;
                    i < parameters.length;
                    i++
            ) {
                statement.setObject(
                        i + 1,
                        parameters[i]
                );
            }

            try (ResultSet rs =
                         statement.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                return rs.getLong(
                        1
                );
            }
        }
    }
}
