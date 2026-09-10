package africa.growtogether.platform.school.grading;

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
class GradingResultTenantIntegrityPostgresIntegrationTest {


    private static final String ACTOR =
            "grading-result-tenant-integrity-test";


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
    void sameTenantRelationshipsSucceed() {

        UUID boundaryId =
                UUID.randomUUID();

        UUID aggregationRuleId =
                UUID.randomUUID();

        UUID divisionRuleId =
                UUID.randomUUID();

        UUID resultId =
                UUID.randomUUID();

        UUID publicationId =
                UUID.randomUUID();


        assertThat(
                insertGradeBoundary(
                        boundaryId,
                        tenantA.tenantId(),
                        tenantA.gradingSchemeId()
                )
        )
                .isEqualTo(
                        1
                );


        assertThat(
                insertAggregationRule(
                        aggregationRuleId,
                        tenantA.tenantId(),
                        tenantA.gradingSchemeId()
                )
        )
                .isEqualTo(
                        1
                );


        assertThat(
                insertDivisionRule(
                        divisionRuleId,
                        tenantA.tenantId(),
                        tenantA.gradingSchemeId()
                )
        )
                .isEqualTo(
                        1
                );


        assertThat(
                insertAcademicResult(
                        resultId,
                        tenantA.tenantId(),
                        tenantA.studentId()
                )
        )
                .isEqualTo(
                        1
                );


        assertThat(
                insertResultPublication(
                        publicationId,
                        tenantA.tenantId(),
                        tenantA.academicYearId(),
                        tenantA.academicTermId(),
                        tenantA.classGradeId()
                )
        )
                .isEqualTo(
                        1
                );
    }


