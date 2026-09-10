package africa.growtogether.platform.school.academic.progression;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface PromotionDecisionRepository
        extends JpaRepository<PromotionDecision, UUID> {


    List<PromotionDecision> findByTenantId(
            UUID tenantId
    );


    List<PromotionDecision> findByTenantIdAndLearnerId(
            UUID tenantId,
            UUID learnerId
    );


    List<PromotionDecision> findByTenantIdAndDecisionStatus(
            UUID tenantId,
            String decisionStatus
    );


}
