package africa.growtogether.platform.school.parent.privacy;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ParentEngagementPrivacyDecisionAuditService {


    private final ParentEngagementPrivacyDecisionAuditRepository repository;


    public ParentEngagementPrivacyDecisionAuditService(
            ParentEngagementPrivacyDecisionAuditRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementPrivacyDecisionAudit record(
            ParentEngagementPrivacyDecisionAudit audit
    ) {

        return repository.save(audit);
    }


    public List<ParentEngagementPrivacyDecisionAudit> findByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId);
    }


    public List<ParentEngagementPrivacyDecisionAudit> findByParent(
            UUID parentId
    ) {

        return repository.findByParentId(parentId);
    }
}
