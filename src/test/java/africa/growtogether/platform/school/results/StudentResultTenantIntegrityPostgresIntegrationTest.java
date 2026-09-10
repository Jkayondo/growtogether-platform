package africa.growtogether.platform.school.results;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Testcontainers
class StudentResultTenantIntegrityPostgresIntegrationTest {


    private static final String ACTOR =
            "student-result-tenant-integrity-test";


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


    private Fixture tenantA;

    private Fixture tenantB;


    @BeforeEach
    void setUp() {

        tenantA =
                createFixture(
                        "A"
                );

        tenantB =
                createFixture(
                        "B"
                );
    }


    @Test
    void freshFlywayAppliesV266AndAllControlledConstraints() {

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '266'
                          AND success = TRUE
                        """,
                        Integer.class
                );


        assertThat(
                migrationCount
        )
                .isEqualTo(
                        1
                );


        Integer constraintCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM pg_constraint
                        WHERE conname IN (
                            'uq_gts_grade_calculation_run_tenant_id',
                            'uq_gts_student_grade_outcome_tenant_id',
                            'uq_gts_assessment_plan_tenant_id',

                            'fk_gts_student_subject_result_student_tenant',
                            'fk_gts_student_subject_result_student_enrollment_tenant',
                            'fk_gts_student_subject_result_academic_year_tenant',
                            'fk_gts_student_subject_result_academic_term_tenant',
                            'fk_gts_student_subject_result_subject_offering_tenant',
                            'fk_gts_student_subject_result_grade_calculation_run_tenant',
                            'fk_gts_student_subject_result_grade_outcome_tenant',

                            'fk_gts_student_term_result_student_tenant',
                            'fk_gts_student_term_result_student_enrollment_tenant',
                            'fk_gts_student_term_result_academic_year_tenant',
                            'fk_gts_student_term_result_academic_term_tenant',
                            'fk_gts_student_term_result_class_grade_tenant',
                            'fk_gts_student_term_result_stream_tenant',
                            'fk_gts_student_term_result_assessment_plan_tenant'
                        )
                        """,
                        Integer.class
                );


