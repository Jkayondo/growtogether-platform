package africa.growtogether.platform.school.parent.privacy;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentEngagementPrivacyDecisionAuditRepository
        extends JpaRepository<ParentEngagementPrivacyDecisionAudit, UUID> {


    List<ParentEngagementPrivacyDecisionAudit>
    findByTenantId(UUID tenantId);


    List<ParentEngagementPrivacyDecisionAudit>
    findByParentId(UUID parentId);
}
