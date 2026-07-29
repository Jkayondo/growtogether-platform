package africa.growtogether.platform.school.reportcard;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class ReportCardService {


    private final ReportCardRepository repository;


    public ReportCardService(
            ReportCardRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public ReportCard create(
            UUID tenantId,
            UUID learnerId,
            UUID academicPeriodId
    ) {

        ReportCard reportCard =
                new ReportCard(
                        tenantId,
                        learnerId,
                        academicPeriodId
                );


        return repository.save(reportCard);
    }


    @Transactional
    public ReportCard addComment(
            ReportCard reportCard,
            String comment
    ) {

        reportCard.addComment(comment);

        return repository.save(reportCard);
    }


    @Transactional(readOnly = true)
    public List<ReportCard> getLearnerReports(
            UUID learnerId
    ) {

        return repository
                .findByLearnerIdOrderByCreatedAtDesc(
                        learnerId
                );
    }


    @Transactional(readOnly = true)
    public List<ReportCard> getPeriodReports(
            UUID academicPeriodId
    ) {

        return repository.findByAcademicPeriodId(
                academicPeriodId
        );
    }


    @Transactional(readOnly = true)
    public List<ReportCard> getSchoolReports(
            UUID tenantId
    ) {

        return repository.findByTenantId(
                tenantId
        );
    }
}
