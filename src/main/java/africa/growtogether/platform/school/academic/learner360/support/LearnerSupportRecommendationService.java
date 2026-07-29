package africa.growtogether.platform.school.academic.learner360.support;

import africa.growtogether.platform.school.academic.learner360.intelligence.LearnerIntelligenceSnapshot;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;


@Service
public class LearnerSupportRecommendationService {


    private final LearnerSupportPlanRepository repository;


    public LearnerSupportRecommendationService(
            LearnerSupportPlanRepository repository
    ) {
        this.repository = repository;
    }


    public LearnerSupportPlan createSupportPlanFromSnapshot(
            LearnerIntelligenceSnapshot snapshot
    ) {

        if (!Boolean.TRUE.equals(snapshot.getSupportRequired())) {
            throw new IllegalStateException(
                    "Learner does not require support."
            );
        }


        LearnerSupportPlan plan =
                new LearnerSupportPlan(
                        snapshot.getLearnerId(),
                        snapshot.getId(),
                        snapshot.getRiskLevel(),
                        "Learner intelligence identified support requirement.",
                        snapshot.getRecommendationSummary(),
                        null,
                        LocalDate.now().plusMonths(1)
                );


        return repository.save(plan);
    }

}
