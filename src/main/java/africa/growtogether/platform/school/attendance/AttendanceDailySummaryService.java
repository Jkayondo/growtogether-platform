package africa.growtogether.platform.school.attendance;

import africa.growtogether.platform.school.profile.SchoolProfileService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceDailySummaryService {

    public static final String
            DAILY_REGISTER = "DAILY_REGISTER";

    private final StudentAttendanceRepository attendance;
    private final SchoolProfileService schools;

    public AttendanceDailySummaryService(
            StudentAttendanceRepository attendance,
            SchoolProfileService schools
    ) {
        this.attendance =
                Objects.requireNonNull(
                        attendance,
                        "attendance"
                );

        this.schools =
                Objects.requireNonNull(
                        schools,
                        "schools"
                );
    }

    /*
     * Resolve "today" using the school's configured timezone.
     *
     * StudentAttendance.recordedAt is intentionally not used to
     * determine the attendance date. V040 assigns period ownership
     * to gts_attendance_session.attendance_date.
     */
    @Transactional(readOnly = true)
    public AttendanceDailySummary loadToday(
            UUID tenantId
    ) {
        ZoneId schoolZone =
                schools.requireTimezone(
                        tenantId
                );

        return loadForDate(
                tenantId,
                LocalDate.now(schoolZone)
        );
    }

    @Transactional(readOnly = true)
    public AttendanceDailySummary loadForDate(
            UUID tenantId,
            LocalDate attendanceDate
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId"
        );

        Objects.requireNonNull(
                attendanceDate,
                "attendanceDate"
        );

        AttendanceDailyAggregate aggregate =
                attendance
                        .aggregateDailyRegisterByTenantAndDate(
                                tenantId,
                                attendanceDate
                        );

        if (aggregate == null) {
            return empty(
                    tenantId,
                    attendanceDate
            );
        }

        long expected =
                nonNegative(
                        aggregate.getExpectedStudentCount()
                );

        long recorded =
                nonNegative(
                        aggregate.getRecordedAttendanceCount()
                );

        long unrecorded =
                Math.max(
                        0L,
                        expected - recorded
                );

        return new AttendanceDailySummary(
                tenantId,
                attendanceDate,
                DAILY_REGISTER,
                nonNegative(
                        aggregate.getSessionCount()
                ),
                expected,
                recorded,
                unrecorded,
                nonNegative(
                        aggregate.getPresentCount()
                ),
                nonNegative(
                        aggregate.getAbsentCount()
                ),
                nonNegative(
                        aggregate.getLateCount()
                ),
                nonNegative(
                        aggregate.getExcusedAbsenceCount()
                ),
                nonNegative(
                        aggregate.getUnexcusedAbsenceCount()
                ),
                nonNegative(
                        aggregate.getMedicalAbsenceCount()
                ),
                nonNegative(
                        aggregate.getSchoolActivityCount()
                ),
                nonNegative(
                        aggregate.getRemoteLearningCount()
                ),
                nonNegative(
                        aggregate.getEarlyDepartureCount()
                ),
                nonNegative(
                        aggregate.getSuspendedCount()
                ),
                nonNegative(
                        aggregate.getNotRequiredCount()
                ),
                nonNegative(
                        aggregate.getUnknownCount()
                )
        );
    }

    private AttendanceDailySummary empty(
            UUID tenantId,
            LocalDate attendanceDate
    ) {
        return new AttendanceDailySummary(
                tenantId,
                attendanceDate,
                DAILY_REGISTER,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0L);
    }

    private long nonNegative(
            long value
    ) {
        return Math.max(
                0L,
                value
        );
    }
}
