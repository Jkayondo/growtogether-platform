package africa.growtogether.platform.school.academic.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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
class TeacherCoveragePostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_teacher_coverage_lifecycle_test"
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

    @Autowired
    private TeacherCoverageRepository repository;

    @Autowired
    private TeacherCoverageService service;

    @Test
    void freshFlywayCreatesTeacherCoverageSchemaAndConstraints() {

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '210'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(migrationCount)
                .isEqualTo(1);

        Integer tableCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name = 'gts_teacher_coverage'
                        """,
                        Integer.class
                );

        assertThat(tableCount)
                .isEqualTo(1);

        Integer lifecycleChecks =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM information_schema.table_constraints
                        WHERE table_schema = 'public'
                          AND table_name = 'gts_teacher_coverage'
                          AND constraint_type = 'CHECK'
                          AND constraint_name IN (
                              'ck_teacher_coverage_type',
                              'ck_teacher_coverage_status'
                          )
                        """,
                        Integer.class
                );

        assertThat(lifecycleChecks)
                .isEqualTo(2);

        Integer foreignKeys =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM information_schema.table_constraints
                        WHERE table_schema = 'public'
                          AND table_name = 'gts_teacher_coverage'
                          AND constraint_type = 'FOREIGN KEY'
                        """,
                        Integer.class
                );

        /*
         * V210 deliberately defines one physical FK: tenant_id -> eiam_tenant.
         * Teacher/assignment ownership is enforced by tenant-scoped application
         * access rather than by FK constraints in this migration.
         */
        assertThat(foreignKeys)
                .isEqualTo(1);

        Integer indexes =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM pg_indexes
                        WHERE schemaname = 'public'
                          AND tablename = 'gts_teacher_coverage'
                          AND indexname IN (
                              'ix_teacher_coverage_teacher',
                              'ix_teacher_coverage_assignment',
                              'ix_teacher_coverage_curriculum',
                              'ix_teacher_coverage_term'
                          )
                        """,
                        Integer.class
                );

        assertThat(indexes)
                .isEqualTo(4);
    }

    @Test
    void persistsDefaultStateAndScopesRepositoryQueries() {

        UUID tenantId =
                activeTenant();

        UUID teacherOne =
                UUID.randomUUID();

        UUID teacherTwo =
                UUID.randomUUID();

        UUID assignmentOne =
                UUID.randomUUID();

        UUID assignmentTwo =
                UUID.randomUUID();

        UUID first =
                UUID.randomUUID();

        UUID second =
                UUID.randomUUID();

        UUID third =
                UUID.randomUUID();

        insertDefaultCoverage(
                first,
                tenantId,
                teacherOne,
                assignmentOne,
                "Fractions"
        );

        insertDefaultCoverage(
                second,
                tenantId,
                teacherOne,
                assignmentTwo,
                "Decimals"
        );

        insertDefaultCoverage(
                third,
                tenantId,
                teacherTwo,
                assignmentOne,
                "Measurement"
        );

        TeacherCoverage persisted =
                repository
                        .findByTenantIdAndId(
                                tenantId,
                                first
                        )
                        .orElseThrow();

        assertThat(
                persisted.getCoverageStatus()
        )
                .isEqualTo(
                        "NOT_STARTED"
                );

        assertThat(
                repository
                        .findByTenantIdAndTeacherProfileId(
                                tenantId,
                                teacherOne
                        )
        )
                .extracting(
                        TeacherCoverage::getCoverageItem
                )
                .containsExactlyInAnyOrder(
                        "Fractions",
                        "Decimals"
                );

        assertThat(
                repository
                        .findByTenantIdAndTeachingAssignmentId(
                                tenantId,
                                assignmentOne
                        )
        )
                .extracting(
                        TeacherCoverage::getCoverageItem
                )
                .containsExactlyInAnyOrder(
                        "Fractions",
                        "Measurement"
                );

        assertThat(
                repository.findByTenantIdAndId(
                        UUID.randomUUID(),
                        first
                )
        )
                .isEmpty();
    }

    @Test
    void lifecycleTransitionsPersistAndRemainTenantScoped() {

        UUID tenantId =
                activeTenant();

        UUID coverageId =
                UUID.randomUUID();

        insertDefaultCoverage(
                coverageId,
                tenantId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Photosynthesis"
        );

        TeacherCoverage inProgress =
                service.markInProgress(
                        tenantId,
                        coverageId
                );

        assertThat(
                inProgress.getCoverageStatus()
        )
                .isEqualTo(
                        "IN_PROGRESS"
                );

        assertDatabaseStatus(
                coverageId,
                "IN_PROGRESS"
        );

        TeacherCoverage remediation =
                service.markRequiresRemediation(
                        tenantId,
                        coverageId
                );

        assertThat(
                remediation.getCoverageStatus()
        )
                .isEqualTo(
                        "REQUIRES_REMEDIATION"
                );

        assertDatabaseStatus(
                coverageId,
                "REQUIRES_REMEDIATION"
        );

        TeacherCoverage ahead =
                service.markAheadOfSchedule(
                        tenantId,
                        coverageId
                );

        assertThat(
                ahead.getCoverageStatus()
        )
                .isEqualTo(
                        "AHEAD_OF_SCHEDULE"
                );

        assertDatabaseStatus(
                coverageId,
                "AHEAD_OF_SCHEDULE"
        );

        TeacherCoverage completed =
                service.markCompleted(
                        tenantId,
                        coverageId,
                        "Completed with practical activity"
                );

        assertThat(
                completed.getCoverageStatus()
        )
                .isEqualTo(
                        "COMPLETED"
                );

        assertThat(
                completed.getCompletionDate()
        )
                .isNotNull();

        assertThat(
                completed.getTeacherRemarks()
        )
                .isEqualTo(
                        "Completed with practical activity"
                );

        CoverageCompletion persisted =
                jdbc.queryForObject(
                        """
                        SELECT
                            coverage_status,
                            completion_date,
                            teacher_remarks
                        FROM gts_teacher_coverage
                        WHERE id = ?
                        """,
                        (rs, rowNum) ->
                                new CoverageCompletion(
                                        rs.getString(
                                                "coverage_status"
                                        ),
                                        rs.getObject(
                                                "completion_date",
                                                LocalDate.class
                                        ),
                                        rs.getString(
                                                "teacher_remarks"
                                        )
                                ),
                        coverageId
                );

        assertThat(persisted)
                .isNotNull();

        assertThat(
                persisted.status()
        )
                .isEqualTo(
                        "COMPLETED"
                );

        assertThat(
                persisted.completionDate()
        )
                .isNotNull();

        assertThat(
                persisted.remarks()
        )
                .isEqualTo(
                        "Completed with practical activity"
                );

        assertThatThrownBy(
                () -> service.markInProgress(
                        UUID.randomUUID(),
                        coverageId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Teacher coverage not found."
                );
    }

    @Test
    void databaseRejectsInvalidTenantAndInvalidLifecycleStatus() {

        UUID validTenant =
                activeTenant();

        assertThatThrownBy(
                () -> insertDefaultCoverage(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Invalid tenant coverage"
                )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );

        assertThatThrownBy(
                () -> insertCoverageWithStatus(
                        UUID.randomUUID(),
                        validTenant,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Invalid status coverage",
                        "NOT_A_REAL_STATUS"
                )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    private UUID activeTenant() {

        UUID tenantId =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_tenant
                        WHERE status = 'ACTIVE'
                        ORDER BY created_at, id
                        LIMIT 1
                        """,
                        UUID.class
                );

        assertThat(tenantId)
                .isNotNull();

        return tenantId;
    }

    private void insertDefaultCoverage(
            UUID id,
            UUID tenantId,
            UUID teacherProfileId,
            UUID teachingAssignmentId,
            String coverageItem
    ) {
        jdbc.update(
                """
                INSERT INTO gts_teacher_coverage (
                    id,
                    tenant_id,
                    teacher_profile_id,
                    teaching_assignment_id,
                    academic_year_id,
                    curriculum_version_id,
                    class_grade_id,
                    coverage_type,
                    coverage_item,
                    planned_week,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?,
                    'TOPIC',
                    ?,
                    1,
                    CURRENT_TIMESTAMP,
                    'teacher-coverage-postgres-test',
                    CURRENT_TIMESTAMP,
                    'teacher-coverage-postgres-test',
                    0,
                    'ACTIVE'
                )
                """,
                id,
                tenantId,
                teacherProfileId,
                teachingAssignmentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                coverageItem
        );
    }

    private void insertCoverageWithStatus(
            UUID id,
            UUID tenantId,
            UUID teacherProfileId,
            UUID teachingAssignmentId,
            String coverageItem,
            String coverageStatus
    ) {
        jdbc.update(
                """
                INSERT INTO gts_teacher_coverage (
                    id,
                    tenant_id,
                    teacher_profile_id,
                    teaching_assignment_id,
                    academic_year_id,
                    curriculum_version_id,
                    class_grade_id,
                    coverage_type,
                    coverage_item,
                    planned_week,
                    coverage_status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?,
                    'TOPIC',
                    ?,
                    1,
                    ?,
                    CURRENT_TIMESTAMP,
                    'teacher-coverage-postgres-test',
                    CURRENT_TIMESTAMP,
                    'teacher-coverage-postgres-test',
                    0,
                    'ACTIVE'
                )
                """,
                id,
                tenantId,
                teacherProfileId,
                teachingAssignmentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                coverageItem,
                coverageStatus
        );
    }

    private void assertDatabaseStatus(
            UUID coverageId,
            String expected
    ) {
        String status =
                jdbc.queryForObject(
                        """
                        SELECT coverage_status
                        FROM gts_teacher_coverage
                        WHERE id = ?
                        """,
                        String.class,
                        coverageId
                );

        assertThat(status)
                .isEqualTo(
                        expected
                );
    }

    private record CoverageCompletion(
            String status,
            LocalDate completionDate,
            String remarks
    ) {
    }
}