    @Test
    void rejectsCrossTenantGradeBoundaryGradingScheme() {

        UUID id =
                UUID.randomUUID();

        assertRejected(
                "gts_grade_boundary",
                id,
                "fk_gts_grade_boundary_grading_scheme_tenant",
                () ->
                        insertGradeBoundary(
                                id,
                                tenantA.tenantId(),
                                tenantB.gradingSchemeId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantAggregationRuleGradingScheme() {

        UUID id =
                UUID.randomUUID();

        assertRejected(
                "gts_grade_aggregation_rule",
                id,
                "fk_gts_grade_aggregation_rule_grading_scheme_tenant",
                () ->
                        insertAggregationRule(
                                id,
                                tenantA.tenantId(),
                                tenantB.gradingSchemeId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantDivisionRuleGradingScheme() {

        UUID id =
                UUID.randomUUID();

        assertRejected(
                "gts_grade_division_rule",
                id,
                "fk_gts_grade_division_rule_grading_scheme_tenant",
                () ->
                        insertDivisionRule(
                                id,
                                tenantA.tenantId(),
                                tenantB.gradingSchemeId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantAcademicResultStudent() {

        UUID id =
                UUID.randomUUID();

        assertRejected(
                "academic_result_record",
                id,
                "fk_academic_result_record_student_tenant",
                () ->
                        insertAcademicResult(
                                id,
                                tenantA.tenantId(),
                                tenantB.studentId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantResultPublicationAcademicYear() {

        UUID id =
                UUID.randomUUID();

        assertRejected(
                "gts_result_publication",
                id,
                "fk_gts_result_publication_academic_year_tenant",
                () ->
                        insertResultPublication(
                                id,
                                tenantA.tenantId(),
                                tenantB.academicYearId(),
                                tenantA.academicTermId(),
                                tenantA.classGradeId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantResultPublicationAcademicTerm() {

        UUID id =
                UUID.randomUUID();

        assertRejected(
                "gts_result_publication",
                id,
                "fk_gts_result_publication_academic_term_tenant",
                () ->
                        insertResultPublication(
                                id,
                                tenantA.tenantId(),
                                tenantA.academicYearId(),
                                tenantB.academicTermId(),
                                tenantA.classGradeId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantResultPublicationClassGrade() {

        UUID id =
                UUID.randomUUID();

        assertRejected(
                "gts_result_publication",
                id,
                "fk_gts_result_publication_class_grade_tenant",
                () ->
                        insertResultPublication(
                                id,
                                tenantA.tenantId(),
                                tenantA.academicYearId(),
                                tenantA.academicTermId(),
                                tenantB.classGradeId()
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

        UUID gradingSchemeId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID academicTermId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();


        /*
         * Fixture creation deliberately bypasses unrelated upstream
         * relationships only. Enforcement is restored before any V265
         * relationship under test is exercised.
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
                                            'V265-ORG-%s',
                                            'V265 Integrity Organisation %s',
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
                                            'V265-TENANT-%s',
                                            'V265 Integrity Tenant %s',
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
                                        INSERT INTO gts_grading_scheme (
                                            id,
                                            tenant_id,
                                            scheme_code,
                                            scheme_name,
                                            grade_scale_type,
                                            minimum_score,
                                            maximum_score,
                                            pass_score,
                                            decimal_places,
                                            rounding_method,
                                            scheme_status,
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
                                            'V265-SCHEME-%s',
                                            'V265 Integrity Scheme %s',
                                            'PERCENTAGE',
                                            0,
                                            100,
                                            50,
                                            2,
                                            'HALF_UP',
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
                                                        gradingSchemeId,
                                                        tenantId,
                                                        shortId(
                                                                gradingSchemeId
                                                        ),
                                                        suffix,
                                                        ACTOR,
                                                        ACTOR
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
                                            'V265-STUDENT-%s',
                                            'V265-PLN-%s',
                                            'V265',
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
                                            'V265-YEAR-%s',
                                            'V265 Academic Year %s',
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
                                            'V265-TERM-%s',
                                            'V265 Term %s',
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
                                            'V265-CLASS-%s',
                                            'V265 Class %s',
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
                gradingSchemeId,
                studentId,
                academicYearId,
                academicTermId,
                classGradeId
        );
    }


    private int insertGradeBoundary(
            UUID id,
            UUID tenantId,
            UUID gradingSchemeId
    ) {

        return jdbc.update(
                """
                INSERT INTO gts_grade_boundary (
                    id,
                    tenant_id,
                    grading_scheme_id,
                    grade_code,
                    grade_name,
                    minimum_score,
                    maximum_score,
                    grade_point,
                    pass_grade,
                    distinction_grade,
                    sequence_number,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?,
                    ?,
                    'V265 Grade',
                    0,
                    100,
                    1,
                    TRUE,
                    FALSE,
                    1,
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
                gradingSchemeId,
                "G-" + shortId(
                        id
                ),
                ACTOR,
                ACTOR
        );
    }


    private int insertAggregationRule(
            UUID id,
            UUID tenantId,
            UUID gradingSchemeId
    ) {

        return jdbc.update(
                """
                INSERT INTO gts_grade_aggregation_rule (
                    id,
                    tenant_id,
                    grading_scheme_id,
                    rule_code,
                    rule_name,
                    aggregation_type,
                    required_subject_count,
                    best_subject_count,
                    include_compulsory_subjects,
                    uses_grade_points,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?,
                    ?,
                    'V265 Aggregation Rule',
                    'SUBJECT_POINT_SUM',
                    1,
                    1,
                    FALSE,
                    TRUE,
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
                gradingSchemeId,
                "AR-" + shortId(
                        id
                ),
                ACTOR,
                ACTOR
        );
    }


    private int insertDivisionRule(
            UUID id,
            UUID tenantId,
            UUID gradingSchemeId
    ) {

        return jdbc.update(
                """
                INSERT INTO gts_grade_division_rule (
                    id,
                    tenant_id,
                    grading_scheme_id,
                    division_code,
                    division_name,
                    minimum_aggregate,
                    maximum_aggregate,
                    grade_point,
                    sequence_number,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?,
                    ?,
                    'V265 Division Rule',
                    1,
                    100,
                    1,
                    1,
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
                gradingSchemeId,
                "D-" + shortId(
                        id
                ),
                ACTOR,
                ACTOR
        );
    }


    private int insertAcademicResult(
            UUID id,
            UUID tenantId,
            UUID studentId
    ) {

        return jdbc.update(
                """
                INSERT INTO academic_result_record (
                    id,
                    tenant_id,
                    student_id,
                    subject_name,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?,
                    'Mathematics',
                    'CALCULATED',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                id,
                tenantId,
                studentId,
                ACTOR,
                ACTOR
        );
    }


    private int insertResultPublication(
            UUID id,
            UUID tenantId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId
    ) {

        return jdbc.update(
                """
                INSERT INTO gts_result_publication (
                    id,
                    tenant_id,
                    academic_year_id,
                    academic_term_id,
                    class_grade_id,
                    publication_type,
                    publication_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    'TERM_RESULT',
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
                academicYearId,
                academicTermId,
                classGradeId,
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
            UUID gradingSchemeId,
            UUID studentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId
    ) {
    }
}
