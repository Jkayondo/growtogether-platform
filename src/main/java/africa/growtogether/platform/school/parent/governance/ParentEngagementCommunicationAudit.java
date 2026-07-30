package africa.growtogether.platform.school.parent.governance;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "parent_engagement_communication_audits")
public class ParentEngagementCommunicationAudit
        extends AuditedTenantEntity {


    @Column(name = "recipient_id",
            nullable = false)
    private UUID recipientId;


    @Column(name = "report_id")
    private UUID reportId;


    @Enumerated(EnumType.STRING)
    @Column(name = "action",
            nullable = false,
            length = 40)
    private ParentEngagementCommunicationAuditAction action;


    protected ParentEngagementCommunicationAudit() {
    }


    public ParentEngagementCommunicationAudit(
            UUID tenantId,
            UUID recipientId,
            UUID reportId,
            ParentEngagementCommunicationAuditAction action
    ) {

        setTenantId(tenantId);

        this.recipientId = recipientId;
        this.reportId = reportId;
        this.action = action;
    }


    public UUID getRecipientId() {

        return recipientId;
    }


    public UUID getReportId() {

        return reportId;
    }


    public ParentEngagementCommunicationAuditAction getAction() {

        return action;
    }
}
