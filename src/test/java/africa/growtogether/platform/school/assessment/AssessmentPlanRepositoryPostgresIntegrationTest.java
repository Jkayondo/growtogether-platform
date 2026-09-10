package africa.growtogether.platform.school.assessment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(
        classMode = DirtiesContext.ClassMode.AFTER_CLASS
)
@SpringBootTest
class AssessmentPlanRepositoryPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_assessment_plan_repository"
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
         * This is a focused Assessment Plan repository/PostgreSQL test.
         *
         * Flyway remains enabled and constructs the actual PostgreSQL
         * schema. Hibernate global schema validation is disabled here so
         * unrelated worktree entities outside this test's persistence
         * boundary cannot prevent the repository behavior under test
         * from executing.
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


    @Autowired
    private AssessmentPlanRepository repository;


    private UUID tenantId;
    private UUID academicYearId;
    private UUID campusId;
    private UUID classGradeId;


    @BeforeEach
    void setUp() {

        tenantId =
                UUID.randomUUID();

        academicYearId =
                UUID.randomUUID();

        campusId =
                UUID.randomUUID();

        classGradeId =
                UUID.randomUUID();

        insertBaseFixture();
        insertAssessmentPlan();
    }


    @Test
    void countsNullSafeAcademicScopeAgainstPostgres() {

        long exactScope =
                repository.countByTenantAndAcademicScope(
                        tenantId,
                        academicYearId,
                        null,
                        campusId,
                        null,
                        null,
                        classGradeId,
                        null
                );

        long changedClassGrade =
                repository.countByTenantAndAcademicScope(
                        tenantId,
                        academicYearId,
                        null,
                        campusId,
                        null,
                        null,
                        UUID.randomUUID(),
                        null
                );

        long differentTenant =
                repository.countByTenantAndAcademicScope(
                        UUID.randomUUID(),
                        academicYearId,
                        null,
                        campusId,
                        null,
                        null,
                        classGradeId,
                        null
                );

        assertThat(
                exactScope
        ).isEqualTo(
                1L
        );

        assertThat(
                changedClassGrade
        ).isZero();

        assertThat(
                differentTenant
        ).isZero();
    }


    private void insertAssessmentPlan() {

        String auditUser =
                "assessment-plan-repository-test";

        jdbc.update(
                """
                INSERT INTO gts_assessment_plan (
                    id,
                    tenant_id,
                    plan_code,
                    plan_name,
                    academic_year_id,
                    campus_id,
                    class_grade_id,
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
                    ?, ?, ?, ?,
                    ?, ?, ?,
                    DATE '2026-01-01',
                    'DRAFT',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                UUID.randomUUID(),
                tenantId,
                "PLAN-" + shortId(
                        UUID.randomUUID()
                ),
                "Nullable Scope Assessment Plan",
                academicYearId,
                campusId,
                classGradeId,
                auditUser,
                auditUser
        );
    }


    private void insertBaseFixture() {

        UUID organizationId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        String auditUser =
                "assessment-plan-repository-test";


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
                "ORG-" + shortId(
                        organizationId
                ),
                "Assessment Plan Repository Organisation"
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
                "TEN-" + shortId(
                        tenantId
                ),
                "Assessment Plan Repository Tenant"
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
                "SCH-" + shortId(
                        schoolProfileId
                ),
                "Assessment Plan Repository School",
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
                "MAIN-" + shortId(
                        campusId
                ),
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
                    ?, ?,
                    '2026',
                    'Academic Year 2026',
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
                "PRIMARY-" + shortId(
                        educationLevelId
                ),
                "Primary",
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
                "P1-" + shortId(
                        classGradeId
                ),
                "Primary One",
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
