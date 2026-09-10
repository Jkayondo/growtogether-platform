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
class CandidateScoreTenantIntegrityPostgresIntegrationTest {


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
         * Flyway must construct the complete schema through V264.
         * Hibernate validation is intentionally disabled because this
         * test proves database tenant integrity, not ORM completeness.
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
    void sameTenantCandidateScoreSucceeds() {

        UUID scoreId =
                UUID.randomUUID();

        int inserted =
                insertCandidateScore(
                        scoreId,
                        tenantA.tenantId(),
                        tenantA.markSheetId(),
                        tenantA.markEntryBatchId(),
                        tenantA.examinationCandidateId(),
                        tenantA.candidatePaperRegistrationId(),
                        tenantA.studentId(),
                        tenantA.enrollmentId()
                );

        assertThat(
                inserted
        )
                .isEqualTo(
                        1
                );

        assertThat(
                candidateScoreCount(
                        scoreId
                )
        )
                .isEqualTo(
                        1
                );
    }


    @Test
    void rejectsCrossTenantMarkSheet() {

        UUID scoreId =
                UUID.randomUUID();

        assertCandidateScoreRejected(
                scoreId,
                "fk_gts_candidate_score_mark_sheet_tenant",
                () ->
                        insertCandidateScore(
                                scoreId,
                                tenantA.tenantId(),
                                tenantB.markSheetId(),
                                tenantA.markEntryBatchId(),
                                tenantA.examinationCandidateId(),
                                tenantA.candidatePaperRegistrationId(),
                                tenantA.studentId(),
                                tenantA.enrollmentId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantMarkEntryBatch() {

        UUID scoreId =
                UUID.randomUUID();

        assertCandidateScoreRejected(
                scoreId,
                "fk_gts_candidate_score_mark_entry_batch_tenant",
                () ->
                        insertCandidateScore(
                                scoreId,
                                tenantA.tenantId(),
                                tenantA.markSheetId(),
                                tenantB.markEntryBatchId(),
                                tenantA.examinationCandidateId(),
                                tenantA.candidatePaperRegistrationId(),
                                tenantA.studentId(),
                                tenantA.enrollmentId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantExaminationCandidate() {

        UUID scoreId =
                UUID.randomUUID();

        assertCandidateScoreRejected(
                scoreId,
                "fk_gts_candidate_score_examination_candidate_tenant",
                () ->
                        insertCandidateScore(
                                scoreId,
                                tenantA.tenantId(),
                                tenantA.markSheetId(),
                                tenantA.markEntryBatchId(),
                                tenantB.examinationCandidateId(),
                                tenantA.candidatePaperRegistrationId(),
                                tenantA.studentId(),
                                tenantA.enrollmentId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantCandidatePaperRegistration() {

        UUID scoreId =
                UUID.randomUUID();

        assertCandidateScoreRejected(
                scoreId,
                "fk_gts_candidate_score_candidate_paper_registration_tenant",
                () ->
                        insertCandidateScore(
                                scoreId,
                                tenantA.tenantId(),
                                tenantA.markSheetId(),
                                tenantA.markEntryBatchId(),
                                tenantA.examinationCandidateId(),
                                tenantB.candidatePaperRegistrationId(),
                                tenantA.studentId(),
                                tenantA.enrollmentId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantStudent() {

        UUID scoreId =
                UUID.randomUUID();

        assertCandidateScoreRejected(
                scoreId,
                "fk_gts_candidate_score_student_tenant",
                () ->
                        insertCandidateScore(
                                scoreId,
                                tenantA.tenantId(),
                                tenantA.markSheetId(),
                                tenantA.markEntryBatchId(),
                                tenantA.examinationCandidateId(),
                                tenantA.candidatePaperRegistrationId(),
                                tenantB.studentId(),
                                tenantA.enrollmentId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantStudentEnrollment() {

        UUID scoreId =
                UUID.randomUUID();

        assertCandidateScoreRejected(
                scoreId,
                "fk_gts_candidate_score_student_enrollment_tenant",
                () ->
                        insertCandidateScore(
                                scoreId,
                                tenantA.tenantId(),
                                tenantA.markSheetId(),
                                tenantA.markEntryBatchId(),
                                tenantA.examinationCandidateId(),
                                tenantA.candidatePaperRegistrationId(),
                                tenantA.studentId(),
                                tenantB.enrollmentId()
                        )
        );
    }


    @Test
    void rejectsCrossTenantMarkSheetForMarkEntryBatch() {

        UUID batchId =
                UUID.randomUUID();

        assertThatThrownBy(
                () ->
                        insertMarkEntryBatch(
                                batchId,
                                tenantA.tenantId(),
                                tenantB.markSheetId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_mark_entry_batch_mark_sheet_tenant"
                );

        assertThat(
                markEntryBatchCount(
                        batchId
                )
        )
                .isZero();
    }


    private void assertCandidateScoreRejected(
            UUID scoreId,
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
                candidateScoreCount(
                        scoreId
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

        UUID markSheetId =
                UUID.randomUUID();

        UUID markEntryBatchId =
                UUID.randomUUID();

        UUID examinationCandidateId =
                UUID.randomUUID();

        UUID candidatePaperRegistrationId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID enrollmentId =
                UUID.randomUUID();

        UUID assessmentPaperId =
                UUID.randomUUID();

        UUID examinationSessionId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();


        /*
         * Fixture rows intentionally bypass unrelated upstream FK/check
         * relationships. After fixture creation, PostgreSQL enforcement
         * is restored before the V264 relationships under test are used.
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
                                            'CS-ORG-%s',
                                            'Candidate Score Org %s',
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
                                            'CS-TENANT-%s',
                                            'Candidate Score Tenant %s',
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
                                            'CS-STUDENT-%s',
                                            'CS-PLN-%s',
                                            'Candidate',
                                            'Score-%s',
                                            DATE '2015-01-01',
                                            'ACTIVE',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
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
                                                        suffix
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
                                            'CS-ENROLL-%s',
                                            DATE '2026-01-01',
                                            DATE '2026-01-01',
                                            'NEW',
                                            'ACTIVE',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
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
                                                        )
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_examination_candidate (
                                            id,
                                            tenant_id,
                                            candidate_number,
                                            examination_session_id,
                                            student_id,
                                            student_enrollment_id,
                                            registration_date,
                                            eligibility_status,
                                            candidate_status,
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
                                            'CS-CAND-%s',
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            DATE '2026-09-09',
                                            'ELIGIBLE',
                                            'VERIFIED',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        examinationCandidateId,
                                                        tenantId,
                                                        shortId(
                                                                examinationCandidateId
                                                        ),
                                                        examinationSessionId,
                                                        studentId,
                                                        enrollmentId
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_candidate_paper_registration (
                                            id,
                                            tenant_id,
                                            examination_candidate_id,
                                            assessment_paper_id,
                                            registration_type,
                                            registration_status,
                                            registered_at,
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
                                            'STANDARD',
                                            'VERIFIED',
                                            CURRENT_TIMESTAMP,
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        candidatePaperRegistrationId,
                                                        tenantId,
                                                        examinationCandidateId,
                                                        assessmentPaperId
                                                )
                                );


                                statement.executeUpdate(
                                        """
                                        INSERT INTO gts_mark_sheet (
                                            id,
                                            tenant_id,
                                            mark_sheet_reference,
                                            assessment_component_id,
                                            subject_offering_id,
                                            class_offering_id,
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
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            'CS-MS-%s',
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            '%s'::uuid,
                                            100.00,
                                            'OPEN',
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
                                            CURRENT_TIMESTAMP,
                                            'candidate-score-integrity-test',
                                            0
                                        )
                                        """
                                                .formatted(
                                                        markSheetId,
                                                        tenantId,
                                                        shortId(
                                                                markSheetId
                                                        ),
                                                        UUID.randomUUID(),
                                                        UUID.randomUUID(),
                                                        UUID.randomUUID()
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


        insertMarkEntryBatch(
                markEntryBatchId,
                tenantId,
                markSheetId
        );


        return new Fixture(
                tenantId,
                markSheetId,
                markEntryBatchId,
                examinationCandidateId,
                candidatePaperRegistrationId,
                studentId,
                enrollmentId
        );
    }


    private int insertMarkEntryBatch(
            UUID batchId,
            UUID tenantId,
            UUID markSheetId
    ) {

        return jdbc.update(
                """
                INSERT INTO gts_mark_entry_batch (
                    id,
                    tenant_id,
                    batch_reference,
                    mark_sheet_id,
                    batch_type,
                    total_records,
                    accepted_records,
                    rejected_records,
                    batch_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'MANUAL',
                    0,
                    0,
                    0,
                    'PENDING',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    'candidate-score-integrity-test',
                    CURRENT_TIMESTAMP,
                    'candidate-score-integrity-test',
                    0
                )
                """,
                batchId,
                tenantId,
                "CS-BATCH-" + shortId(
                        batchId
                ),
                markSheetId
        );
    }


    private int insertCandidateScore(
            UUID scoreId,
            UUID tenantId,
            UUID markSheetId,
            UUID markEntryBatchId,
            UUID examinationCandidateId,
            UUID candidatePaperRegistrationId,
            UUID studentId,
            UUID enrollmentId
    ) {

        return jdbc.update(
                """
                INSERT INTO gts_candidate_score (
                    id,
                    tenant_id,
                    mark_sheet_id,
                    mark_entry_batch_id,
                    examination_candidate_id,
                    candidate_paper_registration_id,
                    student_id,
                    student_enrollment_id,
                    score_status,
                    absent,
                    exempted,
                    missing_mark,
                    withheld,
                    source_type,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?,
                    'DRAFT',
                    FALSE,
                    FALSE,
                    FALSE,
                    FALSE,
                    'MANUAL',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    'candidate-score-integrity-test',
                    CURRENT_TIMESTAMP,
                    'candidate-score-integrity-test',
                    0
                )
                """,
                scoreId,
                tenantId,
                markSheetId,
                markEntryBatchId,
                examinationCandidateId,
                candidatePaperRegistrationId,
                studentId,
                enrollmentId
        );
    }


    private int candidateScoreCount(
            UUID scoreId
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_candidate_score
                        WHERE id = ?
                        """,
                        Integer.class,
                        scoreId
                );

        return count == null
                ? 0
                : count;
    }


    private int markEntryBatchCount(
            UUID batchId
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_mark_entry_batch
                        WHERE id = ?
                        """,
                        Integer.class,
                        batchId
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


    @FunctionalInterface
    private interface ThrowingAction {

        void run();
    }


    private record Fixture(
            UUID tenantId,
            UUID markSheetId,
            UUID markEntryBatchId,
            UUID examinationCandidateId,
            UUID candidatePaperRegistrationId,
            UUID studentId,
            UUID enrollmentId
    ) {
    }
}
