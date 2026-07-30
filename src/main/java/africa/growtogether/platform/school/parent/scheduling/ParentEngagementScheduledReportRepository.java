package africa.growtogether.platform.school.parent.scheduling;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentEngagementScheduledReportRepository
        extends JpaRepository<ParentEngagementScheduledReport, UUID> {


    List<ParentEngagementScheduledReport>
    findByTenantId(
            UUID tenantId
    );


    List<ParentEngagementScheduledReport>
    findByEnabledTrue();
}
