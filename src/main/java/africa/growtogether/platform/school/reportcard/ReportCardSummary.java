package africa.growtogether.platform.school.reportcard;


import java.util.List;
import java.util.UUID;


public record ReportCardSummary(

        UUID learnerId,

        UUID academicPeriodId,

        List<SubjectPerformance> subjects,

        String overallComment

) {


    public record SubjectPerformance(

            UUID subjectConfigurationId,

            Integer score,

            String gradeValue

    ) {
    }
}
