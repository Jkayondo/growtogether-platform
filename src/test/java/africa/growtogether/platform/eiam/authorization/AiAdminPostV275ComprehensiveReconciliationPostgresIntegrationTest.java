package africa.growtogether.platform.eiam.authorization;

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

class AiAdminPostV275ComprehensiveReconciliationPostgresIntegrationTest {

    private static final String[] AI_ADMIN_PERMISSIONS = {
        "ai.provider.manage",
        "ai.model.manage",
        "ai.prompt.manage",
        "ai.governance.read",
        "ai.audit.read",
        "ai.evidence.read",
        "ai.request.approval",
        "ai.request.read"
    };

    @Test
    void reconcilesBothPreV275PartialAndPostV275EmptyTenants()
            throws Exception {

        PostgreSQLContainer<?> postgres =
                new PostgreSQLContainer<>("postgres:16-alpine");

        postgres.start();

        try {
            migrateTo(postgres, "274");

            UUID partialTenant = UUID.randomUUID();

            try (Connection connection = connect(postgres)) {
                cloneTenant(
                        connection,
                        partialTenant,
                        "V276-PARTIAL-" + shortId(partialTenant),
                        "V276 Partial Tenant"
                );
            }

            migrateTo(postgres, "275");

            try (Connection connection = connect(postgres)) {
                assertEquals(1L, aiAdminRoleCount(connection, partialTenant));
                assertEquals(7L, intendedDefinitionCount(connection, partialTenant));
                assertEquals(7L, intendedGrantCount(connection, partialTenant));
                assertEquals(0L, requestReadDefinitionCount(connection, partialTenant));
                assertEquals(0L, requestReadGrantCount(connection, partialTenant));
            }

            UUID emptyTenant = UUID.randomUUID();

            try (Connection connection = connect(postgres)) {
                cloneTenant(
                        connection,
                        emptyTenant,
                        "V276-EMPTY-" + shortId(emptyTenant),
                        "V276 Empty Tenant"
                );

                assertEquals(0L, aiAdminRoleCount(connection, emptyTenant));
                assertEquals(0L, intendedDefinitionCount(connection, emptyTenant));
                assertEquals(0L, intendedGrantCount(connection, emptyTenant));
            }

            migrateAll(postgres);

            try (Connection connection = connect(postgres)) {
                assertCompleteAiAdmin(connection, partialTenant);
                assertCompleteAiAdmin(connection, emptyTenant);

                assertEquals(
                        1L,
                        scalar(
                                connection,
                                "select count(*) "
                                        + "from eiam_permission "
                                        + "where tenant_id='"
                                        + partialTenant
                                        + "'::uuid "
                                        + "and code='ai.request.read' "
                                        + "and created_by='GT-MIGRATION-V276'"
                        )
                );

                assertEquals(
                        8L,
                        scalar(
                                connection,
                                "select count(*) "
                                        + "from eiam_permission "
                                        + "where tenant_id='"
                                        + emptyTenant
                                        + "'::uuid "
                                        + "and created_by='GT-MIGRATION-V276' "
                                        + "and code in ("
                                        + quotedPermissionList()
                                        + ")"
                        )
                );

                assertEquals(
                        1L,
                        scalar(
                                connection,
                                "select count(*) "
                                        + "from eiam_role "
                                        + "where tenant_id='"
                                        + emptyTenant
                                        + "'::uuid "
                                        + "and code='AI_ADMIN' "
                                        + "and created_by='GT-MIGRATION-V276'"
                        )
                );

                assertEquals(
                        0L,
                        forbiddenGrantCount(connection, partialTenant)
                );

                assertEquals(
                        0L,
                        forbiddenGrantCount(connection, emptyTenant)
                );

                assertEquals(
                        0L,
                        aiAdminUserAssignmentCount(
                                connection,
                                partialTenant
                        )
                );

                assertEquals(
                        0L,
                        aiAdminUserAssignmentCount(
                                connection,
                                emptyTenant
                        )
                );

                assertEquals(
                        0L,
                        aiAdminInvitationAssignmentCount(
                                connection,
                                partialTenant
                        )
                );

                assertEquals(
                        0L,
                        aiAdminInvitationAssignmentCount(
                                connection,
                                emptyTenant
                        )
                );

                assertEquals(
                        1L,
                        scalar(
                                connection,
                                "select count(*) "
                                        + "from flyway_schema_history "
                                        + "where version='276' "
                                        + "and success=true"
                        )
                );
            }
        } finally {
            postgres.stop();
        }
    }

