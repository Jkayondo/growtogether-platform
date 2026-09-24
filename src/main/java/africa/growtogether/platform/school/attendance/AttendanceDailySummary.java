package africa.growtogether.platform.school.attendance;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceDailySummary(
        UUID tenantId,
        LocalDate attendanceDate,
        String sessionType,
        long sessionCount,
        long expectedStudentCount,
        long recordedAttendanceCount,
        long unrecordedCount,
        long presentCount,
        long absentCount,
        long lateCount,
        long excusedAbsenceCount,
        long unexcusedAbsenceCount,
        long medicalAbsenceCount,
        long schoolActivityCount,
        long remoteLearningCount,
        long earlyDepartureCount,
        long suspendedCount,
        long notRequiredCount,
        long unknownCount
) {

    public AttendanceDailySummary {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId is required"
            );
        }

        if (attendanceDate == null) {
            throw new IllegalArgumentException(
                    "attendanceDate is required"
            );
        }

        if (sessionType == null || sessionType.isBlank()) {
            throw new IllegalArgumentException(
                    "sessionType is required"
            );
        }

        if (
                sessionCount < 0
                || expectedStudentCount < 0
                || recordedAttendanceCount < 0
                || unrecordedCount < 0
                || presentCount < 0
                || absentCount < 0
                || lateCount < 0
                || excusedAbsenceCount < 0
                || unexcusedAbsenceCount < 0
                || medicalAbsenceCount < 0
                || schoolActivityCount < 0
                || remoteLearningCount < 0
                || earlyDepartureCount < 0
                || suspendedCount < 0
                || notRequiredCount < 0
                || unknownCount < 0
        ) {
            throw new IllegalArgumentException(
                    "Attendance counts must not be negative"
            );
        }
    }

    public boolean registerStarted() {
        return sessionCount > 0;
    }

    public boolean fullyRecorded() {
        return sessionCount > 0
                && recordedAttendanceCount >= expectedStudentCount;
    }
}
