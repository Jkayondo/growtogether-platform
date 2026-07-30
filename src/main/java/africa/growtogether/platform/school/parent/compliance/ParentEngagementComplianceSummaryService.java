package africa.growtogether.platform.school.parent.compliance;


import africa.growtogether.platform.school.parent.privacy.ParentEngagementPrivacyDecisionAuditRepository;
import africa.growtogether.platform.school.parent.privacy.ParentEngagementPrivacyDecisionType;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentEngagementComplianceSummaryService {


    private final ParentEngagementPrivacyDecisionAuditRepository auditRepository;


    public ParentEngagementComplianceSummaryService(
            ParentEngagementPrivacyDecisionAuditRepository auditRepository
    ) {

        this.auditRepository = auditRepository;
    }


    public ParentEngagementComplianceSummary generate(
            UUID tenantId
    ) {


        var audits =
                auditRepository.findByTenantId(tenantId);


        long total =
                audits.size();


        long allowed =
                audits.stream()
                        .filter(
                                audit ->
                                        audit.getDecision()
                                                ==
                                                ParentEngagementPrivacyDecisionType.ALLOWED
                        )
                        .count();


        long blocked =
                total - allowed;


        return new ParentEngagementComplianceSummary(
                total,
                allowed,
                blocked,
                0,
                0
        );
    }
}
