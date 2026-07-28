package africa.growtogether.platform.school.academic.progress;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class LearnerProgressService {


    private final LearnerProgressRepository repository;


    public LearnerProgressService(
            LearnerProgressRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public LearnerProgress create(
            UUID tenantId,
            UUID learnerId,
            UUID assessmentId,
            UUID learningOutcomeId,
            Double score,
            Double maximumScore
    ) {


        if (score == null || maximumScore == null) {
            throw new IllegalArgumentException(
                    "Score values are required."
            );
        }


        if (score < 0 || maximumScore <= 0) {
            throw new IllegalArgumentException(
                    "Invalid score values."
            );
        }


        LearnerProgress progress =
                new LearnerProgress(
                        learnerId,
                        assessmentId,
                        learningOutcomeId
                );


        progress.setTenantId(
                tenantId
        );


        double percentage =
                (score / maximumScore) * 100;


        progress.recordScore(
                score,
                maximumScore
        );


        progress.updateAchievementStatus(
                determineAchievementStatus(
                        percentage
                )
        );


        return repository.save(
                progress
        );
    }


    @Transactional(readOnly = true)
    public List<LearnerProgress> findByLearner(
            UUID tenantId,
            UUID learnerId
    ) {

        return repository
                .findByTenantIdAndLearnerIdOrderByAssessmentDateAsc(
                        tenantId,
                        learnerId
                );
    }


    @Transactional(readOnly = true)
    public List<LearnerProgress> findByOutcome(
            UUID tenantId,
            UUID learningOutcomeId
    ) {

        return repository
                .findByTenantIdAndLearningOutcomeIdOrderByAssessmentDateAsc(
                        tenantId,
                        learningOutcomeId
                );
    }


    private String determineAchievementStatus(
            double percentage
    ) {

        if (percentage < 50) {
            return "NEEDS_SUPPORT";
        }


        if (percentage < 80) {
            return "IN_PROGRESS";
        }


        return "ACHIEVED";
    }


    @Transactional
    public LearnerProgress archive(
            LearnerProgress progress
    ) {

        progress.archive();

        return repository.save(
                progress
        );
    }

}
