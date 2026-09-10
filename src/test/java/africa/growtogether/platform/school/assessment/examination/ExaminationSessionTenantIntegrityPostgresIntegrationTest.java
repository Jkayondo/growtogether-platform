package africa.growtogether.platform.school.assessment.examination;

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
class ExaminationSessionTenantIntegrityPostgresIntegrationTest {


    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_examination_session_tenant_integrity"
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
         * Hibernate global validation is deliberately disabled
         * so unrelated worktree entities cannot prevent this
         * focused V261 tenant-integrity proof.
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
    void sameTenantSessionParentsSucceed() {

        String sessionCode =
                sessionCode(
                        "SAME"
                );

        int inserted =
                insertSession(
                        tenantA.tenantId(),
                        sessionCode,
                        tenantA.academicYearId(),
                        tenantA.academicTermId(),
                        tenantA.campusId()
                );


        assertThat(
                inserted
        ).isEqualTo(
                1
        );


        assertThat(
                sessionCount(
                        tenantA.tenantId(),
                        sessionCode
                )
        ).isEqualTo(
                1
        );
    }


    @Test
    void rejectsCrossTenantAcademicYear() {

        String sessionCode =
                sessionCode(
                        "YEAR"
                );


        assertThatThrownBy(
                () ->
                        insertSession(
                                tenantA.tenantId(),
                                sessionCode,
                                tenantB.academicYearId(),
                                tenantA.academicTermId(),
                                tenantA.campusId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_examination_session_academic_year_tenant"
                );


        assertThat(
                sessionCount(
                        tenantA.tenantId(),
                        sessionCode
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantAcademicTerm() {

        String sessionCode =
                sessionCode(
                        "TERM"
                );


        assertThatThrownBy(
                () ->
                        insertSession(
                                tenantA.tenantId(),
                                sessionCode,
                                tenantA.academicYearId(),
                                tenantB.academicTermId(),
                                tenantA.campusId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_examination_session_academic_term_tenant"
                );


        assertThat(
                sessionCount(
                        tenantA.tenantId(),
                        sessionCode
                )
        ).isZero();
    }


    @Test
    void rejectsCrossTenantCampus() {

        String sessionCode =
                sessionCode(
                        "CAMPUS"
                );


        assertThatThrownBy(
                () ->
                        insertSession(
                                tenantA.tenantId(),
                                sessionCode,
                                tenantA.academicYearId(),
                                tenantA.academicTermId(),
                                tenantB.campusId()
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasStackTraceContaining(
                        "fk_gts_examination_session_campus_tenant"
                );


        assertThat(
                sessionCount(
                        tenantA.tenantId(),
                        sessionCode
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

        UUID academicTermId =
                UUID.randomUUID();

        String auditUser =
                "session-integrity-test";


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
                "Session Integrity Organisation " + suffix
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
                "Session Integrity Tenant " + suffix
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
                "Session Integrity School " + suffix,
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
                    DATE '2026-01-05',
                    DATE '2026-04-30',
                    FALSE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                academicTermId,
                tenantId,
                academicYearId,
                "T1-" + suffix + "-" + shortId(
                        academicTermId
                ),
                "Term One " + suffix,
                auditUser,
                auditUser
        );


        return new TenantFixture(
                tenantId,
                campusId,
                academicYearId,
                academicTermId
        );
    }


    private int insertSession(
            UUID tenantId,
            String sessionCode,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId
    ) {

        String auditUser =
                "session-integrity-test";


        return jdbc.update(
                """
                INSERT INTO gts_examination_session (
                    id,
                    tenant_id,
                    session_code,
                    session_name,
                    academic_year_id,
                    academic_term_id,
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
                    ?, ?, ?, ?,
                    ?, ?, ?,
                    'INTERNAL',
                    DATE '2026-09-01',
                    DATE '2026-09-30',
                    'DRAFT',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                UUID.randomUUID(),
                tenantId,
                sessionCode,
                "Session Integrity Test",
                academicYearId,
                academicTermId,
                campusId,
                auditUser,
                auditUser
        );
    }


    private int sessionCount(
            UUID tenantId,
            String sessionCode
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_examination_session
                        WHERE tenant_id = ?
                          AND session_code = ?
                        """,
                        Integer.class,
                        tenantId,
                        sessionCode
                );


        return count == null
                ? 0
                : count;
    }


    private String sessionCode(
            String label
    ) {

        return "SESSION-"
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
            UUID campusId,
            UUID academicYearId,
            UUID academicTermId
    ) {
    }
}
