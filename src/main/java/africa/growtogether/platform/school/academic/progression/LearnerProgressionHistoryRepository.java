package africa.growtogether.platform.school.academic.progression;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;


@Repository
public interface LearnerProgressionHistoryRepository
        extends JpaRepository<LearnerProgressionHistory, UUID> {


    List<LearnerProgressionHistory>
    findByTenantIdAndLearnerIdOrderByEffectiveDateDesc(
            UUID tenantId,
            UUID learnerId
    );


    List<LearnerProgressionHistory>
    findByTenantIdAndAcademicYearId(
            UUID tenantId,
            UUID academicYearId
    );


    List<LearnerProgressionHistory>
    findByTenantIdAndPromotionDecisionId(
            UUID tenantId,
            UUID promotionDecisionId
    );

}
