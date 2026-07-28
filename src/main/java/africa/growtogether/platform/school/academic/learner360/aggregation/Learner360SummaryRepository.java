package africa.growtogether.platform.school.academic.learner360.aggregation;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface Learner360SummaryRepository
        extends JpaRepository<Learner360Summary, UUID> {


    Optional<Learner360Summary>
    findByTenantIdAndLearnerId(
            UUID tenantId,
            UUID learnerId
    );


    List<Learner360Summary>
    findByTenantIdAndRiskLevel(
            UUID tenantId,
            String riskLevel
    );

}
