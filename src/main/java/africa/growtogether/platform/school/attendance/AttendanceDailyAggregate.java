package africa.growtogether.platform.school.attendance;

public interface AttendanceDailyAggregate {

    long getSessionCount();

    long getExpectedStudentCount();

    long getRecordedAttendanceCount();

    long getPresentCount();

    long getAbsentCount();

    long getLateCount();

    long getExcusedAbsenceCount();

    long getUnexcusedAbsenceCount();

    long getMedicalAbsenceCount();

    long getSchoolActivityCount();

    long getRemoteLearningCount();

    long getEarlyDepartureCount();

    long getSuspendedCount();

    long getNotRequiredCount();

    long getUnknownCount();
}
