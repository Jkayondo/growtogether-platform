package africa.growtogether.platform.school.parent.governance.security.event;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ParentEngagementGovernanceSecurityEventService {


    private final ParentEngagementGovernanceSecurityEventRepository repository;


    public ParentEngagementGovernanceSecurityEventService(
            ParentEngagementGovernanceSecurityEventRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementGovernanceSecurityEvent record(
            ParentEngagementGovernanceSecurityEvent event
    ) {

        return repository.save(event);
    }


    public List<ParentEngagementGovernanceSecurityEvent> findByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId);
    }


    public List<ParentEngagementGovernanceSecurityEvent> findByUser(
            UUID userId
    ) {

        return repository.findByUserId(userId);
    }
}
