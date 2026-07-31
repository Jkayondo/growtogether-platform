package africa.growtogether.platform.school.reportcard.dto;


import java.util.UUID;


public record CreateReportCardRequest(

        UUID learnerId,

        UUID academicPeriodId

) {
}
