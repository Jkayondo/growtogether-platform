package africa.growtogether.platform.school.timetable.generation;

import java.util.List;
import java.util.UUID;

public record TimetableGenerationCandidate(

        UUID generationRequestId,

        String candidateStatus,

        int requiredPeriods,

        int placedPeriods,

        List<Placement> placements,

        List<UnplacedDemand> unplaced

) {

    public record Placement(

            String dayOfWeek,

            UUID bellPeriodId,

            String periodCode,

            UUID classOfferingId,

            UUID subjectOfferingId,

            UUID classGradeId,

            UUID streamId,

            UUID subjectId,

            UUID teachingAssignmentId,

            UUID teacherProfileId,

            UUID schedulingResourceId,

            String placementReason

    ) {
    }

    public record UnplacedDemand(

            UUID subjectOfferingId,

            String subjectOfferingCode,

            UUID classOfferingId,

            UUID streamId,

            int remainingPeriods,

            String reasonCode,

            String explanation

    ) {
    }
}
