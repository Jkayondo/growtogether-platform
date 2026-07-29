package africa.growtogether.platform.school.academic.learner360.support;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearnerSupportPlanRepository
        extends JpaRepository<LearnerSupportPlan, UUID> {

    List<LearnerSupportPlan> findByLearnerId(UUID learnerId);

    List<LearnerSupportPlan> findByAssignedStaffId(UUID assignedStaffId);

    List<LearnerSupportPlan> findBySupportStatus(String supportStatus);
}
