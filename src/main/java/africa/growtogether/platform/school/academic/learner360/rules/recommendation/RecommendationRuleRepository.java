package africa.growtogether.platform.school.academic.learner360.rules.recommendation;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface RecommendationRuleRepository
        extends JpaRepository<RecommendationRule, UUID> {


    List<RecommendationRule> findByTenantIdOrderByRiskLevelAsc(
            UUID tenantId
    );


}
