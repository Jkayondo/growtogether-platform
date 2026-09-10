package africa.growtogether.platform.school.grading;


import africa.growtogether.platform.school.reportcard.result.ReportCardGenerationIntegrationService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class GradingReportCardMapperService {


    public List<ReportCardGenerationIntegrationService.StudentSubjectResultData> map(

            List<GradeCalculationResult> results

    ) {


        return results.stream()

                .map(result ->
                        
                        new ReportCardGenerationIntegrationService.StudentSubjectResultData(

                                UUID.randomUUID(),

                                null,

                                result.getScore().doubleValue(),

                                result.getGradeCode(),

                                result.getGradeName(),

                                result.getGradePoint().doubleValue()

                        )

                )

                .toList();

    }

}
