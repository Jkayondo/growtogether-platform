package africa.growtogether.platform.school.academic.learner360.support;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class LearnerSupportPlanService {

    private final LearnerSupportPlanRepository repository;

    public LearnerSupportPlanService(
            LearnerSupportPlanRepository repository
    ) {
        this.repository = repository;
    }


    public LearnerSupportPlan getPlan(UUID id) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Support plan not found"
                        )
                );
    }


    public List<LearnerSupportPlan> getPlansForLearner(
            UUID learnerId
    ) {

        return repository.findByLearnerId(learnerId);
    }


    public LearnerSupportPlan changeStatus(
            UUID id,
            String status
    ) {

        LearnerSupportPlan plan = getPlan(id);

        plan.setSupportStatus(status);

        return repository.save(plan);
    }
}
