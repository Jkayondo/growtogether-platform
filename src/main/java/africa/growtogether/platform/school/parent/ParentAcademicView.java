package africa.growtogether.platform.school.parent;


import java.util.List;
import java.util.UUID;


public record ParentAcademicView(

        UUID parentId,

        List<LearnerAcademicSummary> learners

) {


    public record LearnerAcademicSummary(

            UUID learnerId,

            List<UUID> reportCardIds

    ) {
    }
}
