package africa.growtogether.platform.school.assessment.examination.registration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(
        classMode = DirtiesContext.ClassMode.AFTER_CLASS
)
@SpringBootTest
class CandidatePaperRegistrationTenantIntegrityPostgresIntegrationTest {


    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_candidate_paper_registration_tenant_integrity"
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
         * entities cannot prevent this focused V262 integrity proof.
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
    void sameTenantCandidatePaperRegistrationSucceeds() {

        UUID registrationId =
                UUID.randomUUID();


        int inserted =
                insertRegistration(
                        registrationId,
                        tenantA.tenantId(),
                        tenantA.candidateId(),
                        tenantA.paperId(),
                        tenantA.scheduleId()
                );


        assertThat(
                inserted
        ).isEqualTo(
                1
        );


        assertThat(
                registrationCount(
                        registrationId
                )
        ).isEqualTo(
                1
        );
    }


    @Test
    void rejectsCrossTenantCandidate() {

        UUID registrationId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertRegistration(
                                registrationId,
                                tenantA.tenantId(),
                                tenantB.candidateId(),
                                tenantA.paperId(),
                                tenantA.scheduleId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_candidate_paper_registration_candidate_tenant"
                );


        assertThat(
                registrationCount(
                        registrationId
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantAssessmentPaper() {

        UUID registrationId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertRegistration(
                                registrationId,
                                tenantA.tenantId(),
                                tenantA.candidateId(),
                                tenantB.paperId(),
                                null
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_candidate_paper_registration_paper_tenant"
                );


        assertThat(
                registrationCount(
                        registrationId
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantExaminationSchedule() {

        UUID registrationId =
                UUID.randomUUID();


        assertThatThrownBy(
                () ->
                        insertRegistration(
                                registrationId,
                                tenantA.tenantId(),
                                tenantA.candidateId(),
                                tenantA.paperId(),
                                tenantB.scheduleId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_candidate_paper_registration_schedule_tenant"
                );


        assertThat(
                registrationCount(
                        registrationId
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

        UUID examinationSessionId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID enrollmentId =
                UUID.randomUUID();

        UUID subjectOfferingId =
                UUID.randomUUID();

        UUID classOfferingId =
                UUID.randomUUID();

        UUID candidateId =
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
                "ORG-CPR-" + suffix + "-" + shortId(
                        organizationId
                ),
                "Candidate Paper Integrity Organisation " + suffix
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
                "TEN-CPR-" + suffix + "-" + shortId(
                        tenantId
                ),
                "Candidate Paper Integrity Tenant " + suffix
        );


        /*
         * These three records are the immediate parent records of
         * gts_candidate_paper_registration.
         *
         * Their unrelated upstream foreign keys are disabled only
         * while the focused fixture is inserted. V262 constraints
         * remain fully enabled when insertRegistration() executes.
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
                                        'CPR-CAND-%s',
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        CURRENT_DATE,
                                        'PENDING',
                                        'REGISTERED',
                                        'ACTIVE',
                                        CURRENT_TIMESTAMP,
                                        'candidate-paper-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'candidate-paper-integrity-test',
                                        0
                                    )
                                    """
                                            .formatted(
                                                    candidateId,
                                                    tenantId,
                                                    shortId(
                                                            candidateId
                                                    ),
                                                    examinationSessionId,
                                                    studentId,
                                                    enrollmentId
                                            )
                            );


                            statement.executeUpdate(
                                    """
                                    INSERT INTO gts_assessment_paper (
                                        id,
                                        tenant_id,
                                        paper_code,
                                        paper_name,
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
                                        'CPR-PAPER-%s',
                                        'Candidate Paper Integrity Paper %s',
                                        '%s'::uuid,
                                        '%s'::uuid,
                                        'WRITTEN',
                                        100.00,
                                        TRUE,
                                        'DRAFT',
                                        'ACTIVE',
                                        CURRENT_TIMESTAMP,
                                        'candidate-paper-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'candidate-paper-integrity-test',
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
                                        'CPR-SCH-%s',
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
                                        'candidate-paper-integrity-test',
                                        CURRENT_TIMESTAMP,
                                        'candidate-paper-integrity-test',
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
                                                    classOfferingId
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
                candidateId,
                paperId,
                scheduleId
        );
    }


    private int insertRegistration(
            UUID registrationId,
            UUID tenantId,
            UUID candidateId,
            UUID paperId,
            UUID scheduleId
    ) {

        String auditUser =
                "candidate-paper-integrity-test";


        return jdbc.update(
                """
                INSERT INTO gts_candidate_paper_registration (
                    id,
                    tenant_id,
                    examination_candidate_id,
                    assessment_paper_id,
                    examination_schedule_id,
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
                    ?, ?, ?, ?, ?,
                    'STANDARD',
                    'REGISTERED',
                    CURRENT_TIMESTAMP,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                registrationId,
                tenantId,
                candidateId,
                paperId,
                scheduleId,
                auditUser,
                auditUser
        );
    }


    private int registrationCount(
            UUID registrationId
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_candidate_paper_registration
                        WHERE id = ?
                        """,
                        Integer.class,
                        registrationId
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
            UUID candidateId,
            UUID paperId,
            UUID scheduleId
    ) {
    }

}
