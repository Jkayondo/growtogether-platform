package africa.growtogether.platform.school.parent.governance.security.event;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentEngagementGovernanceSecurityEventRepository
        extends JpaRepository<ParentEngagementGovernanceSecurityEvent, UUID> {


    List<ParentEngagementGovernanceSecurityEvent>
    findByTenantId(UUID tenantId);


    List<ParentEngagementGovernanceSecurityEvent>
    findByUserId(UUID userId);
}
