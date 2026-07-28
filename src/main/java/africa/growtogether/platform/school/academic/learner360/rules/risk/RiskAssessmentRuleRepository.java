package africa.growtogether.platform.school.academic.learner360.rules.risk;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface RiskAssessmentRuleRepository
        extends JpaRepository<RiskAssessmentRule, UUID> {


    List<RiskAssessmentRule> findByTenantIdOrderByMinimumScoreAsc(
            UUID tenantId
    );

}
