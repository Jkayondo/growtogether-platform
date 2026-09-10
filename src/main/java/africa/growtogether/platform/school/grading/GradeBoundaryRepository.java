package africa.growtogether.platform.school.grading;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface GradeBoundaryRepository
        extends JpaRepository<GradeBoundary, UUID> {


    List<GradeBoundary>
    findByTenantIdAndGradingSchemeIdOrderByMinimumScoreDesc(
            UUID tenantId,
            UUID gradingSchemeId
    );


    List<GradeBoundary>
    findByTenantIdAndGradingSchemeIdAndStatusOrderByMinimumScoreDesc(
            UUID tenantId,
            UUID gradingSchemeId,
            String status
    );

}
