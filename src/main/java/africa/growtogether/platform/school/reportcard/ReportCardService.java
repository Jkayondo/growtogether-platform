package africa.growtogether.platform.school.reportcard;


import africa.growtogether.platform.school.reportcard.dto.CreateReportCardRequest;
import africa.growtogether.platform.school.reportcard.dto.ReportCardResponse;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ReportCardService {


    private final ReportCardRepository repository;


    public ReportCardService(
            ReportCardRepository repository
    ) {

        this.repository = repository;
    }


    public ReportCardResponse create(
            UUID tenantId,
            CreateReportCardRequest request
    ) {


        ReportCard reportCard =
                new ReportCard(
                        tenantId,
                        request.learnerId(),
                        request.academicPeriodId()
                );


        ReportCard saved =
                repository.save(reportCard);


        return map(saved);
    }


    private ReportCardResponse map(
            ReportCard reportCard
    ) {

        return new ReportCardResponse(
                reportCard.getId(),
                reportCard.getLearnerId(),
                reportCard.getAcademicPeriodId(),
                reportCard.getOverallComment()
        );
    }
}
