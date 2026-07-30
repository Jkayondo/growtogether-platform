package africa.growtogether.platform.school.parent.privacy;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "parent_engagement_privacy_decision_audits")
public class ParentEngagementPrivacyDecisionAudit
        extends AuditedTenantEntity {


    @Column(name = "parent_id",
            nullable = false)
    private UUID parentId;


    @Column(name = "communication_id")
    private UUID communicationId;


    @Enumerated(EnumType.STRING)
    @Column(name = "decision",
            nullable = false,
            length = 40)
    private ParentEngagementPrivacyDecisionType decision;


    protected ParentEngagementPrivacyDecisionAudit() {
    }


    public ParentEngagementPrivacyDecisionAudit(
            UUID tenantId,
            UUID parentId,
            UUID communicationId,
            ParentEngagementPrivacyDecisionType decision
    ) {

        setTenantId(tenantId);

        this.parentId = parentId;
        this.communicationId = communicationId;
        this.decision = decision;
    }


    public UUID getParentId() {

        return parentId;
    }


    public UUID getCommunicationId() {

        return communicationId;
    }


    public ParentEngagementPrivacyDecisionType getDecision() {

        return decision;
    }
}
