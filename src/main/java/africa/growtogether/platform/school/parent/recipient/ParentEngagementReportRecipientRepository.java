package africa.growtogether.platform.school.parent.recipient;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentEngagementReportRecipientRepository
        extends JpaRepository<ParentEngagementReportRecipient, UUID> {


    List<ParentEngagementReportRecipient>
    findByTenantId(UUID tenantId);


    List<ParentEngagementReportRecipient>
    findByTenantIdAndEnabledTrue(UUID tenantId);
}
