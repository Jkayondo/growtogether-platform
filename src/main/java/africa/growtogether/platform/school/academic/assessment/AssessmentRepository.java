package africa.growtogether.platform.school.academic.assessment;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface AssessmentRepository
        extends JpaRepository<Assessment, UUID> {


    List<Assessment> findByTenantIdAndLearningOutcomeIdOrderByAssessmentDateAsc(
            UUID tenantId,
            UUID learningOutcomeId
    );


    List<Assessment> findByTenantIdAndAssessmentTypeOrderByAssessmentDateAsc(
            UUID tenantId,
            String assessmentType
    );


    List<Assessment> findByTenantIdAndStatusOrderByAssessmentDateAsc(
            UUID tenantId,
            String status
    );


    Optional<Assessment> findByTenantIdAndLearningOutcomeIdAndAssessmentCode(
            UUID tenantId,
            UUID learningOutcomeId,
            String assessmentCode
    );

}
