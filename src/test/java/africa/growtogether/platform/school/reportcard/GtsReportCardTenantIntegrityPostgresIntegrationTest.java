package africa.growtogether.platform.school.reportcard;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class GtsReportCardTenantIntegrityPostgresIntegrationTest {

    private static final String ACTOR =
            "v267-report-card-tenant-integrity-test";

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("growtogether")
                    .withUsername("growtogether")
                    .withPassword("growtogether");

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
        tenantA = createFixture("A");
        tenantB = createFixture("B");
    }

    @Test
    void freshFlywayAppliesV267AndControlledConstraints() {

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '267'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(migrationCount)
                .isEqualTo(1);

        Integer constraintCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM pg_constraint
                        WHERE conname IN (
                            'uq_gts_report_card_template_tenant_id',
                            'uq_gts_student_term_result_tenant_id',
                            'fk_gts_report_card_tenant_template',
                            'fk_gts_report_card_tenant_student',
                            'fk_gts_report_card_tenant_enrollment',
                            'fk_gts_report_card_tenant_term_result',
                            'fk_gts_report_card_tenant_academic_year',
                            'fk_gts_report_card_tenant_academic_term',
                            'fk_gts_report_card_tenant_class_grade',
                            'fk_gts_report_card_tenant_stream'
                        )
                        """,
                        Integer.class
                );

        assertThat(constraintCount)
                .isEqualTo(10);
    }

    @Test
    void sameTenantReportCardSucceeds() {

        UUID reportCardId = UUID.randomUUID();

        assertThat(
                insertReportCard(
                        reportCardId,
                        tenantA.tenantId(),
                        refs(tenantA)
                )
        )
                .isEqualTo(1);

        assertThat(rowCount(reportCardId))
                .isEqualTo(1);
    }

    @ParameterizedTest(
            name = "report card rejects cross-tenant {0}"
    )
    @MethodSource("relationships")
    void rejectsCrossTenantParent(
            String relationship,
            String constraintName
    ) {

        UUID reportCardId = UUID.randomUUID();

        ReportCardRefs mixed =
                crossTenantRefs(
                        tenantA,
                        tenantB,
                        relationship
                );

        assertThatThrownBy(
                () ->
                        insertReportCard(
                                reportCardId,
                                tenantA.tenantId(),
                                mixed
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        constraintName
                );

        assertThat(rowCount(reportCardId))
                .isZero();
    }

    static Stream<Arguments> relationships() {
        return Stream.of(
                Arguments.of(
                        "template",
                        "fk_gts_report_card_tenant_template"
                ),
                Arguments.of(
                        "student",
                        "fk_gts_report_card_tenant_student"
                ),
                Arguments.of(
                        "enrollment",
                        "fk_gts_report_card_tenant_enrollment"
                ),
                Arguments.of(
                        "term_result",
                        "fk_gts_report_card_tenant_term_result"
                ),
                Arguments.of(
                        "academic_year",
                        "fk_gts_report_card_tenant_academic_year"
                ),
                Arguments.of(
                        "academic_term",
                        "fk_gts_report_card_tenant_academic_term"
                ),
                Arguments.of(
                        "class_grade",
                        "fk_gts_report_card_tenant_class_grade"
                ),
                Arguments.of(
                        "stream",
                        "fk_gts_report_card_tenant_stream"
                )
        );
    }

    private ReportCardRefs refs(
            Fixture fixture
    ) {
        return new ReportCardRefs(
                fixture.templateId(),
                fixture.studentId(),
                fixture.enrollmentId(),
                fixture.termResultId(),
                fixture.academicYearId(),
                fixture.academicTermId(),
                fixture.classGradeId(),
                fixture.streamId()
        );
    }

    private ReportCardRefs crossTenantRefs(
            Fixture a,
            Fixture b,
            String relationship
    ) {

        ReportCardRefs ar = refs(a);
        ReportCardRefs br = refs(b);

        return switch (relationship) {
            case "template" ->
                    new ReportCardRefs(
                            br.templateId(),
                            ar.studentId(),
                            ar.enrollmentId(),
                            ar.termResultId(),
                            ar.academicYearId(),
                            ar.academicTermId(),
                            ar.classGradeId(),
                            ar.streamId()
                    );

            case "student" ->
                    new ReportCardRefs(
                            ar.templateId(),
                            br.studentId(),
                            ar.enrollmentId(),
                            ar.termResultId(),
                            ar.academicYearId(),
                            ar.academicTermId(),
                            ar.classGradeId(),
                            ar.streamId()
                    );

            case "enrollment" ->
                    new ReportCardRefs(
                            ar.templateId(),
                            ar.studentId(),
                            br.enrollmentId(),
                            ar.termResultId(),
                            ar.academicYearId(),
                            ar.academicTermId(),
                            ar.classGradeId(),
                            ar.streamId()
                    );

            case "term_result" ->
                    new ReportCardRefs(
                            ar.templateId(),
                            ar.studentId(),
                            ar.enrollmentId(),
                            br.termResultId(),
                            ar.academicYearId(),
                            ar.academicTermId(),
                            ar.classGradeId(),
                            ar.streamId()
                    );

            case "academic_year" ->
                    new ReportCardRefs(
                            ar.templateId(),
                            ar.studentId(),
                            ar.enrollmentId(),
                            ar.termResultId(),
                            br.academicYearId(),
                            ar.academicTermId(),
                            ar.classGradeId(),
                            ar.streamId()
                    );

            case "academic_term" ->
                    new ReportCardRefs(
                            ar.templateId(),
                            ar.studentId(),
                            ar.enrollmentId(),
                            ar.termResultId(),
                            ar.academicYearId(),
                            br.academicTermId(),
                            ar.classGradeId(),
                            ar.streamId()
                    );

            case "class_grade" ->
                    new ReportCardRefs(
                            ar.templateId(),
                            ar.studentId(),
                            ar.enrollmentId(),
                            ar.termResultId(),
                            ar.academicYearId(),
                            ar.academicTermId(),
                            br.classGradeId(),
                            ar.streamId()
                    );

            case "stream" ->
                    new ReportCardRefs(
                            ar.templateId(),
                            ar.studentId(),
                            ar.enrollmentId(),
                            ar.termResultId(),
                            ar.academicYearId(),
                            ar.academicTermId(),
                            ar.classGradeId(),
                            br.streamId()
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown relationship: "
                                    + relationship
                    );
        };
    }

    private int insertReportCard(
            UUID reportCardId,
            UUID tenantId,
            ReportCardRefs refs
    ) {

        return jdbc.update(
                """
                INSERT INTO gts_report_card (
                    id,
                    tenant_id,
                    report_card_reference,
                    report_card_template_id,
                    student_id,
                    student_enrollment_id,
                    student_term_result_id,
                    academic_year_id,
                    academic_term_id,
                    class_grade_id,
                    stream_id,
                    report_title,
                    report_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?,
                    'DRAFT',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                reportCardId,
                tenantId,
                "V267-RC-" + token(reportCardId),
                refs.templateId(),
                refs.studentId(),
                refs.enrollmentId(),
                refs.termResultId(),
                refs.academicYearId(),
                refs.academicTermId(),
                refs.classGradeId(),
                refs.streamId(),
                "V267 Controlled Report Card",
                ACTOR,
                ACTOR
        );
    }

    private int rowCount(
            UUID reportCardId
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_report_card
                        WHERE id = ?
                        """,
                        Integer.class,
                        reportCardId
                );

        return count == null
                ? 0
                : count;
    }

    private Fixture createFixture(
            String suffix
    ) {

        Fixture fixture =
                new Fixture(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

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

                                try {
                                    seedFixture(
                                            connection,
                                            fixture,
                                            suffix
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

        return fixture;
    }

    private void seedFixture(
            Connection connection,
            Fixture f,
            String suffix
    ) throws SQLException {

        insert(
                connection,
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                f.organizationId(),
                "V267-ORG-" + token(f.organizationId()),
                "V267 Report Card Org " + suffix
        );

        insert(
                connection,
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
                f.tenantId(),
                f.organizationId(),
                "V267-TENANT-" + token(f.tenantId()),
                "V267 Report Card Tenant " + suffix
        );

        insert(
                connection,
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
                    ?, ?, ?, ?,
                    'Report',
                    ?,
                    DATE '2015-01-01',
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                f.studentId(),
                f.tenantId(),
                "V267-STUDENT-" + token(f.studentId()),
                "V267-PLN-" + token(f.studentId()),
                "Card-" + suffix,
                ACTOR,
                ACTOR
        );

        insert(
                connection,
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
                    ?, ?, ?, ?,
                    DATE '2026-01-01',
                    DATE '2026-12-31',
                    FALSE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                f.academicYearId(),
                f.tenantId(),
                "V267-YEAR-" + token(f.academicYearId()),
                "V267 Academic Year " + suffix,
                ACTOR,
                ACTOR
        );

        insert(
                connection,
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
                    ?, ?, ?, ?, ?,
                    1,
                    DATE '2026-01-01',
                    DATE '2026-04-30',
                    FALSE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                f.academicTermId(),
                f.tenantId(),
                f.academicYearId(),
                "V267-TERM-" + token(f.academicTermId()),
                "V267 Term " + suffix,
                ACTOR,
                ACTOR
        );

        insert(
                connection,
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
                    ?, ?, ?, ?, ?,
                    1,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                f.classGradeId(),
                f.tenantId(),
                f.educationLevelId(),
                "V267-CLASS-" + token(f.classGradeId()),
                "V267 Class " + suffix,
                ACTOR,
                ACTOR
        );

        insert(
                connection,
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
                    ?, ?, ?, ?, ?, ?,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                f.streamId(),
                f.tenantId(),
                f.campusId(),
                f.classGradeId(),
                "V267-STREAM-" + token(f.streamId()),
                "V267 Stream " + suffix,
                ACTOR,
                ACTOR
        );

        insert(
                connection,
                """
                INSERT INTO gts_student_enrollment (
                    id,
                    tenant_id,
                    student_id,
                    academic_year_id,
                    academic_term_id,
                    campus_id,
                    class_grade_id,
                    stream_id,
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
                    ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    DATE '2026-01-01',
                    DATE '2026-01-01',
                    'NEW',
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                f.enrollmentId(),
                f.tenantId(),
                f.studentId(),
                f.academicYearId(),
                f.academicTermId(),
                f.campusId(),
                f.classGradeId(),
                f.streamId(),
                "V267-ENROLL-" + token(f.enrollmentId()),
                ACTOR,
                ACTOR
        );

        insert(
                connection,
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
                    total_subjects,
                    subjects_passed,
                    subjects_failed,
                    result_status,
                    withheld,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    0, 0, 0,
                    'DRAFT',
                    FALSE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                f.termResultId(),
                f.tenantId(),
                "V267-TERM-RESULT-" + token(f.termResultId()),
                f.studentId(),
                f.enrollmentId(),
                f.academicYearId(),
                f.academicTermId(),
                f.classGradeId(),
                f.streamId(),
                ACTOR,
                ACTOR
        );

        insert(
                connection,
                """
                INSERT INTO gts_report_card_template (
                    id,
                    tenant_id,
                    template_code,
                    template_name,
                    class_grade_id,
                    template_type,
                    template_configuration,
                    template_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    'TERM_REPORT',
                    '{}'::jsonb,
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                f.templateId(),
                f.tenantId(),
                "V267-TEMPLATE-" + token(f.templateId()),
                "V267 Report Card Template " + suffix,
                f.classGradeId(),
                ACTOR,
                ACTOR
        );
    }

    private static void insert(
            Connection connection,
            String sql,
            Object... values
    ) throws SQLException {

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            for (
                    int index = 0;
                    index < values.length;
                    index++
            ) {
                statement.setObject(
                        index + 1,
                        values[index]
                );
            }

            statement.executeUpdate();
        }
    }

    private static String token(
            UUID id
    ) {
        return id.toString()
                .substring(0, 8);
    }

    private record ReportCardRefs(
            UUID templateId,
            UUID studentId,
            UUID enrollmentId,
            UUID termResultId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId,
            UUID streamId
    ) {
    }

    private record Fixture(
            UUID organizationId,
            UUID tenantId,
            UUID studentId,
            UUID enrollmentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId,
            UUID streamId,
            UUID termResultId,
            UUID templateId,
            UUID campusId,
            UUID educationLevelId
    ) {
    }
}
