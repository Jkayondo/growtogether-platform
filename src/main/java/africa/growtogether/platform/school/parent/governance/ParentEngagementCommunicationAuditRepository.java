package africa.growtogether.platform.school.parent.governance;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentEngagementCommunicationAuditRepository
        extends JpaRepository<ParentEngagementCommunicationAudit, UUID> {


    List<ParentEngagementCommunicationAudit>
    findByTenantId(UUID tenantId);


    List<ParentEngagementCommunicationAudit>
    findByRecipientId(UUID recipientId);
}
