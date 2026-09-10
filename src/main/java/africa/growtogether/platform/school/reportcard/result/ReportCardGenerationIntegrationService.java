package africa.growtogether.platform.school.reportcard.result;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class ReportCardGenerationIntegrationService {


    private final ReportCardResultLineService resultLineService;


    public ReportCardGenerationIntegrationService(
            ReportCardResultLineService resultLineService
    ){

        this.resultLineService = resultLineService;

    }



    public void generateSubjectResults(
            UUID tenantId,
            UUID reportCardId,
            List<StudentSubjectResultData> results
    ){


        int sequence = 1;


        for(StudentSubjectResultData result : results){


            ReportCardResultLine line =
                    new ReportCardResultLine(
                            reportCardId,
                            result.studentSubjectResultId(),
                            result.subjectName(),
                            result.finalScore(),
                            result.gradeCode(),
                            result.gradeName(),
                            result.gradePoint(),
                            sequence
                    );


            resultLineService.addResultLine(
                    tenantId,
                    line
            );


            sequence++;

        }

    }



    public record StudentSubjectResultData(

            UUID studentSubjectResultId,

            String subjectName,

            Double finalScore,

            String gradeCode,

            String gradeName,

            Double gradePoint

    ){

    }

}
