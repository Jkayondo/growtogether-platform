package africa.growtogether.platform.school.assessment.marking;

import java.math.BigDecimal;
import java.util.UUID;


public record CreateMarkSheetCommand(

        String markSheetReference,

        UUID assessmentComponentId,

        UUID assessmentPaperId,

        UUID examinationScheduleId,

        UUID subjectOfferingId,

        UUID classOfferingId,

        UUID streamId,

        UUID teacherProfileId,

        BigDecimal maximumScore,

        BigDecimal passScore

) {
}
