package africa.growtogether.platform.eiam.permission;

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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class SchoolAdminPermissionBackfillIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_eiam_backfill_test"
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
    void historicalSchoolAdminPermissionsAreFullyBackfilled() {

        Integer roleCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role r
                        JOIN eiam_tenant t
                          ON t.id = r.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'SCHOOL_ADMIN'
                          AND r.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(roleCount)
                .isEqualTo(1);

        Integer definitionCount =
                jdbc.queryForObject(
                        """
                        WITH intended_permission(code) AS (
                            VALUES
                                ('school.academic.year.create'),
                                ('school.academic.year.read'),
                                ('school.academic.term.create'),
                                ('school.academic.term.read'),
                                ('school.academic.curriculum.create'),
                                ('school.academic.curriculum.read'),
                                ('school.academic.curriculum.manage'),
                                ('school.academic.curriculum.version.create'),
                                ('school.academic.curriculum.version.read'),
                                ('school.academic.curriculum.version.manage'),
                                ('school.academic.curriculum.learning-area.create'),
                                ('school.academic.curriculum.learning-area.read'),
                                ('school.academic.curriculum.learning-area.manage'),
                                ('school.academic.subject.create'),
                                ('school.academic.subject.read'),
                                ('school.academic.subject.manage'),
                                ('school.academic.class-grade.create'),
                                ('school.academic.class-grade.read'),
                                ('school.academic.class-grade.manage'),
                                ('school.academic.campus.create'),
                                ('school.academic.campus.read'),
                                ('school.academic.campus.manage'),
                                ('school.academic.class-offering.create'),
                                ('school.academic.class-offering.read'),
                                ('school.academic.class-offering.manage'),
                                ('enterprise.workforce.member.create'),
                                ('enterprise.workforce.member.read'),
                                ('enterprise.workforce.member.manage'),
                                ('school.academic.teacher-profile.create'),
                                ('school.academic.teacher-profile.read'),
                                ('school.academic.teacher-profile.manage'),
                                ('school.academic.teacher-subject-qualification.create'),
                                ('school.academic.teacher-subject-qualification.read'),
                                ('school.academic.teacher-subject-qualification.manage'),
                                ('school.academic.teaching-assignment.create'),
                                ('school.academic.teaching-assignment.read'),
                                ('school.academic.teaching-assignment.manage'),
                                ('school.academic.stream.create'),
                                ('school.academic.stream.read'),
                                ('school.academic.stream.manage'),
                                ('school.student.create'),
                                ('school.student.read'),
                                ('school.student.manage'),
                                ('school.enrollment.create'),
                                ('school.enrollment.read'),
                                ('school.enrollment.manage'),
                                ('school.guardian.create'),
                                ('school.guardian.read'),
                                ('school.guardian.manage'),
                                ('school.guardian-relationship.create'),
                                ('school.guardian-relationship.read'),
                                ('school.guardian-relationship.manage'),
                                ('school.timetable.create'),
                                ('school.timetable.read'),
                                ('school.timetable.manage'),
                                ('school.timetable.review'),
                                ('school.timetable.approve'),
                                ('school.timetable.publish'),
                                ('school.timetable.activate'),
                                ('school.timetable.suspend'),
                                ('school.admission.payment.read'),
                                ('school.admission.payment.manage'),
                                ('school.admission.payment.waive'),
                                ('school.admission.payment.reconcile')
                        )
                        SELECT COUNT(*)
                        FROM intended_permission intended
                        JOIN eiam_tenant t
                          ON t.code = 'GT-SCHOOL'
                        JOIN eiam_permission p
                          ON p.tenant_id = t.id
                         AND p.code = intended.code
                        """,
                        Integer.class
                );

        assertThat(definitionCount)
                .isEqualTo(64);

        Integer assignmentCount =
                jdbc.queryForObject(
                        """
                        WITH intended_permission(code) AS (
                            VALUES
                                ('school.academic.year.create'),
                                ('school.academic.year.read'),
                                ('school.academic.term.create'),
                                ('school.academic.term.read'),
                                ('school.academic.curriculum.create'),
                                ('school.academic.curriculum.read'),
                                ('school.academic.curriculum.manage'),
                                ('school.academic.curriculum.version.create'),
                                ('school.academic.curriculum.version.read'),
                                ('school.academic.curriculum.version.manage'),
                                ('school.academic.curriculum.learning-area.create'),
                                ('school.academic.curriculum.learning-area.read'),
                                ('school.academic.curriculum.learning-area.manage'),
                                ('school.academic.subject.create'),
                                ('school.academic.subject.read'),
                                ('school.academic.subject.manage'),
                                ('school.academic.class-grade.create'),
                                ('school.academic.class-grade.read'),
                                ('school.academic.class-grade.manage'),
                                ('school.academic.campus.create'),
                                ('school.academic.campus.read'),
                                ('school.academic.campus.manage'),
                                ('school.academic.class-offering.create'),
                                ('school.academic.class-offering.read'),
                                ('school.academic.class-offering.manage'),
                                ('enterprise.workforce.member.create'),
                                ('enterprise.workforce.member.read'),
                                ('enterprise.workforce.member.manage'),
                                ('school.academic.teacher-profile.create'),
                                ('school.academic.teacher-profile.read'),
                                ('school.academic.teacher-profile.manage'),
                                ('school.academic.teacher-subject-qualification.create'),
                                ('school.academic.teacher-subject-qualification.read'),
                                ('school.academic.teacher-subject-qualification.manage'),
                                ('school.academic.teaching-assignment.create'),
                                ('school.academic.teaching-assignment.read'),
                                ('school.academic.teaching-assignment.manage'),
                                ('school.academic.stream.create'),
                                ('school.academic.stream.read'),
                                ('school.academic.stream.manage'),
                                ('school.student.create'),
                                ('school.student.read'),
                                ('school.student.manage'),
                                ('school.enrollment.create'),
                                ('school.enrollment.read'),
                                ('school.enrollment.manage'),
                                ('school.guardian.create'),
                                ('school.guardian.read'),
                                ('school.guardian.manage'),
                                ('school.guardian-relationship.create'),
                                ('school.guardian-relationship.read'),
                                ('school.guardian-relationship.manage'),
                                ('school.timetable.create'),
                                ('school.timetable.read'),
                                ('school.timetable.manage'),
                                ('school.timetable.review'),
                                ('school.timetable.approve'),
                                ('school.timetable.publish'),
                                ('school.timetable.activate'),
                                ('school.timetable.suspend'),
                                ('school.admission.payment.read'),
                                ('school.admission.payment.manage'),
                                ('school.admission.payment.waive'),
                                ('school.admission.payment.reconcile')
                        )
                        SELECT COUNT(*)
                        FROM intended_permission intended
                        JOIN eiam_tenant t
                          ON t.code = 'GT-SCHOOL'
                        JOIN eiam_role r
                          ON r.tenant_id = t.id
                         AND r.code = 'SCHOOL_ADMIN'
                        JOIN eiam_permission p
                          ON p.tenant_id = t.id
                         AND p.code = intended.code
                        JOIN eiam_role_permission rp
                          ON rp.tenant_id = t.id
                         AND rp.role_id = r.id
                         AND rp.permission_id = p.id
                         AND rp.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(assignmentCount)
                .isEqualTo(64);

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '153'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(migrationCount)
                .isEqualTo(1);
    }
}
