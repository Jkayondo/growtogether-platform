package africa.growtogether.platform.school.academic.learner360.intelligence;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface LearnerIntelligenceSnapshotRepository
        extends JpaRepository<LearnerIntelligenceSnapshot, UUID> {


    Optional<LearnerIntelligenceSnapshot>
    findByTenantIdAndLearnerId(
            UUID tenantId,
            UUID learnerId
    );

}
