package africa.growtogether.platform.school.academic.progression;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface PromotionRuleRepository
        extends JpaRepository<PromotionRule, UUID> {


    Optional<PromotionRule> findByTenantIdAndRuleCode(
            UUID tenantId,
            String ruleCode
    );


    List<PromotionRule> findByTenantIdAndRuleStatus(
            UUID tenantId,
            String ruleStatus
    );


}
