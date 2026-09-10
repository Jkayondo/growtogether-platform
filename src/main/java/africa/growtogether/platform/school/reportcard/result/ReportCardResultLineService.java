package africa.growtogether.platform.school.reportcard.result;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class ReportCardResultLineService {


    private final ReportCardResultLineRepository repository;


    public ReportCardResultLineService(
            ReportCardResultLineRepository repository
    ){

        this.repository = repository;

    }


    public ReportCardResultLine addResultLine(
            UUID tenantId,
            ReportCardResultLine resultLine
    ){

        if (
            repository.existsByTenantIdAndReportCardIdAndStudentSubjectResultId(
                    tenantId,
                    resultLine.getReportCardId(),
                    resultLine.getStudentSubjectResultId()
            )
        ){

            throw new IllegalArgumentException(
                    "Result already attached to report card"
            );

        }


        return repository.save(resultLine);

    }



    @Transactional(readOnly = true)
    public List<ReportCardResultLine> getReportCardResults(
            UUID tenantId,
            UUID reportCardId
    ){

        return repository
                .findByTenantIdAndReportCardIdOrderBySequenceNumberAsc(
                        tenantId,
                        reportCardId
                );

    }


}
