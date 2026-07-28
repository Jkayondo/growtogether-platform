package africa.growtogether.platform.school.academic.progress;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface LearnerProgressRepository
        extends JpaRepository<LearnerProgress, UUID> {


    List<LearnerProgress>
    findByTenantIdAndLearnerIdOrderByAssessmentDateAsc(
            UUID tenantId,
            UUID learnerId
    );


    List<LearnerProgress>
    findByTenantIdAndLearningOutcomeIdOrderByAssessmentDateAsc(
            UUID tenantId,
            UUID learningOutcomeId
    );


    List<LearnerProgress>
    findByTenantIdAndAssessmentIdOrderByAssessmentDateAsc(
            UUID tenantId,
            UUID assessmentId
    );


    List<LearnerProgress>
    findByTenantIdAndAchievementStatusOrderByAssessmentDateAsc(
            UUID tenantId,
            String achievementStatus
    );


    List<LearnerProgress>
    findByTenantIdAndStatusOrderByAssessmentDateAsc(
            UUID tenantId,
            String status
    );

}	
