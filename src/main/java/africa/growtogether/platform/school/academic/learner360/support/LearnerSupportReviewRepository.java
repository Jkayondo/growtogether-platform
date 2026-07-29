package africa.growtogether.platform.school.academic.learner360.support;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearnerSupportReviewRepository
        extends JpaRepository<LearnerSupportReview, UUID> {

    List<LearnerSupportReview> findBySupportPlanIdOrderByReviewDateDesc(
            UUID supportPlanId
    );
}
