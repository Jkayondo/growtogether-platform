package africa.growtogether.platform.school.learner;

import java.util.UUID;

public record Learner360View(

        UUID learnerId,

        String permanentLearnerNumber,

        String studentNumber,

        String firstName,

        String middleName,

        String lastName,

        String preferredName,

        String studentStatus,

        GuardianSummary guardian,

        AcademicSummary academic,

        AttendanceSummary attendance,

        WelfareSummary welfare,

        FinanceSummary finance,

        HealthSummary health

) {


    public record GuardianSummary(

            int totalGuardians,

            String primaryGuardianName,

            boolean emergencyContactAvailable

    ) {
    }


    public record AcademicSummary(

            String currentClass,

            String currentAcademicYear,

            String academicStatus

    ) {
    }


    public record AttendanceSummary(

            double attendancePercentage,

            int daysAbsent,

            int lateArrivals

    ) {
    }


    public record WelfareSummary(

            int incidents,

            boolean supportPlanActive

    ) {
    }


    public record FinanceSummary(

            String feeStatus,

            double outstandingBalance

    ) {
    }


    public record HealthSummary(

            boolean medicalProfileAvailable,

            boolean specialNeedsSupport

    ) {
    }
}
