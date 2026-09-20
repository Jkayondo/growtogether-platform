package africa.growtogether.platform.school.timetable.generation;


import africa.growtogether.platform.school.teacher.programme.TeacherProgrammeLessonRepository;

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
class TeacherProgrammeLessonRepositoryPostgresIntegrationTest {

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


    private static final String PROGRAMME_AUDIT_USER = "teacher-programme-postgres-test";

    @org.springframework.beans.factory.annotation.Autowired
    private TeacherProgrammeLessonRepository programmeRepository;

    @Test
    void isolatesCandidatesByTeacherAndTenant() {

        java.time.LocalDate date =
                java.time.LocalDate.of(
                        2026,
                        3,
                        16
                );

        java.util.UUID timetableId =
                java.util.UUID.randomUUID();

        java.util.UUID entryId =
                java.util.UUID.randomUUID();

        insertProgrammeTimetable(
                timetableId,
                date.minusDays(10),
                date.plusDays(10)
        );

        insertProgrammeEntry(
                entryId,
                timetableId,
                "MONDAY",
                date.minusDays(1),
                date.plusDays(1),
                "LESSON",
                "SCHEDULED",
                "ACTIVE"
        );

        var candidates =
                programmeRepository.findLessonCandidates(
                        tenantId,
                        teacherProfileId,
                        date,
                        "MONDAY",
                        africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                candidates.size()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                teacherProfileId,
                candidates.get(0).getTeacherProfileId()
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                programmeRepository.findLessonCandidates(
                        tenantId,
                        java.util.UUID.randomUUID(),
                        date,
                        "MONDAY",
                        africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
                ).isEmpty()
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                programmeRepository.findLessonCandidates(
                        java.util.UUID.randomUUID(),
                        teacherProfileId,
                        date,
                        "MONDAY",
                        africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
                ).isEmpty()
        );
    }

    @Test
    void filtersLifecycleTypeAndEntityStatus() {

        java.time.LocalDate date =
                java.time.LocalDate.of(
                        2026,
                        3,
                        16
                );

        java.util.UUID timetableId =
                java.util.UUID.randomUUID();

        java.util.UUID entryId =
                java.util.UUID.randomUUID();

        insertProgrammeTimetable(
                timetableId,
                date.minusDays(10),
                date.plusDays(10)
        );

        insertProgrammeEntry(
                entryId,
                timetableId,
                "MONDAY",
                null,
                null,
                "LESSON",
                "SCHEDULED",
                "ACTIVE"
        );

        assertCandidateCount(
                1,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable_entry
                SET entry_status = 'COMPLETED'
                WHERE id = ?
                """,
                entryId
        );

        assertCandidateCount(
                0,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable_entry
                SET entry_status = 'SCHEDULED',
                    entry_type = 'BREAK'
                WHERE id = ?
                """,
                entryId
        );

        assertCandidateCount(
                0,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable_entry
                SET entry_type = 'LESSON',
                    status = 'INACTIVE'
                WHERE id = ?
                """,
                entryId
        );

        assertCandidateCount(
                0,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable_entry
                SET status = 'ACTIVE'
                WHERE id = ?
                """,
                entryId
        );

        jdbc.update(
                """
                UPDATE gts_timetable
                SET timetable_status = 'DRAFT'
                WHERE id = ?
                """,
                timetableId
        );

        assertCandidateCount(
                0,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable
                SET timetable_status = 'PUBLISHED',
                    status = 'INACTIVE'
                WHERE id = ?
                """,
                timetableId
        );

        assertCandidateCount(
                0,
                date
        );
    }

    @Test
    void appliesWeekdayAndInclusiveDateBounds() {

        java.time.LocalDate date =
                java.time.LocalDate.of(
                        2026,
                        3,
                        16
                );

        java.util.UUID timetableId =
                java.util.UUID.randomUUID();

        java.util.UUID entryId =
                java.util.UUID.randomUUID();

        /*
         * Exact equality on both timetable and entry boundaries
         * proves the intended inclusive-date behavior.
         */
        insertProgrammeTimetable(
                timetableId,
                date,
                date
        );

        insertProgrammeEntry(
                entryId,
                timetableId,
                "MONDAY",
                date,
                date,
                "LESSON",
                "SCHEDULED",
                "ACTIVE"
        );

        assertCandidateCount(
                1,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable_entry
                SET day_of_week = 'TUESDAY'
                WHERE id = ?
                """,
                entryId
        );

        assertCandidateCount(
                0,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable_entry
                SET day_of_week = 'MONDAY',
                    effective_from = NULL,
                    effective_to = NULL
                WHERE id = ?
                """,
                entryId
        );

        assertCandidateCount(
                1,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable
                SET effective_from = ?,
                    effective_to = ?
                WHERE id = ?
                """,
                date.plusDays(1),
                date.plusDays(1),
                timetableId
        );

        assertCandidateCount(
                0,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable
                SET effective_from = ?,
                    effective_to = ?
                WHERE id = ?
                """,
                date.minusDays(2),
                date.minusDays(1),
                timetableId
        );

        assertCandidateCount(
                0,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable
                SET effective_from = ?,
                    effective_to = ?
                WHERE id = ?
                """,
                date,
                date,
                timetableId
        );

        jdbc.update(
                """
                UPDATE gts_timetable_entry
                SET effective_from = ?,
                    effective_to = NULL
                WHERE id = ?
                """,
                date.plusDays(1),
                entryId
        );

        assertCandidateCount(
                0,
                date
        );

        jdbc.update(
                """
                UPDATE gts_timetable_entry
                SET effective_from = ?,
                    effective_to = ?
                WHERE id = ?
                """,
                date.minusDays(2),
                date.minusDays(1),
                entryId
        );

        assertCandidateCount(
                0,
                date
        );
    }

