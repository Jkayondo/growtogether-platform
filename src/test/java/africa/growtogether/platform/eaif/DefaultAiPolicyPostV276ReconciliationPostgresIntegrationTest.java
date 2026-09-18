package africa.growtogether.platform.eaif;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

class DefaultAiPolicyPostV276ReconciliationPostgresIntegrationTest {

    @Test
    void reconcilesTenantCreatedAfterV276WithoutChangingAuthorizationOrProviders()
            throws Exception {

        PostgreSQLContainer<?> postgres =
                new PostgreSQLContainer<>("postgres:16-alpine");

        postgres.start();

        try {
            migrateTo(postgres, "276");

            UUID tenantId = UUID.randomUUID();

            long rolesBefore;
            long permissionsBefore;
            long rolePermissionsBefore;
            long userRolesBefore;
            long invitationRolesBefore;
            long providersBefore;
            long modelsBefore;
            long policiesBefore;

            try (Connection connection = connect(postgres)) {
                cloneTenant(
                        connection,
                        tenantId,
                        "V277-POLICY-" + shortId(tenantId),
                        "V277 Policy Reconciliation Tenant"
                );

                assertEquals(
                        0L,
                        policyCount(connection, tenantId)
                );

                rolesBefore =
                        scalar(connection, "select count(*) from eiam_role");

                permissionsBefore =
                        scalar(connection, "select count(*) from eiam_permission");

                rolePermissionsBefore =
                        scalar(connection, "select count(*) from eiam_role_permission");

                userRolesBefore =
                        scalar(connection, "select count(*) from eiam_user_role");

                invitationRolesBefore =
                        scalar(connection, "select count(*) from eiam_invitation_role");

                providersBefore =
                        scalar(connection, "select count(*) from eaif_providers");

                modelsBefore =
                        scalar(connection, "select count(*) from eaif_models");

                policiesBefore =
                        scalar(
                                connection,
                                "select count(*) from ai_governance_policies"
                        );
            }

            migrateAll(postgres);

            try (Connection connection = connect(postgres)) {
                assertEquals(
                        1L,
                        policyCount(connection, tenantId)
                );

                assertEquals(
                        1L,
                        scalar(
                                connection,
                                "select count(*) "
                                        + "from ai_governance_policies "
                                        + "where tenant_id='"
                                        + tenantId
                                        + "'::uuid "
                                        + "and policy_code='DEFAULT_AI_POLICY' "
                                        + "and policy_name='Default AI Governance Policy' "
                                        + "and maximum_risk_level='HIGH' "
                                        + "and approval_required=true "
                                        + "and active=true "
                                        + "and status='ACTIVE' "
                                        + "and created_by='GT-MIGRATION-V277'"
                        )
                );

                assertEquals(
                        policiesBefore + 1L,
                        scalar(
                                connection,
                                "select count(*) from ai_governance_policies"
                        )
                );

                assertEquals(
                        rolesBefore,
                        scalar(connection, "select count(*) from eiam_role")
                );

                assertEquals(
                        permissionsBefore,
                        scalar(connection, "select count(*) from eiam_permission")
                );

                assertEquals(
                        rolePermissionsBefore,
                        scalar(
                                connection,
                                "select count(*) from eiam_role_permission"
                        )
                );

                assertEquals(
                        userRolesBefore,
                        scalar(connection, "select count(*) from eiam_user_role")
                );

                assertEquals(
                        invitationRolesBefore,
                        scalar(
                                connection,
                                "select count(*) from eiam_invitation_role"
                        )
                );

                assertEquals(
                        providersBefore,
                        scalar(connection, "select count(*) from eaif_providers")
                );

                assertEquals(
                        modelsBefore,
                        scalar(connection, "select count(*) from eaif_models")
                );

                assertEquals(
                        1L,
                        scalar(
                                connection,
                                "select count(*) "
                                        + "from flyway_schema_history "
                                        + "where version='277' "
                                        + "and success=true"
                        )
                );
            }
        } finally {
            postgres.stop();
        }
    }

    private static long policyCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalar(
                connection,
                "select count(*) "
                        + "from ai_governance_policies "
                        + "where tenant_id='"
                        + tenantId
                        + "'::uuid "
                        + "and policy_code='DEFAULT_AI_POLICY'"
        );
    }

    private static void cloneTenant(
            Connection connection,
            UUID tenantId,
            String code,
            String name
    ) throws Exception {

        String sql =
                "insert into eiam_tenant "
                        + "(id, organization_id, code, name, "
                        + "status, created_at, version) "
                        + "select ?, organization_id, ?, ?, "
                        + "status, current_timestamp, 0 "
                        + "from eiam_tenant "
                        + "order by created_at "
                        + "limit 1";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setObject(1, tenantId);
            statement.setString(2, code);
            statement.setString(3, name);

            assertEquals(1, statement.executeUpdate());
        }
    }

    private static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    private static void migrateTo(
            PostgreSQLContainer<?> postgres,
            String version
    ) {
        Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion(version))
                .load()
                .migrate();
    }

    private static void migrateAll(
            PostgreSQLContainer<?> postgres
    ) {
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

    private static Connection connect(
            PostgreSQLContainer<?> postgres
    ) throws Exception {

        return DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
    }

    private static long scalar(
            Connection connection,
            String sql
    ) throws Exception {

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }
}
