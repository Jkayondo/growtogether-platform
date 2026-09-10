package africa.growtogether.platform.school.assessment.marking;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Testcontainers
class MarkSheetTenantIntegrityPostgresIntegrationTest {


    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether"
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

        /*
         * Flyway constructs the real PostgreSQL schema.
         *
         * Hibernate validation is disabled so unrelated worktree
         * entities cannot prevent this focused V263 integrity proof.
         */
        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "none"
        );

        registry.add(
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }


    @Autowired
    private JdbcTemplate jdbc;


    private ParentFixture tenantA;

    private ParentFixture tenantB;


    @BeforeEach
    void setUp() {

        tenantA =
                createParentFixture(
                        "A"
                );

        tenantB =
                createParentFixture(
                        "B"
                );
    }


    @Test
    void sameTenantMarkSheetSucceeds() {

        UUID markSheetId =
                UUID.randomUUID();


        int inserted =
                insertMarkSheet(
                        markSheetId,
                        tenantA.tenantId(),
                        tenantA.componentId(),
                        tenantA.paperId(),
                        tenantA.scheduleId(),
                        tenantA.subjectOfferingId(),
                        tenantA.classOfferingId(),
                        tenantA.streamId(),
                        tenantA.teacherProfileId()
                );


        assertThat(
                inserted
        ).isEqualTo(
                1
        );


        assertThat(
                markSheetCount(
                        markSheetId
                )
        ).isEqualTo(
                1
        );
    }


    @Test
    void rejectsCrossTenantAssessmentComponent() {

        UUID markSheetId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertMarkSheet(
                                markSheetId,
                                tenantA.tenantId(),
                                tenantB.componentId(),
                                tenantA.paperId(),
                                tenantA.scheduleId(),
                                tenantA.subjectOfferingId(),
                                tenantA.classOfferingId(),
                                tenantA.streamId(),
                                tenantA.teacherProfileId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_mark_sheet_component_tenant"
                );


        assertThat(
                markSheetCount(
                        markSheetId
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantAssessmentPaper() {

        UUID markSheetId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertMarkSheet(
                                markSheetId,
                                tenantA.tenantId(),
                                tenantA.componentId(),
                                tenantB.paperId(),
                                tenantA.scheduleId(),
                                tenantA.subjectOfferingId(),
                                tenantA.classOfferingId(),
                                tenantA.streamId(),
                                tenantA.teacherProfileId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_mark_sheet_paper_tenant"
                );


        assertThat(
                markSheetCount(
                        markSheetId
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantExaminationSchedule() {

        UUID markSheetId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertMarkSheet(
                                markSheetId,
                                tenantA.tenantId(),
                                tenantA.componentId(),
                                tenantA.paperId(),
                                tenantB.scheduleId(),
                                tenantA.subjectOfferingId(),
                                tenantA.classOfferingId(),
                                tenantA.streamId(),
                                tenantA.teacherProfileId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_mark_sheet_schedule_tenant"
                );


        assertThat(
                markSheetCount(
                        markSheetId
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantSubjectOffering() {

        UUID markSheetId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertMarkSheet(
                                markSheetId,
                                tenantA.tenantId(),
                                tenantA.componentId(),
                                tenantA.paperId(),
                                tenantA.scheduleId(),
                                tenantB.subjectOfferingId(),
                                tenantA.classOfferingId(),
                                tenantA.streamId(),
                                tenantA.teacherProfileId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_mark_sheet_subject_offering_tenant"
                );


        assertThat(
                markSheetCount(
                        markSheetId
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantClassOffering() {

        UUID markSheetId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertMarkSheet(
                                markSheetId,
                                tenantA.tenantId(),
                                tenantA.componentId(),
                                tenantA.paperId(),
                                tenantA.scheduleId(),
                                tenantA.subjectOfferingId(),
                                tenantB.classOfferingId(),
                                tenantA.streamId(),
                                tenantA.teacherProfileId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_mark_sheet_class_offering_tenant"
                );


        assertThat(
                markSheetCount(
                        markSheetId
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantStream() {

        UUID markSheetId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertMarkSheet(
                                markSheetId,
                                tenantA.tenantId(),
                                tenantA.componentId(),
                                tenantA.paperId(),
                                tenantA.scheduleId(),
                                tenantA.subjectOfferingId(),
                                tenantA.classOfferingId(),
                                tenantB.streamId(),
                                tenantA.teacherProfileId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_mark_sheet_stream_tenant"
                );


        assertThat(
                markSheetCount(
                        markSheetId
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantTeacherProfile() {

        UUID markSheetId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertMarkSheet(
                                markSheetId,
                                tenantA.tenantId(),
                                tenantA.componentId(),
                                tenantA.paperId(),
                                tenantA.scheduleId(),
                                tenantA.subjectOfferingId(),
                                tenantA.classOfferingId(),
                                tenantA.streamId(),
                                tenantB.teacherProfileId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_mark_sheet_teacher_profile_tenant"
                );


        assertThat(
                markSheetCount(
                        markSheetId
                )
        ).isZero();
    }


    private ParentFixture createParentFixture(
            String suffix
    ) {

        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID assessmentPlanId =
                UUID.randomUUID();

        UUID assessmentTypeId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        UUID classOfferingId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        UUID subjectOfferingId =
                UUID.randomUUID();

        UUID streamId =
                UUID.randomUUID();

        UUID workforceMemberId =
                UUID.randomUUID();

        UUID teacherProfileId =
                UUID.randomUUID();

        UUID componentId =
                UUID.randomUUID();

        UUID examinationSessionId =
                UUID.randomUUID();

        UUID paperId =
                UUID.randomUUID();

        UUID scheduleId =
                UUID.randomUUID();


        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (
                    ?, ?, ?,
                    CURRENT_TIMESTAMP
                )
                """,
                organizationId,
                "ORG-MS-" + suffix + "-" + shortId(
                        organizationId
                ),
                "Mark Sheet Integrity Organisation " + suffix
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
                VALUES (
                    ?, ?, ?, ?,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    0
                )
                """,
                tenantId,
                organizationId,
                "TEN-MS-" + suffix + "-" + shortId(
                        tenantId
                ),
                "Mark Sheet Integrity Tenant " + suffix
        );


        /*
         * These records are the immediate parents used by gts_mark_sheet.
         *
         * Their unrelated upstream foreign keys are disabled only while the
         * focused fixture is seeded. V263 and all Mark Sheet constraints are
         * fully enabled again before insertMarkSheet() executes.
         */
        jdbc.execute(
                (ConnectionCallback<Void>) connection -> {

                    try (
                            Statement statement =
                                    connection.createStatement()
                    ) {

                        statement.execute(
                                "SET session_replication_role = replica"
                        );


                        try {

                            statement.executeUpdate(
                                    """
                                    INSERT INTO gts_class_offering (
                                        id,
                                        tenant_id,
                                        offering_code,
                                        academic_year_id,
                                        campus_id,
                                        class_grade_id,
                                        created_at,
                                        created_by,
                                        updated_at,
                                        updated_by
                                    )
                                    VALUES (
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        'MS-CLASS-%s',
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test'
                                    )
                                    """
                                            .formatted(
                                                    classOfferingId,
                                                    tenantId,
                                                    shortId(
                                                            classOfferingId
                                                    ),
                                                    academicYearId,
                                                    campusId,
                                                    classGradeId
                                            )
                            );


                            statement.executeUpdate(
                                    """
                                    INSERT INTO gts_stream (
                                        id,
                                        tenant_id,
                                        campus_id,
                                        class_grade_id,
                                        stream_code,
                                        stream_name,
                                        created_at,
                                        created_by,
                                        updated_at,
                                        updated_by
                                    )
                                    VALUES (
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        'MS-STREAM-%s',
                                        'Mark Sheet Stream %s',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test'
                                    )
                                    """
                                            .formatted(
                                                    streamId,
                                                    tenantId,
                                                    campusId,
                                                    classGradeId,
                                                    shortId(
                                                            streamId
                                                    ),
                                                    suffix
                                            )
                            );


                            statement.executeUpdate(
                                    """
                                    INSERT INTO gts_subject_offering (
                                        id,
                                        tenant_id,
                                        subject_offering_code,
                                        class_offering_id,
                                        stream_id,
                                        subject_id,
                                        created_at,
                                        created_by,
                                        updated_at,
                                        updated_by
                                    )
                                    VALUES (
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        'MS-SUBJECT-%s',
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test'
                                    )
                                    """
                                            .formatted(
                                                    subjectOfferingId,
                                                    tenantId,
                                                    shortId(
                                                            subjectOfferingId
                                                    ),
                                                    classOfferingId,
                                                    streamId,
                                                    subjectId
                                            )
                            );


                            statement.executeUpdate(
                                    """
                                    INSERT INTO gts_teacher_profile (
                                        id,
                                        tenant_id,
                                        workforce_member_id,
                                        teacher_number,
                                        created_at,
                                        created_by,
                                        updated_at,
                                        updated_by
                                    )
                                    VALUES (
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        'MS-TEACHER-%s',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test'
                                    )
                                    """
                                            .formatted(
                                                    teacherProfileId,
                                                    tenantId,
                                                    workforceMemberId,
                                                    shortId(
                                                            teacherProfileId
                                                    )
                                            )
                            );


                            statement.executeUpdate(
                                    """
                                    INSERT INTO gts_assessment_component (
                                        id,
                                        tenant_id,
                                        assessment_plan_id,
                                        component_code,
                                        component_name,
                                        assessment_type_id,
                                        subject_offering_id,
                                        sequence_number,
                                        maximum_score,
                                        weight_percentage,
                                        created_at,
                                        created_by,
                                        updated_at,
                                        updated_by
                                    )
                                    VALUES (
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        'MS-COMP-%s',
                                        'Mark Sheet Component %s',
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        1,
                                        100.00,
                                        100.00,
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test'
                                    )
                                    """
                                            .formatted(
                                                    componentId,
                                                    tenantId,
                                                    assessmentPlanId,
                                                    shortId(
                                                            componentId
                                                    ),
                                                    suffix,
                                                    assessmentTypeId,
                                                    subjectOfferingId
                                            )
                            );


                            statement.executeUpdate(
                                    """
                                    INSERT INTO gts_assessment_paper (
                                        id,
                                        tenant_id,
                                        paper_code,
                                        paper_name,
                                        assessment_component_id,
                                        examination_session_id,
                                        subject_offering_id,
                                        paper_type,
                                        maximum_score,
                                        confidential,
                                        paper_status,
                                        status,
                                        created_at,
                                        created_by,
                                        updated_at,
                                        updated_by,
                                        version
                                    )
                                    VALUES (
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        'MS-PAPER-%s',
                                        'Mark Sheet Integrity Paper %s',
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        'WRITTEN',
                                        100.00,
                                        TRUE,
                                        'DRAFT',
                                        'ACTIVE',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test',
                                        0
                                    )
                                    """
                                            .formatted(
                                                    paperId,
                                                    tenantId,
                                                    shortId(
                                                            paperId
                                                    ),
                                                    suffix,
                                                    componentId,
                                                    examinationSessionId,
                                                    subjectOfferingId
                                            )
                            );


                            statement.executeUpdate(
                                    """
                                    INSERT INTO gts_examination_schedule (
                                        id,
                                        tenant_id,
                                        schedule_reference,
                                        examination_session_id,
                                        assessment_paper_id,
                                        class_offering_id,
                                        stream_id,
                                        examination_date,
                                        start_time,
                                        end_time,
                                        expected_candidate_count,
                                        schedule_status,
                                        status,
                                        created_at,
                                        created_by,
                                        updated_at,
                                        updated_by,
                                        version
                                    )
                                    VALUES (
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        'MS-SCHEDULE-%s',
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        DATE '2026-09-20',
                                        TIME '09:00',
                                        TIME '11:00',
                                        1,
                                        'SCHEDULED',
                                        'ACTIVE',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'mark-sheet-integrity-test',
                                        0
                                    )
                                    """
                                            .formatted(
                                                    scheduleId,
                                                    tenantId,
                                                    shortId(
                                                            scheduleId
                                                    ),
                                                    examinationSessionId,
                                                    paperId,
                                                    classOfferingId,
                                                    streamId
                                            )
                            );


                        } finally {

                            statement.execute(
                                    "SET session_replication_role = origin"
                            );
                        }
                    }


                    return null;
                }
        );


        return new ParentFixture(
                tenantId,
                componentId,
                paperId,
                scheduleId,
                subjectOfferingId,
                classOfferingId,
                streamId,
                teacherProfileId
        );
    }


    private int insertMarkSheet(
            UUID markSheetId,
            UUID tenantId,
            UUID componentId,
            UUID paperId,
            UUID scheduleId,
            UUID subjectOfferingId,
            UUID classOfferingId,
            UUID streamId,
            UUID teacherProfileId
    ) {

        String auditUser =
                "mark-sheet-integrity-test";


        return jdbc.update(
                """
                INSERT INTO gts_mark_sheet (
                    id,
                    tenant_id,
                    mark_sheet_reference,
                    assessment_component_id,
                    assessment_paper_id,
                    examination_schedule_id,
                    subject_offering_id,
                    class_offering_id,
                    stream_id,
                    teacher_profile_id,
                    maximum_score,
                    mark_sheet_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    100.00,
                    'DRAFT',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                markSheetId,
                tenantId,
                "MS-PG-" + shortId(
                        markSheetId
                ),
                componentId,
                paperId,
                scheduleId,
                subjectOfferingId,
                classOfferingId,
                streamId,
                teacherProfileId,
                auditUser,
                auditUser
        );
    }


    private int markSheetCount(
            UUID markSheetId
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_mark_sheet
                        WHERE id = ?
                        """,
                        Integer.class,
                        markSheetId
                );


        return count == null
                ? 0
                : count;
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


    private record ParentFixture(
            UUID tenantId,
            UUID componentId,
            UUID paperId,
            UUID scheduleId,
            UUID subjectOfferingId,
            UUID classOfferingId,
            UUID streamId,
            UUID teacherProfileId
    ) {
    }
}
