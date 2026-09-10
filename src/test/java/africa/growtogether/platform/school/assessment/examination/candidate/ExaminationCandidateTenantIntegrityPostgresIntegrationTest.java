package africa.growtogether.platform.school.assessment.examination.candidate;

import org.junit.jupiter.api.BeforeEach;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(
        classMode = DirtiesContext.ClassMode.AFTER_CLASS
)
@SpringBootTest
class ExaminationCandidateTenantIntegrityPostgresIntegrationTest {


    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_examination_candidate_tenant_integrity"
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
         * Hibernate global validation is deliberately disabled for this
         * focused database-integrity test so unrelated worktree entities
         * cannot prevent the V237 constraints from being exercised.
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


    private TenantFixture tenantA;
    private TenantFixture tenantB;


    @BeforeEach
    void setUp() {

        tenantA =
                createTenantFixture(
                        "A"
                );

        tenantB =
                createTenantFixture(
                        "B"
                );
    }


    @Test
    void sameTenantCandidateInsertSucceeds() {

        String candidateNumber =
                candidateNumber(
                        "SAME"
                );

        int inserted =
                insertCandidate(
                        tenantA.tenantId(),
                        candidateNumber,
                        tenantA.examinationSessionId(),
                        tenantA.studentId(),
                        tenantA.studentEnrollmentId()
                );

        assertThat(
                inserted
        ).isEqualTo(
                1
        );

        assertThat(
                candidateCount(
                        tenantA.tenantId(),
                        candidateNumber
                )
        ).isEqualTo(
                1
        );
    }


    @Test
    void rejectsCrossTenantExaminationSession() {

        String candidateNumber =
                candidateNumber(
                        "SESSION"
                );

        assertThatThrownBy(
                () -> insertCandidate(
                        tenantA.tenantId(),
                        candidateNumber,
                        tenantB.examinationSessionId(),
                        tenantA.studentId(),
                        tenantA.studentEnrollmentId()
                )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_examination_candidate_session_tenant"
                );

        assertThat(
                candidateCount(
                        tenantA.tenantId(),
                        candidateNumber
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantStudent() {

        String candidateNumber =
                candidateNumber(
                        "STUDENT"
                );

        assertThatThrownBy(
                () -> insertCandidate(
                        tenantA.tenantId(),
                        candidateNumber,
                        tenantA.examinationSessionId(),
                        tenantB.studentId(),
                        tenantA.studentEnrollmentId()
                )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_examination_candidate_student_tenant"
                );

        assertThat(
                candidateCount(
                        tenantA.tenantId(),
                        candidateNumber
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantStudentEnrollment() {

        String candidateNumber =
                candidateNumber(
                        "ENROLLMENT"
                );

        assertThatThrownBy(
                () -> insertCandidate(
                        tenantA.tenantId(),
                        candidateNumber,
                        tenantA.examinationSessionId(),
                        tenantA.studentId(),
                        tenantB.studentEnrollmentId()
                )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_examination_candidate_enrollment_tenant"
                );

        assertThat(
                candidateCount(
                        tenantA.tenantId(),
                        candidateNumber
                )
        ).isZero();
    }


    private TenantFixture createTenantFixture(
            String suffix
    ) {

        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID studentEnrollmentId =
                UUID.randomUUID();

        UUID examinationSessionId =
                UUID.randomUUID();

        String auditUser =
                "candidate-integrity-test";


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
                "ORG-" + suffix + "-" + shortId(
                        organizationId
                ),
                "Candidate Integrity Organisation " + suffix
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
                "TEN-" + suffix + "-" + shortId(
                        tenantId
                ),
                "Candidate Integrity Tenant " + suffix
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
                    'UG',
                    'UGX',
                    'Africa/Kampala',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                schoolProfileId,
                tenantId,
                "SCH-" + suffix + "-" + shortId(
                        schoolProfileId
                ),
                "Candidate Integrity School " + suffix,
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
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                campusId,
                tenantId,
                schoolProfileId,
                "MAIN-" + suffix + "-" + shortId(
                        campusId
                ),
                "Main Campus " + suffix,
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
                    ?, ?, ?, ?,
                    DATE '2026-01-01',
                    DATE '2026-12-31',
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                academicYearId,
                tenantId,
                "2026-" + suffix + "-" + shortId(
                        academicYearId
                ),
                "Academic Year 2026 " + suffix,
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
                    ?, ?, ?, ?,
                    1,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                educationLevelId,
                tenantId,
                "PRIMARY-" + suffix + "-" + shortId(
                        educationLevelId
                ),
                "Primary " + suffix,
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
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                classGradeId,
                tenantId,
                educationLevelId,
                "P1-" + suffix + "-" + shortId(
                        classGradeId
                ),
                "Primary One " + suffix,
                auditUser,
                auditUser
        );


        jdbc.update(
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
                    'Test',
                    ?,
                    DATE '2015-01-01',
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                studentId,
                tenantId,
                "STU-" + suffix + "-" + shortId(
                        studentId
                ),
                "PLN-" + studentId,
                "Learner-" + suffix,
                auditUser,
                auditUser
        );


        jdbc.update(
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
                    ?, ?, ?, ?, ?, ?, ?,
                    DATE '2026-01-10',
                    DATE '2026-01-10',
                    'NEW',
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                studentEnrollmentId,
                tenantId,
                studentId,
                academicYearId,
                campusId,
                classGradeId,
                "ENR-" + suffix + "-" + shortId(
                        studentEnrollmentId
                ),
                auditUser,
                auditUser
        );


        jdbc.update(
                """
                INSERT INTO gts_examination_session (
                    id,
                    tenant_id,
                    session_code,
                    session_name,
                    academic_year_id,
                    campus_id,
                    examination_type,
                    start_date,
                    end_date,
                    session_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?,
                    'INTERNAL',
                    DATE '2026-09-01',
                    DATE '2026-09-30',
                    'REGISTRATION_OPEN',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                examinationSessionId,
                tenantId,
                "EXAM-" + suffix + "-" + shortId(
                        examinationSessionId
                ),
                "Examination Session " + suffix,
                academicYearId,
                campusId,
                auditUser,
                auditUser
        );


        return new TenantFixture(
                tenantId,
                studentId,
                studentEnrollmentId,
                examinationSessionId
        );
    }


    private int insertCandidate(
            UUID tenantId,
            String candidateNumber,
            UUID examinationSessionId,
            UUID studentId,
            UUID studentEnrollmentId
    ) {

        String auditUser =
                "candidate-integrity-test";

        return jdbc.update(
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
                    ?, ?, ?, ?, ?, ?,
                    CURRENT_DATE,
                    'PENDING',
                    'REGISTERED',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                UUID.randomUUID(),
                tenantId,
                candidateNumber,
                examinationSessionId,
                studentId,
                studentEnrollmentId,
                auditUser,
                auditUser
        );
    }


    private int candidateCount(
            UUID tenantId,
            String candidateNumber
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_examination_candidate
                        WHERE tenant_id = ?
                          AND candidate_number = ?
                        """,
                        Integer.class,
                        tenantId,
                        candidateNumber
                );

        return count == null
                ? 0
                : count;
    }


    private String candidateNumber(
            String label
    ) {

        return "CAND-"
                + label
                + "-"
                + shortId(
                        UUID.randomUUID()
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


    private record TenantFixture(
            UUID tenantId,
            UUID studentId,
            UUID studentEnrollmentId,
            UUID examinationSessionId
    ) {
    }

}
