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
class TeacherProgrammePermissionHistoricalReconciliationPostgresIntegrationTest {

    private static final String PROGRAMME_PERMISSION =
            "school.teacher.programme.read";

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_teacher_programme_permission_test"
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
    void historicalTenantsReceiveDedicatedTeacherProgrammeReadPermission() {

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '278'
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

        Integer canonicalDefinitions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE code = ?
                          AND name = 'Read Teacher Programme'
                          AND module = 'SCHOOL_TEACHER'
                          AND description =
                              'Allows an authenticated teacher to view their own authorised Today''s Programme, including applicable teaching lessons and teacher-visible school calendar events within their authenticated tenant.'
                          AND system_permission = FALSE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        PROGRAMME_PERMISSION
                );

        assertThat(canonicalDefinitions)
                .isEqualTo(tenants);

        Integer teacherRoles =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE code = 'TEACHER'
                          AND system_role = FALSE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(teacherRoles)
                .isEqualTo(tenants);

        Integer teacherProgrammeGrants =
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
                          AND p.code = ?
                          AND p.status = 'ACTIVE'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        PROGRAMME_PERMISSION
                );

        assertThat(teacherProgrammeGrants)
                .isEqualTo(teacherRoles);

        Integer nonTeacherProgrammeGrants =
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
                          AND p.code = ?
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        PROGRAMME_PERMISSION
                );

        assertThat(nonTeacherProgrammeGrants)
                .isZero();

        Integer teacherCalendarReadGrants =
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
                          AND p.code = 'school.academic.calendar.read'
                          AND p.status = 'ACTIVE'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(teacherCalendarReadGrants)
                .isZero();
    }
}
