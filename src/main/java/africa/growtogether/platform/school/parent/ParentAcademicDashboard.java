package africa.growtogether.platform.school.parent;


import java.util.List;
import java.util.UUID;


public record ParentAcademicDashboard(

        UUID parentId,

        int learnerCount,

        List<LearnerDashboardItem> learners

) {


    public record LearnerDashboardItem(

            UUID learnerId,

            int availableReports

    ) {
    }
}
