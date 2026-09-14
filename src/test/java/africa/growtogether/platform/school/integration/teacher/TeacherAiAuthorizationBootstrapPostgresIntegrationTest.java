package africa.growtogether.platform.school.integration.teacher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeacherAiAuthorizationBootstrapPostgresIntegrationTest {

    private static final int TEACHER_BASELINE_SIZE = 7;
    private static final int TEACHER_AI_PERMISSION_COUNT = 3;

    @Test
    void freshMigrationBootstrapsTeacherRoleAndMinimumPermissions()
            throws Exception {

        try (PostgreSQLContainer<?> postgres =
                     new PostgreSQLContainer<>("postgres:16-alpine")) {

            postgres.start();

            fullFlyway(postgres).migrate();

            try (Connection connection =
                         postgres.createConnection("")) {

                assertTeacherBaseline(connection);

                assertEquals(
                        1,
                        scalar(
                                connection,
                                """
                                select count(*)
                                from flyway_schema_history
                                where version = '269'
                                  and success = true
                                """
                        )
                );
            }
        }
    }

    @Test
    void upgradePreservesExistingTeacherRoleAndBackfillsMissingGrants()
            throws Exception {

        try (PostgreSQLContainer<?> postgres =
                     new PostgreSQLContainer<>("postgres:16-alpine")) {

            postgres.start();

            Flyway through268 =
                    Flyway.configure()
                            .dataSource(
                                    postgres.getJdbcUrl(),
                                    postgres.getUsername(),
                                    postgres.getPassword()
                            )
                            .locations("classpath:db/migration")
                            .target(
                                    MigrationVersion.fromVersion("268")
                            )
                            .load();

            through268.migrate();

            String existingTeacherId;

            try (Connection connection =
                         postgres.createConnection("");
                 Statement statement =
                         connection.createStatement()) {

                int inserted =
                        statement.executeUpdate(
                                """
                                insert into eiam_role (
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
                                select
                                    gen_random_uuid(),
                                    tenant.id,
                                    'TEACHER',
                                    'Teacher',
                                    'Existing runtime Teacher role.',
                                    false,
                                    current_timestamp,
                                    'TEST-EXISTING-RUNTIME',
                                    current_timestamp,
                                    'TEST-EXISTING-RUNTIME',
                                    0,
                                    'ACTIVE'
                                from eiam_tenant tenant
                                where not exists (
                                    select 1
                                    from eiam_role existing
                                    where existing.tenant_id = tenant.id
                                      and existing.code = 'TEACHER'
                                )
                                """
                        );

                assertTrue(
                        inserted > 0,
                        "Pre-V269 simulation must create an existing Teacher role."
                );

                existingTeacherId =
                        stringScalar(
                                connection,
                                """
                                select id::text
                                from eiam_role
                                where code = 'TEACHER'
                                  and created_by =
                                      'TEST-EXISTING-RUNTIME'
                                limit 1
                                """
                        );

                assertEquals(
                        0,
                        teacherAiGrantCount(connection)
                );
            }

            fullFlyway(postgres).migrate();

            try (Connection connection =
                         postgres.createConnection("")) {

                assertTeacherBaseline(connection);

                assertEquals(
                        existingTeacherId,
                        stringScalar(
                                connection,
                                """
                                select id::text
                                from eiam_role
                                where code = 'TEACHER'
                                  and created_by =
                                      'TEST-EXISTING-RUNTIME'
                                limit 1
                                """
                        ),
                        "V269 must preserve the existing Teacher role identity."
                );
            }
        }
    }

    private static Flyway fullFlyway(
            PostgreSQLContainer<?> postgres
    ) {

        return Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .locations("classpath:db/migration")
                .load();
    }

    private static void assertTeacherBaseline(
            Connection connection
    ) throws SQLException {

        long tenantCount =
                scalar(
                        connection,
                        """
                        select count(*)
                        from eiam_tenant
                        """
                );

        assertTrue(
                tenantCount > 0,
                "Migration baseline must contain a tenant."
        );

        long teacherRoleCount =
                scalar(
                        connection,
                        """
                        select count(*)
                        from eiam_role
                        where code = 'TEACHER'
                          and status = 'ACTIVE'
                        """
                );

        assertEquals(
                tenantCount,
                teacherRoleCount,
                "Every tenant must have one active TEACHER role."
        );

        long aiPermissionCount =
                scalar(
                        connection,
                        """
                        select count(*)
                        from eiam_permission
                        where code in (
                            'ai.request.create',
                            'ai.request.read',
                            'ai.runtime.execute'
                        )
                          and status = 'ACTIVE'
                        """
                );

        assertEquals(
                tenantCount * TEACHER_AI_PERMISSION_COUNT,
                aiPermissionCount
        );

        long teacherBaselineGrantCount =
                scalar(
                        connection,
                        """
                        select count(*)
                        from eiam_role role
                        join eiam_role_permission grant_row
                          on grant_row.tenant_id = role.tenant_id
                         and grant_row.role_id = role.id
                         and grant_row.status = 'ACTIVE'
                        join eiam_permission permission
                          on permission.tenant_id =
                             grant_row.tenant_id
                         and permission.id =
                             grant_row.permission_id
                         and permission.status = 'ACTIVE'
                        where role.code = 'TEACHER'
                          and role.status = 'ACTIVE'
                          and permission.code in (
                              'school.academic.curriculum.read',
                              'school.academic.class-grade.read',
                              'school.academic.subject.read',
                              'school.academic.teaching-assignment.read',
                              'ai.request.create',
                              'ai.request.read',
                              'ai.runtime.execute'
                          )
                        """
                );

        assertEquals(
                teacherRoleCount * TEACHER_BASELINE_SIZE,
                teacherBaselineGrantCount
        );

        assertEquals(
                teacherRoleCount * TEACHER_AI_PERMISSION_COUNT,
                teacherAiGrantCount(connection)
        );
    }

    private static long teacherAiGrantCount(
            Connection connection
    ) throws SQLException {

        return scalar(
                connection,
                """
                select count(*)
                from eiam_role role
                join eiam_role_permission grant_row
                  on grant_row.tenant_id = role.tenant_id
                 and grant_row.role_id = role.id
                 and grant_row.status = 'ACTIVE'
                join eiam_permission permission
                  on permission.tenant_id =
                     grant_row.tenant_id
                 and permission.id =
                     grant_row.permission_id
                 and permission.status = 'ACTIVE'
                where role.code = 'TEACHER'
                  and role.status = 'ACTIVE'
                  and permission.code in (
                      'ai.request.create',
                      'ai.request.read',
                      'ai.runtime.execute'
                  )
                """
        );
    }

    private static long scalar(
            Connection connection,
            String sql
    ) throws SQLException {

        try (Statement statement =
                     connection.createStatement();
             ResultSet result =
                     statement.executeQuery(sql)) {

            assertTrue(result.next());
            return result.getLong(1);
        }
    }

    private static String stringScalar(
            Connection connection,
            String sql
    ) throws SQLException {

        try (Statement statement =
                     connection.createStatement();
             ResultSet result =
                     statement.executeQuery(sql)) {

            assertTrue(result.next());
            return result.getString(1);
        }
    }
}