    private void assertCandidateCount(
            int expected,
            java.time.LocalDate date
    ) {

        int actual =
                programmeRepository.findLessonCandidates(
                        tenantId,
                        teacherProfileId,
                        date,
                        "MONDAY",
                        africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
                ).size();

        org.junit.jupiter.api.Assertions.assertEquals(
                expected,
                actual
        );
    }

    private void insertProgrammeTimetable(
            java.util.UUID timetableId,
            java.time.LocalDate effectiveFrom,
            java.time.LocalDate effectiveTo
    ) {

        jdbc.update(
                """
                INSERT INTO gts_timetable (
                    id,
                    tenant_id,
                    timetable_code,
                    timetable_name,
                    academic_year_id,
                    academic_term_id,
                    campus_id,
                    bell_schedule_id,
                    timetable_type,
                    version_number,
                    effective_from,
                    effective_to,
                    generated_by,
                    approved_at,
                    approved_by,
                    published_at,
                    published_by,
                    timetable_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    ?, ?, ?, ?,
                    'TEACHER', 99,
                    ?, ?,
                    'MANUAL',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    'PUBLISHED',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                timetableId,
                tenantId,
                "TP-" + shortId(timetableId),
                "Teacher Programme Test",
                academicYearId,
                academicTermId,
                campusId,
                bellScheduleId,
                effectiveFrom,
                effectiveTo,
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                PROGRAMME_AUDIT_USER,
                PROGRAMME_AUDIT_USER
        );
    }

    private void insertProgrammeEntry(
            java.util.UUID entryId,
            java.util.UUID timetableId,
            String dayOfWeek,
            java.time.LocalDate effectiveFrom,
            java.time.LocalDate effectiveTo,
            String entryType,
            String entryStatus,
            String entityStatus
    ) {

        jdbc.update(
                """
                INSERT INTO gts_timetable_entry (
                    id,
                    tenant_id,
                    timetable_id,
                    bell_period_id,
                    day_of_week,
                    class_offering_id,
                    subject_offering_id,
                    class_grade_id,
                    teaching_assignment_id,
                    teacher_profile_id,
                    entry_type,
                    activity_name,
                    recurring,
                    effective_from,
                    effective_to,
                    entry_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?,
                    ?, 'Programme Test Lesson',
                    FALSE,
                    ?, ?,
                    ?, ?,
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                entryId,
                tenantId,
                timetableId,
                bellPeriodId,
                dayOfWeek,
                classOfferingId,
                subjectOfferingId,
                classGradeId,
                teachingAssignmentId,
                teacherProfileId,
                entryType,
                effectiveFrom,
                effectiveTo,
                entryStatus,
                entityStatus,
                PROGRAMME_AUDIT_USER,
                PROGRAMME_AUDIT_USER
        );
    }

}