        assertThat(
                constraintCount
        )
                .isEqualTo(
                        17
                );
    }


    @Test
    void sameTenantSubjectAndTermResultsSucceed() {

        UUID subjectResultId =
                UUID.randomUUID();

        UUID termResultId =
                UUID.randomUUID();


        assertThat(
                insertSubjectResult(
                        subjectResultId,
                        tenantA.tenantId(),
                        tenantA.studentId(),
                        tenantA.enrollmentId(),
                        tenantA.academicYearId(),
                        tenantA.academicTermId(),
                        tenantA.subjectOfferingId(),
                        tenantA.gradeCalculationRunId(),
                        tenantA.gradeOutcomeId()
                )
        )
                .isEqualTo(
                        1
                );


        assertThat(
                insertTermResult(
                        termResultId,
                        tenantA.tenantId(),
                        tenantA.studentId(),
                        tenantA.enrollmentId(),
                        tenantA.academicYearId(),
                        tenantA.academicTermId(),
                        tenantA.classGradeId(),
                        tenantA.streamId(),
                        tenantA.assessmentPlanId()
                )
        )
                .isEqualTo(
                        1
                );


        assertThat(
                rowCount(
                        "gts_student_subject_result",
                        subjectResultId
                )
        )
                .isEqualTo(
                        1
                );


        assertThat(
                rowCount(
                        "gts_student_term_result",
                        termResultId
                )
        )
                .isEqualTo(
                        1
                );
    }


    @ParameterizedTest(name = "subject result rejects cross-tenant {0}")
    @MethodSource("subjectRelationships")
    void rejectsCrossTenantSubjectResult(
            String relationship,
            String constraintName
    ) {

        UUID resultId =
                UUID.randomUUID();


        assertRejected(
                "gts_student_subject_result",
                resultId,
                constraintName,
                () ->
                        insertSubjectResult(
                                resultId,
                                tenantA.tenantId(),

                                "student".equals(relationship)
                                        ? tenantB.studentId()
                                        : tenantA.studentId(),

                                "student_enrollment".equals(relationship)
                                        ? tenantB.enrollmentId()
                                        : tenantA.enrollmentId(),

                                "academic_year".equals(relationship)
                                        ? tenantB.academicYearId()
                                        : tenantA.academicYearId(),

                                "academic_term".equals(relationship)
                                        ? tenantB.academicTermId()
                                        : tenantA.academicTermId(),

                                "subject_offering".equals(relationship)
                                        ? tenantB.subjectOfferingId()
                                        : tenantA.subjectOfferingId(),

                                "grade_calculation_run".equals(relationship)
                                        ? tenantB.gradeCalculationRunId()
                                        : tenantA.gradeCalculationRunId(),

                                "grade_outcome".equals(relationship)
                                        ? tenantB.gradeOutcomeId()
                                        : tenantA.gradeOutcomeId()
                        )
        );
    }


    static Stream<Arguments> subjectRelationships() {

        return Stream.of(
                Arguments.of(
                        "student",
                        "fk_gts_student_subject_result_student_tenant"
                ),
                Arguments.of(
                        "student_enrollment",
                        "fk_gts_student_subject_result_student_enrollment_tenant"
                ),
                Arguments.of(
                        "academic_year",
                        "fk_gts_student_subject_result_academic_year_tenant"
                ),
                Arguments.of(
                        "academic_term",
                        "fk_gts_student_subject_result_academic_term_tenant"
                ),
                Arguments.of(
                        "subject_offering",
                        "fk_gts_student_subject_result_subject_offering_tenant"
                ),
                Arguments.of(
                        "grade_calculation_run",
                        "fk_gts_student_subject_result_grade_calculation_run_tenant"
                ),
                Arguments.of(
                        "grade_outcome",
                        "fk_gts_student_subject_result_grade_outcome_tenant"
                )
        );
    }


    @ParameterizedTest(name = "term result rejects cross-tenant {0}")
    @MethodSource("termRelationships")
    void rejectsCrossTenantTermResult(
            String relationship,
            String constraintName
    ) {

        UUID resultId =
                UUID.randomUUID();


        assertRejected(
                "gts_student_term_result",
                resultId,
                constraintName,
                () ->
                        insertTermResult(
                                resultId,
                                tenantA.tenantId(),

                                "student".equals(relationship)
                                        ? tenantB.studentId()
                                        : tenantA.studentId(),

                                "student_enrollment".equals(relationship)
                                        ? tenantB.enrollmentId()
                                        : tenantA.enrollmentId(),

                                "academic_year".equals(relationship)
                                        ? tenantB.academicYearId()
                                        : tenantA.academicYearId(),

                                "academic_term".equals(relationship)
                                        ? tenantB.academicTermId()
                                        : tenantA.academicTermId(),

                                "class_grade".equals(relationship)
                                        ? tenantB.classGradeId()
                                        : tenantA.classGradeId(),

                                "stream".equals(relationship)
                                        ? tenantB.streamId()
                                        : tenantA.streamId(),

                                "assessment_plan".equals(relationship)
                                        ? tenantB.assessmentPlanId()
                                        : tenantA.assessmentPlanId()
                        )
        );
    }


    static Stream<Arguments> termRelationships() {

        return Stream.of(
                Arguments.of(
                        "student",
                        "fk_gts_student_term_result_student_tenant"
                ),
                Arguments.of(
                        "student_enrollment",
                        "fk_gts_student_term_result_student_enrollment_tenant"
                ),
                Arguments.of(
                        "academic_year",
                        "fk_gts_student_term_result_academic_year_tenant"
                ),
                Arguments.of(
                        "academic_term",
                        "fk_gts_student_term_result_academic_term_tenant"
                ),
                Arguments.of(
                        "class_grade",
                        "fk_gts_student_term_result_class_grade_tenant"
                ),
                Arguments.of(
                        "stream",
                        "fk_gts_student_term_result_stream_tenant"
                ),
                Arguments.of(
                        "assessment_plan",
                        "fk_gts_student_term_result_assessment_plan_tenant"
                )
        );
    }


    private void assertRejected(
            String tableName,
            UUID id,
            String constraintName,
            ThrowingAction action
    ) {

        assertThatThrownBy(
                action::run
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        constraintName
                );


        assertThat(
                rowCount(
                        tableName,
                        id
                )
        )
                .isZero();
    }


    private Fixture createFixture(
            String suffix
    ) {

        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID enrollmentId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID academicTermId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        UUID streamId =
                UUID.randomUUID();

        UUID assessmentPlanId =
                UUID.randomUUID();

        UUID subjectOfferingId =
                UUID.randomUUID();

        UUID gradeCalculationRunId =
                UUID.randomUUID();

        UUID gradeOutcomeId =
                UUID.randomUUID();


        UUID campusId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        UUID classOfferingId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        UUID gradingSchemeId =
                UUID.randomUUID();


        /*
         * Fixture construction bypasses unrelated upstream relationships only.
         * PostgreSQL enforcement is restored before any V266 relationship is
         * exercised by the actual result inserts.
         */
        jdbc.execute(
                (ConnectionCallback<Void>)
                        connection -> {

                            try (
                                    Statement statement =
                                            connection.createStatement()
                            ) {

                                statement.execute(
                                        "SET session_replication_role = replica"
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO eiam_organization (
                                            id,
                                            code,
                                            name,
                                            created_at
                                        )
                                        VALUES (
                                            '%s'::uuid,
                                            'V266-ORG-%s',
                                            'V266 Result Integrity Org %s',
                                            CURRENT_TIMESTAMP
                                        )
                                        """
                                                .formatted(
                                                        organizationId,
                                                        shortId(
                                                                organizationId
                                                        ),
                                                        suffix
                                                )
                                );


                                statement.executeUpdate(
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
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            'V266-TENANT-%s',
                                            'V266 Result Integrity Tenant %s',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            0
                                        )
                                        """
                                                .formatted(
                                                        tenantId,
                                                        organizationId,
                                                        shortId(
                                                                tenantId
                                                        ),
                                                        suffix
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_student (
                                            id,
                                            tenant_id,
                                            student_number,
                                            permanent_learner_number,
                                            first_name,
                                            last_name,
                                            date_of_birth,
                                            student_status,
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
                                            'V266-STUDENT-%s',
                                            'V266-PLN-%s',
                                            'Result',
                                            'Student-%s',
                                            DATE '2015-01-01',
                                            'ACTIVE',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        studentId,
                                                        tenantId,
                                                        shortId(
                                                                studentId
                                                        ),
                                                        shortId(
                                                                studentId
                                                        ),
                                                        suffix,
                                                        ACTOR,
                                                        ACTOR
                                                )
                                );


                                statement.executeUpdate(
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
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            'V266-YEAR-%s',
                                            'V266 Academic Year %s',
                                            DATE '2026-01-01',
                                            DATE '2026-12-31',
                                            FALSE,
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        academicYearId,
                                                        tenantId,
                                                        shortId(
                                                                academicYearId
                                                        ),
                                                        suffix,
                                                        ACTOR,
                                                        ACTOR
                                                )
                                );


                                statement.executeUpdate(
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
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            'V266-TERM-%s',
                                            'V266 Term %s',
                                            1,
                                            DATE '2026-01-01',
                                            DATE '2026-04-30',
                                            FALSE,
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        academicTermId,
                                                        tenantId,
                                                        academicYearId,
                                                        shortId(
                                                                academicTermId
                                                        ),
                                                        suffix,
                                                        ACTOR,
                                                        ACTOR
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_class_grade (
                                            id,
                                            tenant_id,
                                            education_level_id,
                                            class_code,
                                            class_name,
                                            sequence_number,
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
                                            '%s'::uuid,
                                            'V266-CLASS-%s',
                                            'V266 Class %s',
                                            1,
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        classGradeId,
                                                        tenantId,
                                                        educationLevelId,
                                                        shortId(
                                                                classGradeId
                                                        ),
                                                        suffix,
                                                        ACTOR,
                                                        ACTOR
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_student_enrollment (
                                            id,
                                            tenant_id,
                                            student_id,
                                            academic_year_id,
                                            campus_id,
                                            class_grade_id,
                                            enrollment_number,
                                            enrollment_date,
                                            effective_from,
                                            enrollment_type,
                                            enrollment_status,
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
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            'V266-ENROLL-%s',
                                            DATE '2026-01-01',
                                            DATE '2026-01-01',
                                            'NEW',
                                            'ACTIVE',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        enrollmentId,
                                                        tenantId,
                                                        studentId,
                                                        academicYearId,
                                                        campusId,
                                                        classGradeId,
                                                        shortId(
                                                                enrollmentId
                                                        ),
                                                        ACTOR,
                                                        ACTOR
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
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            'V266-STREAM-%s',
                                            'V266 Stream %s',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
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
                                                        suffix,
                                                        ACTOR,
                                                        ACTOR
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_assessment_plan (
                                            id,
                                            tenant_id,
                                            plan_code,
                                            plan_name,
                                            academic_year_id,
                                            academic_term_id,
                                            campus_id,
                                            class_grade_id,
                                            stream_id,
                                            effective_from,
                                            plan_status,
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
                                            'V266-PLAN-%s',
                                            'V266 Assessment Plan %s',
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            DATE '2026-01-01',
                                            'DRAFT',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        assessmentPlanId,
                                                        tenantId,
                                                        shortId(
                                                                assessmentPlanId
                                                        ),
                                                        suffix,
                                                        academicYearId,
                                                        academicTermId,
                                                        campusId,
                                                        classGradeId,
                                                        streamId,
                                                        ACTOR,
                                                        ACTOR
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_subject_offering (
                                            id,
                                            tenant_id,
                                            subject_offering_code,
                                            class_offering_id,
                                            academic_term_id,
                                            stream_id,
                                            subject_id,
                                            grading_scheme_id,
                                            offering_status,
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
                                            'V266-SUBJECT-%s',
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            'PLANNED',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        subjectOfferingId,
                                                        tenantId,
                                                        shortId(
                                                                subjectOfferingId
                                                        ),
                                                        classOfferingId,
                                                        academicTermId,
                                                        streamId,
                                                        subjectId,
                                                        gradingSchemeId,
                                                        ACTOR,
                                                        ACTOR
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_grade_calculation_run (
                                            id,
                                            tenant_id,
                                            calculation_reference,
                                            assessment_plan_id,
                                            grading_scheme_id,
                                            class_offering_id,
                                            stream_id,
                                            calculation_type,
                                            calculation_status,
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
                                            'V266-CALC-%s',
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            'STANDARD',
                                            'PENDING',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        gradeCalculationRunId,
                                                        tenantId,
                                                        shortId(
                                                                gradeCalculationRunId
                                                        ),
                                                        assessmentPlanId,
                                                        gradingSchemeId,
                                                        classOfferingId,
                                                        streamId,
                                                        ACTOR,
                                                        ACTOR
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_student_grade_outcome (
                                            id,
                                            tenant_id,
                                            grade_calculation_run_id,
                                            student_id,
                                            student_enrollment_id,
                                            subject_offering_id,
                                            weighted_score,
                                            final_score,
                                            grade_code,
                                            grade_name,
                                            grade_point,
                                            passed,
                                            outcome_status,
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
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            80,
                                            80,
                                            'A',
                                            'Excellent',
                                            1,
                                            TRUE,
                                            'CALCULATED',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            CURRENT_TIMESTAMP,
                                            '%s',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        gradeOutcomeId,
                                                        tenantId,
                                                        gradeCalculationRunId,
                                                        studentId,
                                                        enrollmentId,
                                                        subjectOfferingId,
                                                        ACTOR,
                                                        ACTOR
                                                )
                                );


                                statement.execute(
                                        "SET session_replication_role = origin"
                                );

                            } catch (Exception exception) {

                                try (
                                        Statement restore =
                                                connection.createStatement()
                                ) {

                                    restore.execute(
                                            "SET session_replication_role = origin"
                                    );

                                } catch (Exception ignored) {
                                }

                                throw exception;
                            }

                            return null;
                        }
        );


        return new Fixture(
                tenantId,
                studentId,
                enrollmentId,
                academicYearId,
                academicTermId,
                classGradeId,
                streamId,
                assessmentPlanId,
                subjectOfferingId,
                gradeCalculationRunId,
                gradeOutcomeId
        );
    }


    private int insertSubjectResult(
            UUID id,
            UUID tenantId,
            UUID studentId,
            UUID enrollmentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID subjectOfferingId,
            UUID gradeCalculationRunId,
            UUID gradeOutcomeId
    ) {

        return jdbc.update(
                """
                INSERT INTO gts_student_subject_result (
                    id,
                    tenant_id,
                    result_reference,
                    student_id,
                    student_enrollment_id,
                    academic_year_id,
                    academic_term_id,
                    subject_offering_id,
                    grade_calculation_run_id,
                    grade_outcome_id,
                    final_score,
                    grade_code,
                    grade_name,
                    grade_point,
                    result_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    80,
                    'A',
                    'Excellent',
                    1,
                    'DRAFT',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                id,
                tenantId,
                "V266-SUBJECT-RESULT-" + shortId(
                        id
                ),
                studentId,
                enrollmentId,
                academicYearId,
                academicTermId,
                subjectOfferingId,
                gradeCalculationRunId,
                gradeOutcomeId,
                ACTOR,
                ACTOR
        );
    }


    private int insertTermResult(
            UUID id,
            UUID tenantId,
            UUID studentId,
            UUID enrollmentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId,
            UUID streamId,
            UUID assessmentPlanId
    ) {

        return jdbc.update(
                """
                INSERT INTO gts_student_term_result (
                    id,
                    tenant_id,
                    result_reference,
                    student_id,
                    student_enrollment_id,
                    academic_year_id,
                    academic_term_id,
                    class_grade_id,
                    stream_id,
                    assessment_plan_id,
                    total_subjects,
                    subjects_passed,
                    subjects_failed,
                    average_score,
                    overall_grade_code,
                    overall_grade_name,
                    result_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    1,
                    1,
                    0,
                    80,
                    'A',
                    'Excellent',
                    'DRAFT',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                id,
                tenantId,
                "V266-TERM-RESULT-" + shortId(
                        id
                ),
                studentId,
                enrollmentId,
                academicYearId,
                academicTermId,
                classGradeId,
                streamId,
                assessmentPlanId,
                ACTOR,
                ACTOR
        );
    }


    private int rowCount(
            String tableName,
            UUID id
    ) {

        Integer count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM "
                                + tableName
                                + " WHERE id = ?",
                        Integer.class,
                        id
                );


        return count == null
                ? 0
                : count;
    }


    private static String shortId(
            UUID id
    ) {

        return id.toString()
                .substring(
                        0,
                        8
                );
    }


    @FunctionalInterface
    private interface ThrowingAction {

        void run()
                throws Exception;
    }


    private record Fixture(
            UUID tenantId,
            UUID studentId,
            UUID enrollmentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId,
            UUID streamId,
            UUID assessmentPlanId,
            UUID subjectOfferingId,
            UUID gradeCalculationRunId,
            UUID gradeOutcomeId
    ) {
    }
}
