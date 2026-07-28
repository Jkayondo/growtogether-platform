package africa.growtogether.platform.school.academic.learner360.rules;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface PerformanceRuleRepository
        extends JpaRepository<PerformanceRule, UUID> {


    List<PerformanceRule> findByTenantIdOrderByMinimumScoreAsc(
            UUID tenantId
    );

}
