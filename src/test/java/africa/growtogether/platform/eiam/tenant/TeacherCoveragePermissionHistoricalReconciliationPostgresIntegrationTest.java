package africa.growtogether.platform.eiam.tenant;

import static org.assertj.core.api.Assertions.assertThat;

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
class TeacherCoveragePermissionHistoricalReconciliationPostgresIntegrationTest {

    private static final String READ_PERMISSION =
            "school.teacher.coverage.read";

    private static final String UPDATE_PERMISSION =
            "school.teacher.coverage.update";

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_teacher_coverage_history_test"
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
    private JdbcTemplate jdbc;

    @Test
    void historicalTenantsReceiveDedicatedTeacherCoveragePermissions() {

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '281'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(migrationCount)
                .isEqualTo(1);

        Integer tenants =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_tenant
                        """,
                        Integer.class
                );

        assertThat(tenants)
                .isNotNull()
                .isPositive();

        Integer permissionDefinitions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE code IN (?, ?)
                          AND module = 'SCHOOL_TEACHER'
                          AND system_permission = FALSE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        READ_PERMISSION,
                        UPDATE_PERMISSION
                );

        assertThat(permissionDefinitions)
                .isEqualTo(
                        tenants * 2
                );

        Integer teacherRoles =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE code = 'TEACHER'
                          AND status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(teacherRoles)
                .isNotNull()
                .isPositive();

        Integer teacherCoverageGrants =
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
                        WHERE r.code = 'TEACHER'
                          AND r.status = 'ACTIVE'
                          AND p.code IN (?, ?)
                          AND p.status = 'ACTIVE'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        READ_PERMISSION,
                        UPDATE_PERMISSION
                );

        assertThat(teacherCoverageGrants)
                .isEqualTo(
                        teacherRoles * 2
                );

        Integer nonTeacherCoverageGrants =
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
                        WHERE r.code <> 'TEACHER'
                          AND p.code IN (?, ?)
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        READ_PERMISSION,
                        UPDATE_PERMISSION
                );

        assertThat(nonTeacherCoverageGrants)
                .isZero();

        Integer crossTenantCoverageGrants =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role_permission rp
                        JOIN eiam_role r
                          ON r.id = rp.role_id
                        JOIN eiam_permission p
                          ON p.id = rp.permission_id
                        WHERE p.code IN (?, ?)
                          AND (
                              rp.tenant_id <> r.tenant_id
                              OR rp.tenant_id <> p.tenant_id
                          )
                        """,
                        Integer.class,
                        READ_PERMISSION,
                        UPDATE_PERMISSION
                );

        assertThat(crossTenantCoverageGrants)
                .isZero();
    }
}
