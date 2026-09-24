package africa.growtogether.platform.school.attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface StudentAttendanceRepository
        extends JpaRepository<StudentAttendance, UUID> {


    List<StudentAttendance> findByStudentId(
            UUID studentId
    );


    List<StudentAttendance> findByStudentIdAndAttendanceStatus(
            UUID studentId,
            String attendanceStatus
    );

    long countByTenantIdAndAttendanceStatus(
            UUID tenantId,
            String attendanceStatus
   );

    /*
     * Authoritative school-day DAILY_REGISTER aggregate.
     *
     * Period ownership belongs to gts_attendance_session.attendance_date,
     * not StudentAttendance.recordedAt.
     *
     * Operational sessions include:
     * OPEN, IN_PROGRESS, PENDING_REVIEW, CLOSED and REOPENED.
     *
     * PLANNED has not started.
     * CANCELLED and ARCHIVED are excluded.
     *
     * Attendance rows exclude DISPUTED, VOIDED and ARCHIVED records.
     * No attendance-percentage policy is inferred here.
     */
    @Query(
            value = """
                    WITH selected_sessions AS (
                        SELECT
                            id,
                            expected_student_count
                        FROM gts_attendance_session
                        WHERE tenant_id = :tenantId
                          AND attendance_date = :attendanceDate
                          AND session_type = 'DAILY_REGISTER'
                          AND status = 'ACTIVE'
                          AND session_status IN (
                              'OPEN',
                              'IN_PROGRESS',
                              'PENDING_REVIEW',
                              'CLOSED',
                              'REOPENED'
                          )
                    ),
                    session_totals AS (
                        SELECT
                            COUNT(*)::bigint
                                AS session_count,
                            COALESCE(
                                SUM(expected_student_count),
                                0
                            )::bigint
                                AS expected_student_count
                        FROM selected_sessions
                    ),
                    attendance_totals AS (
                        SELECT
                            COUNT(a.id)::bigint
                                AS recorded_attendance_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status = 'PRESENT'
                            )::bigint
                                AS present_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status = 'ABSENT'
                            )::bigint
                                AS absent_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status = 'LATE'
                            )::bigint
                                AS late_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status =
                                      'EXCUSED_ABSENCE'
                            )::bigint
                                AS excused_absence_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status =
                                      'UNEXCUSED_ABSENCE'
                            )::bigint
                                AS unexcused_absence_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status =
                                      'MEDICAL_ABSENCE'
                            )::bigint
                                AS medical_absence_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status =
                                      'SCHOOL_ACTIVITY'
                            )::bigint
                                AS school_activity_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status =
                                      'REMOTE_LEARNING'
                            )::bigint
                                AS remote_learning_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status =
                                      'EARLY_DEPARTURE'
                            )::bigint
                                AS early_departure_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status =
                                      'SUSPENDED'
                            )::bigint
                                AS suspended_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status =
                                      'NOT_REQUIRED'
                            )::bigint
                                AS not_required_count,

                            COUNT(a.id) FILTER (
                                WHERE a.attendance_status =
                                      'UNKNOWN'
                            )::bigint
                                AS unknown_count
                        FROM selected_sessions s
                        LEFT JOIN gts_student_attendance a
                          ON a.tenant_id = :tenantId
                         AND a.attendance_session_id = s.id
                         AND a.status = 'ACTIVE'
                         AND a.record_status IN (
                             'RECORDED',
                             'PENDING_REVIEW',
                             'CONFIRMED',
                             'CORRECTED'
                         )
                    )
                    SELECT
                        st.session_count
                            AS sessionCount,
                        st.expected_student_count
                            AS expectedStudentCount,
                        at.recorded_attendance_count
                            AS recordedAttendanceCount,
                        at.present_count
                            AS presentCount,
                        at.absent_count
                            AS absentCount,
                        at.late_count
                            AS lateCount,
                        at.excused_absence_count
                            AS excusedAbsenceCount,
                        at.unexcused_absence_count
                            AS unexcusedAbsenceCount,
                        at.medical_absence_count
                            AS medicalAbsenceCount,
                        at.school_activity_count
                            AS schoolActivityCount,
                        at.remote_learning_count
                            AS remoteLearningCount,
                        at.early_departure_count
                            AS earlyDepartureCount,
                        at.suspended_count
                            AS suspendedCount,
                        at.not_required_count
                            AS notRequiredCount,
                        at.unknown_count
                            AS unknownCount
                    FROM session_totals st
                    CROSS JOIN attendance_totals at
                    """,
            nativeQuery = true
    )
    AttendanceDailyAggregate aggregateDailyRegisterByTenantAndDate(
            @Param("tenantId") UUID tenantId,
            @Param("attendanceDate") LocalDate attendanceDate
    );

}
