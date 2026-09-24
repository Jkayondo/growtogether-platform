package africa.growtogether.platform.school.leadership;


import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Read-only Leadership overview.
 *
 * A capability marked PENDING_AGGREGATION intentionally carries no invented
 * institutional value. Authoritative domain integration must be added before
 * that capability is reported as AVAILABLE.
 */
public record LeadershipOverviewResponse(
        UUID tenantId,
        Instant asOf,
        Instant eventWindowEnd,
        int activeVisitors,
        int upcomingEvents,
        CoverageSummary coverage,
        ParentEngagementSummary parentEngagement,
        LearnerSummary learners,
        TeacherSummary teachers,

        AttendanceSummary attendance,
        List<CapabilityState> capabilities
) {

    public LeadershipOverviewResponse {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(asOf, "asOf");
        Objects.requireNonNull(eventWindowEnd, "eventWindowEnd");
        Objects.requireNonNull(coverage, "coverage");
        Objects.requireNonNull(parentEngagement, "parentEngagement");
        Objects.requireNonNull(learners, "learners");
        Objects.requireNonNull(teachers, "teachers");
        capabilities = List.copyOf(
                Objects.requireNonNull(capabilities, "capabilities")
        );
    }

    public record CoverageSummary(
            long totalItems,
            long notStarted,
            long inProgress,
            long completed,
            long requiresRemediation,
            long aheadOfSchedule
    ) {
    }

    public record ParentEngagementSummary(
            long totalNotifications,
            long deliveredNotifications,
            long viewedNotifications,
            long acknowledgedNotifications
    ) {
    }

    public record LearnerSummary(
            long activeLearnerRecords,
            long activeEnrollments
    ) {
    }

    public record TeacherSummary(
            long activeTeacherProfiles,
            long activeTeachingAssignments
    ) {
    }

    public record AttendanceSummary(
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
            long unknownCount,
            boolean registerStarted,
            boolean fullyRecorded
    ) {
    }


    public record CapabilityState(
            String code,
            String status,
            String source
    ) {
        public CapabilityState {
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(source, "source");
        }
    }
}
