package africa.growtogether.platform.school.assessment.examination.paper;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
public class AssessmentPaperService {


    private final AssessmentPaperRepository repository;


    public AssessmentPaperService(
            AssessmentPaperRepository repository
    ) {

        this.repository = repository;

    }


    public AssessmentPaper create(
            AssessmentPaper paper
    ) {

        return repository.save(paper);

    }


    @Transactional(readOnly = true)
    public AssessmentPaper get(
            UUID id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Assessment paper not found"
                        )
                );

    }


    @Transactional(readOnly = true)
    public AssessmentPaper getByCode(
            UUID tenantId,
            String paperCode
    ) {

        return repository
                .findByTenantIdAndPaperCode(
                        tenantId,
                        paperCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Assessment paper not found"
                        )
                );

    }


    public AssessmentPaper submitForReview(
            UUID id
    ) {

        AssessmentPaper paper = get(id);

        paper.submitForReview();

        return paper;

    }


    public AssessmentPaper moderate(
            UUID id
    ) {

        AssessmentPaper paper = get(id);

        paper.moderate();

        return paper;

    }


    public AssessmentPaper approve(
            UUID id
    ) {

        AssessmentPaper paper = get(id);

        paper.approve();

        return paper;

    }


    public AssessmentPaper schedule(
            UUID id
    ) {

        AssessmentPaper paper = get(id);

        paper.schedule();

        return paper;

    }


    public AssessmentPaper complete(
            UUID id
    ) {

        AssessmentPaper paper = get(id);

        paper.complete();

        return paper;

    }

}
