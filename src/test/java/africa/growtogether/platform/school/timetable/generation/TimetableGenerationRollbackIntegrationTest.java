package africa.growtogether.platform.school.timetable.generation;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class TimetableGenerationRollbackIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_timetable_rollback"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    @DynamicPropertySource
    static void databaseProperties(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
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
    private TimetableGenerationExecutionService execution;

    @MockitoBean
    private TimetableGenerationSnapshotService snapshots;

    @MockitoBean
    private DeterministicTimetableCandidateGenerator generator;


    private UUID tenantId;
    private UUID requestId;

    private UUID academicYearId;
    private UUID academicTermId;
    private UUID campusId;

    private UUID classGradeId;
    private UUID classOfferingId;

    private UUID subjectId;
    private UUID subjectOfferingId;

    private UUID teacherProfileId;
    private UUID teachingAssignmentId;

    private UUID bellScheduleId;
    private UUID bellPeriodId;


    @BeforeEach
    void setUp() {

        tenantId = UUID.randomUUID();
        requestId = UUID.randomUUID();

        academicYearId = UUID.randomUUID();
        academicTermId = UUID.randomUUID();
        campusId = UUID.randomUUID();

        classGradeId = UUID.randomUUID();
        classOfferingId = UUID.randomUUID();

        subjectId = UUID.randomUUID();
        subjectOfferingId = UUID.randomUUID();

        teacherProfileId = UUID.randomUUID();
        teachingAssignmentId = UUID.randomUUID();

        bellScheduleId = UUID.randomUUID();
        bellPeriodId = UUID.randomUUID();

        insertFixture();

        RequestContextHolder.set(
                new RequestContext(
                        "a10-rollback-test",
                        tenantId.toString()
                )
        );
    }


    @AfterEach
    void clearContext() {

        RequestContextHolder.clear();
    }


    @Test
    void rollsBackEntireGenerationWhenLaterAuthoritativeEntryFails() {

        TimetableGenerationSnapshot snapshot =
                snapshot();

        UUID nonexistentSubjectOfferingId =
                UUID.randomUUID();

        TimetableGenerationCandidate candidate =
                new TimetableGenerationCandidate(
                        requestId,
                        "COMPLETE",
                        2,
                        2,
                        List.of(
                                validPlacement(),
                                invalidSecondPlacement(
                                        nonexistentSubjectOfferingId
                                )
                        ),
                        List.of()
                );

        when(
                snapshots.build(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                snapshot
        );

        when(
                generator.generate(
                        tenantId,
                        snapshot
                )
        ).thenReturn(
                candidate
        );

        assertThatThrownBy(
                () ->
                        execution.execute(
                                tenantId,
                                requestId
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "Subject offering not found for tenant"
                );

        /*
         * These queries execute after execute() has thrown and its
         * Spring transaction has completed.
         *
         * Therefore they observe committed PostgreSQL state rather
         * than the failed generation transaction.
         */
        Integer generatedTimetables =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_timetable
                        WHERE tenant_id = ?
                          AND generation_reference = ?
                        """,
                        Integer.class,
                        tenantId,
                        requestId
                );

        assertThat(
                generatedTimetables
        ).isZero();


        Integer generatedEntries =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_timetable_entry e
                        JOIN gts_timetable t
                          ON t.id = e.timetable_id
                        WHERE t.tenant_id = ?
                          AND t.generation_reference = ?
                        """,
                        Integer.class,
                        tenantId,
                        requestId
                );

        assertThat(
                generatedEntries
        ).isZero();


        Integer generatedHistory =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_timetable_change_history
                        WHERE tenant_id = ?
                          AND correlation_id = ?
                        """,
                        Integer.class,
                        tenantId,
                        requestId.toString()
                );

        assertThat(
                generatedHistory
        ).isZero();


        String generationStatus =
                jdbc.queryForObject(
                        """
                        SELECT generation_status
                        FROM gts_timetable_generation_request
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        String.class,
                        tenantId,
                        requestId
                );

        assertThat(
                generationStatus
        ).isEqualTo(
                "READY"
        );


        UUID resultTimetableId =
                jdbc.queryForObject(
                        """
                        SELECT result_timetable_id
                        FROM gts_timetable_generation_request
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        (rs, rowNum) ->
                                rs.getObject(
                                        "result_timetable_id",
                                        UUID.class
                                ),
                        tenantId,
                        requestId
                );

        assertThat(
                resultTimetableId
        ).isNull();
    }


    private TimetableGenerationSnapshot snapshot() {

        TimetableGenerationSnapshot.BellSlot slot =
                new TimetableGenerationSnapshot.BellSlot(
                        bellPeriodId,
                        "P1",
                        1,
                        "TEACHING",
                        LocalTime.of(
                                8,
                                0
                        ),
                        LocalTime.of(
                                8,
                                40
                        )
                );

        return new TimetableGenerationSnapshot(
                requestId,
                "GEN-A10-ROLLBACK",
                new TimetableGenerationSnapshot.Scope(
                        academicYearId,
                        academicTermId,
                        campusId,
                        bellScheduleId,
                        "MASTER",
                        LocalDate.of(
                                2026,
                                2,
                                1
                        ),
                        LocalDate.of(
                                2026,
                                4,
                                30
                        ),
                        "Africa/Kampala",
                        "RULE_ENGINE",
                        null,
                        "A10 transactional rollback verification"
                ),
                List.of(
                        "MONDAY"
                ),
                List.of(
                        slot
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }


    private TimetableGenerationCandidate.Placement validPlacement() {

        return new TimetableGenerationCandidate.Placement(
                "MONDAY",
                bellPeriodId,
                "P1",
                classOfferingId,
                subjectOfferingId,
                classGradeId,
                null,
                subjectId,
                teachingAssignmentId,
                teacherProfileId,
                null,
                "First valid persisted placement"
        );
    }


    private TimetableGenerationCandidate.Placement invalidSecondPlacement(
            UUID nonexistentSubjectOfferingId
    ) {

        return new TimetableGenerationCandidate.Placement(
                "MONDAY",
                bellPeriodId,
                "P1",
                classOfferingId,
                nonexistentSubjectOfferingId,
                classGradeId,
                null,
                subjectId,
                teachingAssignmentId,
                teacherProfileId,
                null,
                "Second placement deliberately fails authoritative validation"
        );
    }


    private void insertFixture() {

        UUID organizationId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        UUID workforceMemberId =
                UUID.randomUUID();

        String auditUser =
                "a10-integration-test";


        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                organizationId,
                "ORG-" + shortId(organizationId),
                "A10 Integration Organisation"
        );


        jdbc.update(
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
                VALUES (?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, 0)
                """,
                tenantId,
                organizationId,
                "TEN-" + shortId(tenantId),
                "A10 Integration Tenant"
        );


        jdbc.update(
                """
                INSERT INTO gts_school_profile (
                    id,
                    tenant_id,
                    school_code,
                    school_name,
                    country_code,
                    default_currency,
                    timezone,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'UG', 'UGX', 'Africa/Kampala',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                schoolProfileId,
                tenantId,
                "SCH-" + shortId(schoolProfileId),
                "A10 Integration School",
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_campus (
                    id,
                    tenant_id,
                    school_profile_id,
                    campus_code,
                    campus_name,
                    main_campus,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    TRUE, 'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                campusId,
                tenantId,
                schoolProfileId,
                "MAIN-" + shortId(campusId),
                "Main Campus",
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_academic_year (
                    id,
                    tenant_id,
                    academic_year_code,
                    academic_year_name,
                    start_date,
                    end_date,
                    current_year,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, '2026', 'Academic Year 2026',
                    DATE '2026-01-01',
                    DATE '2026-12-31',
                    TRUE, 'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                academicYearId,
                tenantId,
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_academic_term (
                    id,
                    tenant_id,
                    academic_year_id,
                    term_code,
                    term_name,
                    sequence_number,
                    start_date,
                    end_date,
                    current_term,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?,
                    'T1', 'Term One', 1,
                    DATE '2026-02-01',
                    DATE '2026-04-30',
                    TRUE, 'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                academicTermId,
                tenantId,
                academicYearId,
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_education_level (
                    id,
                    tenant_id,
                    level_code,
                    level_name,
                    sequence_number,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, 'PRIMARY', 'Primary', 1,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                educationLevelId,
                tenantId,
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_class_grade (
                    id,
                    tenant_id,
                    education_level_id,
                    class_code,
                    class_name,
                    sequence_number,
                    capacity,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?,
                    'P7', 'Primary Seven', 7, 40,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                classGradeId,
                tenantId,
                educationLevelId,
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_subject (
                    id,
                    tenant_id,
                    subject_code,
                    subject_name,
                    subject_type,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?,
                    'SCI', 'Science', 'CORE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                subjectId,
                tenantId,
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_class_offering (
                    id,
                    tenant_id,
                    offering_code,
                    academic_year_id,
                    campus_id,
                    class_grade_id,
                    planned_capacity,
                    offering_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, 40,
                    'ACTIVE', 'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                classOfferingId,
                tenantId,
                "P7-2026-" + shortId(classOfferingId),
                academicYearId,
                campusId,
                classGradeId,
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_subject_offering (
                    id,
                    tenant_id,
                    subject_offering_code,
                    class_offering_id,
                    academic_term_id,
                    subject_id,
                    weekly_periods,
                    offering_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, 5,
                    'ACTIVE', 'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                subjectOfferingId,
                tenantId,
                "SCI-P7-" + shortId(subjectOfferingId),
                classOfferingId,
                academicTermId,
                subjectId,
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO ewf_workforce_member (
                    id,
                    tenant_id,
                    workforce_number,
                    first_name,
                    last_name,
                    workforce_category,
                    workforce_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, 'Test', 'Teacher',
                    'EMPLOYEE', 'ACTIVE', 'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                workforceMemberId,
                tenantId,
                "WF-" + shortId(workforceMemberId),
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_teacher_profile (
                    id,
                    tenant_id,
                    workforce_member_id,
                    teacher_number,
                    teacher_category,
                    teaching_status,
                    qualified_for_boarding_duty,
                    qualified_for_special_needs,
                    qualified_for_counselling,
                    maximum_weekly_periods,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'SUBJECT_TEACHER', 'ACTIVE',
                    FALSE, FALSE, FALSE, 30,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                teacherProfileId,
                tenantId,
                workforceMemberId,
                "T-" + shortId(teacherProfileId),
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_teaching_assignment (
                    id,
                    tenant_id,
                    assignment_reference,
                    teacher_profile_id,
                    academic_year_id,
                    academic_term_id,
                    campus_id,
                    class_grade_id,
                    subject_id,
                    assignment_type,
                    weekly_periods,
                    workload_percentage,
                    effective_from,
                    effective_to,
                    approved_at,
                    approved_by,
                    assignment_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    'PRIMARY_TEACHER',
                    5, 100.00,
                    DATE '2026-02-01',
                    DATE '2026-04-30',
                    CURRENT_TIMESTAMP,
                    ?,
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                teachingAssignmentId,
                tenantId,
                "TA-" + shortId(teachingAssignmentId),
                teacherProfileId,
                academicYearId,
                academicTermId,
                campusId,
                classGradeId,
                subjectId,
                UUID.randomUUID(),
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_bell_schedule (
                    id,
                    tenant_id,
                    campus_id,
                    schedule_code,
                    schedule_name,
                    schedule_type,
                    effective_from,
                    effective_to,
                    monday_enabled,
                    tuesday_enabled,
                    wednesday_enabled,
                    thursday_enabled,
                    friday_enabled,
                    saturday_enabled,
                    sunday_enabled,
                    schedule_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    'REGULAR',
                    DATE '2026-02-01',
                    DATE '2026-04-30',
                    TRUE, TRUE, TRUE, TRUE, TRUE,
                    FALSE, FALSE,
                    'ACTIVE', 'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                bellScheduleId,
                tenantId,
                campusId,
                "REG-" + shortId(bellScheduleId),
                "Regular Schedule",
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_bell_period (
                    id,
                    tenant_id,
                    bell_schedule_id,
                    period_code,
                    period_name,
                    sequence_number,
                    period_type,
                    start_time,
                    end_time,
                    instructional_minutes,
                    attendance_required,
                    scheduling_allowed,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?,
                    'P1', 'Period 1', 1,
                    'TEACHING',
                    TIME '08:00',
                    TIME '08:40',
                    40, TRUE, TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                bellPeriodId,
                tenantId,
                bellScheduleId,
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_timetable_generation_request (
                    id,
                    tenant_id,
                    generation_code,
                    academic_year_id,
                    academic_term_id,
                    campus_id,
                    bell_schedule_id,
                    timetable_type,
                    effective_from,
                    effective_to,
                    generation_mode,
                    model_code,
                    objectives,
                    generation_status,
                    requested_by,
                    requested_at,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?,
                    'MASTER',
                    DATE '2026-02-01',
                    DATE '2026-04-30',
                    'RULE_ENGINE',
                    NULL,
                    'A10 rollback verification',
                    'READY',
                    ?,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0,
                    'ACTIVE'
                )
                """,
                requestId,
                tenantId,
                "GEN-A10-" + shortId(requestId),
                academicYearId,
                academicTermId,
                campusId,
                bellScheduleId,
                UUID.randomUUID(),
                auditUser,
                auditUser
        );
    }


    private String shortId(
            UUID value
    ) {

        return value
                .toString()
                .substring(
                        0,
                        8
                );
    }
}