    private static void assertCompleteAiAdmin(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        assertEquals(1L, aiAdminRoleCount(connection, tenantId));
        assertEquals(8L, intendedDefinitionCount(connection, tenantId));
        assertEquals(8L, intendedGrantCount(connection, tenantId));
        assertEquals(1L, requestReadDefinitionCount(connection, tenantId));
        assertEquals(1L, requestReadGrantCount(connection, tenantId));
    }

    private static long aiAdminRoleCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalar(
                connection,
                "select count(*) "
                        + "from eiam_role "
                        + "where tenant_id='"
                        + tenantId
                        + "'::uuid "
                        + "and code='AI_ADMIN' "
                        + "and status='ACTIVE'"
        );
    }

    private static long intendedDefinitionCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalar(
                connection,
                "select count(distinct code) "
                        + "from eiam_permission "
                        + "where tenant_id='"
                        + tenantId
                        + "'::uuid "
                        + "and status='ACTIVE' "
                        + "and code in ("
                        + quotedPermissionList()
                        + ")"
        );
    }

    private static long intendedGrantCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalar(
                connection,
                "select count(distinct p.code) "
                        + "from eiam_role r "
                        + "join eiam_role_permission rp "
                        + "on rp.role_id=r.id "
                        + "and rp.tenant_id=r.tenant_id "
                        + "and rp.status='ACTIVE' "
                        + "join eiam_permission p "
                        + "on p.id=rp.permission_id "
                        + "and p.tenant_id=rp.tenant_id "
                        + "and p.status='ACTIVE' "
                        + "where r.tenant_id='"
                        + tenantId
                        + "'::uuid "
                        + "and r.code='AI_ADMIN' "
                        + "and r.status='ACTIVE' "
                        + "and p.code in ("
                        + quotedPermissionList()
                        + ")"
        );
    }

    private static long requestReadDefinitionCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalar(
                connection,
                "select count(*) "
                        + "from eiam_permission "
                        + "where tenant_id='"
                        + tenantId
                        + "'::uuid "
                        + "and code='ai.request.read' "
                        + "and status='ACTIVE'"
        );
    }

    private static long requestReadGrantCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalar(
                connection,
                "select count(*) "
                        + "from eiam_role r "
                        + "join eiam_role_permission rp "
                        + "on rp.role_id=r.id "
                        + "and rp.tenant_id=r.tenant_id "
                        + "and rp.status='ACTIVE' "
                        + "join eiam_permission p "
                        + "on p.id=rp.permission_id "
                        + "and p.tenant_id=rp.tenant_id "
                        + "and p.status='ACTIVE' "
                        + "where r.tenant_id='"
                        + tenantId
                        + "'::uuid "
                        + "and r.code='AI_ADMIN' "
                        + "and p.code='ai.request.read'"
        );
    }

    private static long forbiddenGrantCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalar(
                connection,
                "select count(*) "
                        + "from eiam_role r "
                        + "join eiam_role_permission rp "
                        + "on rp.role_id=r.id "
                        + "and rp.tenant_id=r.tenant_id "
                        + "join eiam_permission p "
                        + "on p.id=rp.permission_id "
                        + "and p.tenant_id=rp.tenant_id "
                        + "where r.tenant_id='"
                        + tenantId
                        + "'::uuid "
                        + "and r.code='AI_ADMIN' "
                        + "and p.code in "
                        + "('ai.request.create','ai.runtime.execute')"
        );
    }

    private static long aiAdminUserAssignmentCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalar(
                connection,
                "select count(*) "
                        + "from eiam_user_role ur "
                        + "join eiam_role r "
                        + "on r.id=ur.role_id "
                        + "and r.tenant_id=ur.tenant_id "
                        + "where r.tenant_id='"
                        + tenantId
                        + "'::uuid "
                        + "and r.code='AI_ADMIN'"
        );
    }

    private static long aiAdminInvitationAssignmentCount(
            Connection connection,
            UUID tenantId
    ) throws Exception {

        return scalar(
                connection,
                "select count(*) "
                        + "from eiam_invitation_role ir "
                        + "join eiam_role r "
                        + "on r.id=ir.role_id "
                        + "and r.tenant_id=ir.tenant_id "
                        + "where r.tenant_id='"
                        + tenantId
                        + "'::uuid "
                        + "and r.code='AI_ADMIN'"
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

    private static String quotedPermissionList() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < AI_ADMIN_PERMISSIONS.length; i++) {
            if (i > 0) {
                result.append(",");
            }

            result.append("'")
                    .append(AI_ADMIN_PERMISSIONS[i])
                    .append("'");
        }

        return result.toString();
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

        try (Statement statement=connection.createStatement();
             ResultSet rs=statement.executeQuery(sql)) {

            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }
}
