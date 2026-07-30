package africa.growtogether.platform.school.parent.recipient;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "parent_engagement_report_recipients")
public class ParentEngagementReportRecipient
        extends AuditedTenantEntity {


    @Column(name = "user_id",
            nullable = false)
    private UUID userId;


    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type",
            nullable = false,
            length = 40)
    private ParentEngagementRecipientType recipientType;


    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel",
            nullable = false,
            length = 40)
    private ParentEngagementDeliveryChannel deliveryChannel;


    @Column(name = "enabled",
            nullable = false)
    private boolean enabled;


    protected ParentEngagementReportRecipient() {
    }


    public ParentEngagementReportRecipient(
            UUID tenantId,
            UUID userId,
            ParentEngagementRecipientType recipientType,
            ParentEngagementDeliveryChannel deliveryChannel
    ) {

        setTenantId(tenantId);

        this.userId = userId;
        this.recipientType = recipientType;
        this.deliveryChannel = deliveryChannel;
        this.enabled = true;
    }


    public void disable() {

        this.enabled = false;
    }


    public void enable() {

        this.enabled = true;
    }


    public boolean isEnabled() {

        return enabled;
    }


    public UUID getUserId() {

        return userId;
    }


    public ParentEngagementRecipientType getRecipientType() {

        return recipientType;
    }


    public ParentEngagementDeliveryChannel getDeliveryChannel() {

        return deliveryChannel;
    }
}
