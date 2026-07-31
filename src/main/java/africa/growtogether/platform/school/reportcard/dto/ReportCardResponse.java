package africa.growtogether.platform.school.reportcard.dto;


import java.util.UUID;


public record ReportCardResponse(

        UUID id,

        UUID learnerId,

        UUID academicPeriodId,

        String overallComment

) {
}
