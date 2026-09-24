package africa.growtogether.platform.school.attendance;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class AttendanceDailySummaryRepositoryPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "gt_ld10_attendance_test"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    private static SingleConnectionDataSource dataSource;

    private static NamedParameterJdbcTemplate jdbc;

    private static String aggregateSql;

    private UUID tenantId;
    private UUID otherTenantId;

    private LocalDate targetDate;
    private LocalDate otherDate;

    @BeforeAll
    static void migrateV040AndCaptureRepositorySql()
            throws Exception {

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                )
                .locations(
                        "classpath:db/migration"
                )
                .target(
                        MigrationVersion.fromVersion(
                                "040"
                        )
                )
                .load()
                .migrate();

        Integer v040 =
                new org.springframework.jdbc.core.JdbcTemplate(
                        new DriverManagerDataSource(
                                POSTGRES.getJdbcUrl(),
                                POSTGRES.getUsername(),
                                POSTGRES.getPassword()
                        )
                ).queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE script =
                            'V040__create_gtschool_attendance_foundation.sql'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertEquals(
                1,
                v040
        );

        java.sql.Connection connection =
                java.sql.DriverManager.getConnection(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                );

        dataSource =
                new SingleConnectionDataSource(
                        connection,
                        true
                );

        jdbc =
                new NamedParameterJdbcTemplate(
                        dataSource
                );

        Method method =
                StudentAttendanceRepository.class.getMethod(
                        "aggregateDailyRegisterByTenantAndDate",
                        UUID.class,
                        LocalDate.class
                );

        Query query =
                method.getAnnotation(
                        Query.class
                );

        assertNotNull(
                query
        );

        assertTrue(
                query.nativeQuery()
        );

        aggregateSql =
                query.value();

        assertTrue(
                aggregateSql.contains(
                        "gts_attendance_session"
                )
        );

        assertTrue(
                aggregateSql.contains(
                        "attendance_date = :attendanceDate"
                )
        );

        assertTrue(
                aggregateSql.contains(
                        "session_type = 'DAILY_REGISTER'"
                )
        );
    }

    @AfterAll
    static void closeDataSource() {

        if (dataSource != null) {
            dataSource.destroy();
        }
    }

    @BeforeEach
    void setUp() {

        jdbc.getJdbcTemplate()
                .execute(
                        """
                        TRUNCATE TABLE
                            gts_student_attendance,
                            gts_attendance_session
                        CASCADE
                        """
                );

        tenantId =
                UUID.randomUUID();

        otherTenantId =
                UUID.randomUUID();

        targetDate =
                LocalDate.of(
                        2026,
                        9,
                        24
                );

        otherDate =
                targetDate.minusDays(
                        1
                );
    }

    @Test
    void aggregatesOnlyAuthoritativeOperationalDailyRegisterRows() {

        UUID open =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "OPEN",
                        "ACTIVE",
                        10
                );

        UUID inProgress =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "IN_PROGRESS",
                        "ACTIVE",
                        20
                );

        UUID pendingReview =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "PENDING_REVIEW",
                        "ACTIVE",
                        30
                );

        UUID closed =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "CLOSED",
                        "ACTIVE",
                        40
                );

        UUID reopened =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "REOPENED",
                        "ACTIVE",
                        50
                );

        /*
         * Sessions that MUST NOT contribute.
         */
        UUID planned =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "PLANNED",
                        "ACTIVE",
                        1000
                );

        UUID cancelled =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "CANCELLED",
                        "ACTIVE",
                        1000
                );

        UUID archivedLifecycle =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "ARCHIVED",
                        "ACTIVE",
                        1000
                );

        UUID inactive =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "OPEN",
                        "INACTIVE",
                        1000
                );

        UUID lesson =
                session(
                        tenantId,
                        targetDate,
                        "LESSON",
                        "OPEN",
                        "ACTIVE",
                        1000
                );

        UUID wrongDate =
                session(
                        tenantId,
                        otherDate,
                        "DAILY_REGISTER",
                        "OPEN",
                        "ACTIVE",
                        1000
                );

        UUID wrongTenant =
                session(
                        otherTenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "OPEN",
                        "ACTIVE",
                        1000
                );

        /*
         * Twelve authoritative bucket rows.
         */
        attendance(
                tenantId,
                open,
                "PRESENT",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                inProgress,
                "ABSENT",
                "CONFIRMED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                pendingReview,
                "LATE",
                "PENDING_REVIEW",
                "ACTIVE"
        );

        attendance(
                tenantId,
                closed,
                "EXCUSED_ABSENCE",
                "CORRECTED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                reopened,
                "UNEXCUSED_ABSENCE",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "MEDICAL_ABSENCE",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "SCHOOL_ACTIVITY",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "REMOTE_LEARNING",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "EARLY_DEPARTURE",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "SUSPENDED",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "NOT_REQUIRED",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "UNKNOWN",
                "RECORDED",
                "ACTIVE"
        );

        /*
         * Invalidated attendance rows on an otherwise valid
         * session MUST NOT contribute.
         */
        attendance(
                tenantId,
                open,
                "PRESENT",
                "VOIDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "ABSENT",
                "DISPUTED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "LATE",
                "ARCHIVED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                open,
                "PRESENT",
                "RECORDED",
                "INACTIVE"
        );

        /*
         * Valid-looking rows in excluded sessions MUST NOT
         * contribute.
         */
        attendance(
                tenantId,
                planned,
                "PRESENT",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                cancelled,
                "PRESENT",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                archivedLifecycle,
                "PRESENT",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                inactive,
                "PRESENT",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                lesson,
                "PRESENT",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                tenantId,
                wrongDate,
                "PRESENT",
                "RECORDED",
                "ACTIVE"
        );

        attendance(
                otherTenantId,
                wrongTenant,
                "PRESENT",
                "RECORDED",
                "ACTIVE"
        );

        Map<String, Object> result =
                aggregate(
                        tenantId,
                        targetDate
                );

        assertLong(
                result,
                "sessionCount",
                5
        );

        assertLong(
                result,
                "expectedStudentCount",
                150
        );

        assertLong(
                result,
                "recordedAttendanceCount",
                12
        );

        assertLong(result, "presentCount", 1);
        assertLong(result, "absentCount", 1);
        assertLong(result, "lateCount", 1);
        assertLong(result, "excusedAbsenceCount", 1);
        assertLong(result, "unexcusedAbsenceCount", 1);
        assertLong(result, "medicalAbsenceCount", 1);
        assertLong(result, "schoolActivityCount", 1);
        assertLong(result, "remoteLearningCount", 1);
        assertLong(result, "earlyDepartureCount", 1);
        assertLong(result, "suspendedCount", 1);
        assertLong(result, "notRequiredCount", 1);
        assertLong(result, "unknownCount", 1);
    }

    @Test
    void zeroSessionDateReturnsZeroSafeAggregate() {

        Map<String, Object> result =
                aggregate(
                        tenantId,
                        targetDate
                );

        assertLong(result, "sessionCount", 0);
        assertLong(result, "expectedStudentCount", 0);
        assertLong(result, "recordedAttendanceCount", 0);
        assertLong(result, "presentCount", 0);
        assertLong(result, "absentCount", 0);
        assertLong(result, "lateCount", 0);
    }

    @Test
    void tenantSessionStudentUniquenessIsEnforcedByPostgres() {

        UUID session =
                session(
                        tenantId,
                        targetDate,
                        "DAILY_REGISTER",
                        "OPEN",
                        "ACTIVE",
                        10
                );

        UUID studentId =
                UUID.randomUUID();

        UUID enrollmentId =
                UUID.randomUUID();

        attendance(
                tenantId,
                session,
                studentId,
                enrollmentId,
                "PRESENT",
                "RECORDED",
                "ACTIVE"
        );

        withForeignKeysBypassed(
                () -> assertThrows(
                        DataIntegrityViolationException.class,
                        () -> insertAttendance(
                                UUID.randomUUID(),
                                tenantId,
                                session,
                                studentId,
                                UUID.randomUUID(),
                                "PRESENT",
                                "RECORDED",
                                "ACTIVE"
                        )
                )
        );
    }

    private Map<String, Object> aggregate(
            UUID tenant,
            LocalDate date
    ) {

        return jdbc.queryForMap(
                aggregateSql,
                new MapSqlParameterSource()
                        .addValue(
                                "tenantId",
                                tenant
                        )
                        .addValue(
                                "attendanceDate",
                                date
                        )
        );
    }

    private UUID session(
            UUID tenant,
            LocalDate date,
            String sessionType,
            String sessionStatus,
            String entityStatus,
            int expectedStudentCount
    ) {

        UUID id =
                UUID.randomUUID();

        withForeignKeysBypassed(
                () -> jdbc.update(
                        """
                        INSERT INTO gts_attendance_session (
                            id,
                            tenant_id,
                            session_reference,
                            academic_year_id,
                            campus_id,
                            class_grade_id,
                            attendance_date,
                            session_type,
                            expected_student_count,
                            recorded_student_count,
                            register_closed_at,
                            session_status,
                            status,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by,
                            version
                        )
                        VALUES (
                            :id,
                            :tenantId,
                            :sessionReference,
                            :academicYearId,
                            :campusId,
                            :classGradeId,
                            :attendanceDate,
                            :sessionType,
                            :expectedStudentCount,
                            0,
                            CASE
                                WHEN :sessionStatus = 'CLOSED'
                                THEN CURRENT_TIMESTAMP
                                ELSE NULL
                            END,
                            :sessionStatus,
                            :entityStatus,
                            CURRENT_TIMESTAMP,
                            'ld10-postgres-proof',
                            CURRENT_TIMESTAMP,
                            'ld10-postgres-proof',
                            0
                        )
                        """,
                        new MapSqlParameterSource()
                                .addValue("id", id)
                                .addValue("tenantId", tenant)
                                .addValue(
                                        "sessionReference",
                                        "LD10-" + id
                                )
                                .addValue(
                                        "academicYearId",
                                        UUID.randomUUID()
                                )
                                .addValue(
                                        "campusId",
                                        UUID.randomUUID()
                                )
                                .addValue(
                                        "classGradeId",
                                        UUID.randomUUID()
                                )
                                .addValue(
                                        "attendanceDate",
                                        date
                                )
                                .addValue(
                                        "sessionType",
                                        sessionType
                                )
                                .addValue(
                                        "expectedStudentCount",
                                        expectedStudentCount
                                )
                                .addValue(
                                        "sessionStatus",
                                        sessionStatus
                                )
                                .addValue(
                                        "entityStatus",
                                        entityStatus
                                )
                )
        );

        return id;
    }

    private void attendance(
            UUID tenant,
            UUID session,
            String attendanceStatus,
            String recordStatus,
            String entityStatus
    ) {

        attendance(
                tenant,
                session,
                UUID.randomUUID(),
                UUID.randomUUID(),
                attendanceStatus,
                recordStatus,
                entityStatus
        );
    }

    private void attendance(
            UUID tenant,
            UUID session,
            UUID studentId,
            UUID enrollmentId,
            String attendanceStatus,
            String recordStatus,
            String entityStatus
    ) {

        withForeignKeysBypassed(
                () -> insertAttendance(
                        UUID.randomUUID(),
                        tenant,
                        session,
                        studentId,
                        enrollmentId,
                        attendanceStatus,
                        recordStatus,
                        entityStatus
                )
        );
    }

    private void insertAttendance(
            UUID id,
            UUID tenant,
            UUID session,
            UUID studentId,
            UUID enrollmentId,
            String attendanceStatus,
            String recordStatus,
            String entityStatus
    ) {

        jdbc.update(
                """
                INSERT INTO gts_student_attendance (
                    id,
                    tenant_id,
                    attendance_session_id,
                    student_id,
                    student_enrollment_id,
                    attendance_status,
                    recorded_at,
                    record_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    :id,
                    :tenantId,
                    :sessionId,
                    :studentId,
                    :enrollmentId,
                    :attendanceStatus,
                    CURRENT_TIMESTAMP,
                    :recordStatus,
                    :entityStatus,
                    CURRENT_TIMESTAMP,
                    'ld10-postgres-proof',
                    CURRENT_TIMESTAMP,
                    'ld10-postgres-proof',
                    0
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("tenantId", tenant)
                        .addValue("sessionId", session)
                        .addValue("studentId", studentId)
                        .addValue(
                                "enrollmentId",
                                enrollmentId
                        )
                        .addValue(
                                "attendanceStatus",
                                attendanceStatus
                        )
                        .addValue(
                                "recordStatus",
                                recordStatus
                        )
                        .addValue(
                                "entityStatus",
                                entityStatus
                        )
        );
    }

    private void withForeignKeysBypassed(
            Runnable action
    ) {

        jdbc.getJdbcTemplate()
                .execute(
                        "SET session_replication_role = replica"
                );

        try {
            action.run();
        } finally {
            jdbc.getJdbcTemplate()
                    .execute(
                            "SET session_replication_role = origin"
                    );
        }
    }

    private void assertLong(
            Map<String, Object> result,
            String column,
            long expected
    ) {

        Object value =
                result.get(
                        column
                );

        assertNotNull(
                value,
                "Missing aggregate column: " + column
        );

        assertTrue(
                value instanceof Number,
                "Aggregate column is not numeric: " + column
        );

        assertEquals(
                expected,
                ((Number) value).longValue(),
                column
        );
    }
}
