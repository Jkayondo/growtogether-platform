package africa.growtogether.platform.school.timetable.core;

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

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class TimetableYearLevelUniquenessIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_timetable_year_uniqueness"
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


    private UUID tenantId;
    private UUID academicYearId;
    private UUID campusId;
    private UUID bellScheduleId;


    @BeforeEach
    void setUp() {

        tenantId =
                UUID.randomUUID();

        academicYearId =
                UUID.randomUUID();

        campusId =
                UUID.randomUUID();

        bellScheduleId =
                UUID.randomUUID();

        insertBaseFixture();
    }


    @Test
    void rejectsDuplicateYearLevelTimetableVersion() {

        insertDraftTimetable(
                UUID.randomUUID(),
                "YEAR-DRAFT-001",
                1
        );

        assertThatThrownBy(
                () ->
                        insertDraftTimetable(
                                UUID.randomUUID(),
                                "YEAR-DRAFT-002",
                                1
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasMessageContaining(
                        "uq_gts_timetable_year_version"
                );
    }


    @Test
    void rejectsSecondActiveYearLevelTimetableInSameScope() {

        insertActiveTimetable(
                UUID.randomUUID(),
                "YEAR-ACTIVE-001",
                1
        );

        /*
         * Version 2 deliberately avoids the version-uniqueness index.
         * The failure must therefore come from the ACTIVE-scope
         * invariant introduced by V141.
         */
        assertThatThrownBy(
                () ->
                        insertActiveTimetable(
                                UUID.randomUUID(),
                                "YEAR-ACTIVE-002",
                                2
                        )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                )
                .hasMessageContaining(
                        "uq_gts_active_timetable_year"
                );
    }


    private void insertDraftTimetable(
            UUID timetableId,
            String timetableCode,
            int versionNumber
    ) {

        jdbc.update(
                """
                INSERT INTO gts_timetable (
                    id,
                    tenant_id,
                    timetable_code,
                    timetable_name,
                    academic_year_id,
                    campus_id,
                    bell_schedule_id,
                    timetable_type,
                    version_number,
                    effective_from,
                    generated_by,
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
                    ?, ?, ?,
                    'MASTER',
                    ?,
                    DATE '2026-01-01',
                    'MANUAL',
                    'DRAFT',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                timetableId,
                tenantId,
                timetableCode,
                "Year Level Draft Timetable",
                academicYearId,
                campusId,
                bellScheduleId,
                versionNumber,
                "a11-year-uniqueness-test",
                "a11-year-uniqueness-test"
        );
    }


    private void insertActiveTimetable(
            UUID timetableId,
            String timetableCode,
            int versionNumber
    ) {

        UUID actor =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_timetable (
                    id,
                    tenant_id,
                    timetable_code,
                    timetable_name,
                    academic_year_id,
                    campus_id,
                    bell_schedule_id,
                    timetable_type,
                    version_number,
                    effective_from,
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
                    ?, ?, ?,
                    'MASTER',
                    ?,
                    DATE '2026-01-01',
                    'MANUAL',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                timetableId,
                tenantId,
                timetableCode,
                "Year Level Active Timetable",
                academicYearId,
                campusId,
                bellScheduleId,
                versionNumber,
                actor,
                actor,
                "a11-year-uniqueness-test",
                "a11-year-uniqueness-test"
        );
    }


    private void insertBaseFixture() {

        UUID organizationId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        String auditUser =
                "a11-year-uniqueness-test";


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
                "A11 Year-Level Uniqueness Organisation"
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
                "TEN-" + shortId(tenantId),
                "A11 Year-Level Uniqueness Tenant"
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
                "SCH-" + shortId(schoolProfileId),
                "A11 Year-Level Uniqueness School",
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
                    DATE '2026-01-01',
                    DATE '2026-12-31',
                    TRUE, TRUE, TRUE, TRUE, TRUE,
                    FALSE, FALSE,
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
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
