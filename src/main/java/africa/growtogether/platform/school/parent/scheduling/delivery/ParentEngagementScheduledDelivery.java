package africa.growtogether.platform.school.parent.scheduling.delivery;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "parent_engagement_scheduled_deliveries")
public class ParentEngagementScheduledDelivery
        extends AuditedTenantEntity {


    @Column(name = "scheduled_report_id",
            nullable = false)
    private UUID scheduledReportId;


    @Enumerated(EnumType.STRING)
    @Column(name = "status",
            nullable = false,
            length = 30)
    private ParentEngagementScheduledDeliveryStatus status;


    protected ParentEngagementScheduledDelivery() {
    }


    public ParentEngagementScheduledDelivery(
            UUID tenantId,
            UUID scheduledReportId
    ) {

        setTenantId(tenantId);

        this.scheduledReportId = scheduledReportId;
        this.status =
                ParentEngagementScheduledDeliveryStatus.CREATED;
    }


    public void markGenerated() {

        this.status =
                ParentEngagementScheduledDeliveryStatus.GENERATED;
    }


    public void markSent() {

        this.status =
                ParentEngagementScheduledDeliveryStatus.SENT;
    }


    public void markFailed() {

        this.status =
                ParentEngagementScheduledDeliveryStatus.FAILED;
    }


    public ParentEngagementScheduledDeliveryStatus getDeliveryStatus() {
        return status;
    }
}
