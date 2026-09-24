package africa.growtogether.platform.school.teacher.workspace.api;


import java.util.List;
import java.util.UUID;


public record TeacherWorkspaceResponse(

        UUID teacherProfileId,

        String teacherName,

        AcademicPeriodSummary academicPeriod,

        AssignmentSummary assignments,

        CoverageSummary coverage,

        AssessmentSummary assessments,

        CalendarSummary calendar,

        ConnectSummary connect

) {


    public record AcademicPeriodSummary(

            UUID academicYearId,

            UUID academicTermId,

            String termName

    ) {}


    public record AssignmentSummary(

            int totalAssignments,

            int totalSubjects,

            int totalClasses

    ) {}


    public record CoverageSummary(

            int totalTopics,

            int completed,

            int inProgress,

            int requiresRemediation,

            int aheadOfSchedule

    ) {}


    public record AssessmentSummary(

            int pendingAssessments,

            int pendingMarking

    ) {}


    public record CalendarSummary(

            int upcomingEvents

    ) {}


    public record ConnectSummary(

            int unreadMessages

    ) {}

}
