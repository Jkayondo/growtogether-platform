package africa.growtogether.platform.school.academic.learner360.support;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class LearnerSupportReviewService {

    private final LearnerSupportReviewRepository repository;


    public LearnerSupportReviewService(
            LearnerSupportReviewRepository repository
    ) {
        this.repository = repository;
    }


    public LearnerSupportReview createReview(
            LearnerSupportReview review
    ) {
        return repository.save(review);
    }


    public LearnerSupportReview getReview(
            UUID id
    ) {
        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Learner support review not found"
                        )
                );
    }


    public List<LearnerSupportReview> getReviewsForPlan(
            UUID supportPlanId
    ) {
        return repository
                .findBySupportPlanIdOrderByReviewDateDesc(
                        supportPlanId
                );
    }
}
