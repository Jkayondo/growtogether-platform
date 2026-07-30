package africa.growtogether.platform.school.parent.governance;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ParentEngagementCommunicationAuditService {


    private final ParentEngagementCommunicationAuditRepository repository;


    public ParentEngagementCommunicationAuditService(
            ParentEngagementCommunicationAuditRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementCommunicationAudit record(
            ParentEngagementCommunicationAudit audit
    ) {

        return repository.save(audit);
    }


    public List<ParentEngagementCommunicationAudit> findByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId);
    }


    public List<ParentEngagementCommunicationAudit> findByRecipient(
            UUID recipientId
    ) {

        return repository.findByRecipientId(recipientId);
    }
}
