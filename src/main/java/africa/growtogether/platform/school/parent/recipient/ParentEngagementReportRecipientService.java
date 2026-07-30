package africa.growtogether.platform.school.parent.recipient;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ParentEngagementReportRecipientService {


    private final ParentEngagementReportRecipientRepository repository;


    public ParentEngagementReportRecipientService(
            ParentEngagementReportRecipientRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementReportRecipient create(
            ParentEngagementReportRecipient recipient
    ) {

        return repository.save(recipient);
    }


    public List<ParentEngagementReportRecipient> findByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId);
    }


    public List<ParentEngagementReportRecipient> findActiveRecipients(
            UUID tenantId
    ) {

        return repository.findByTenantIdAndEnabledTrue(tenantId);
    }


    public void disable(
            ParentEngagementReportRecipient recipient
    ) {

        recipient.disable();

        repository.save(recipient);
    }
}
