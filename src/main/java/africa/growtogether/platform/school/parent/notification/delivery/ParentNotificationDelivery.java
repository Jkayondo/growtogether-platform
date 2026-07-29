package africa.growtogether.platform.school.parent.notification.delivery;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
        name = "parent_notification_deliveries",
        indexes = {
                @Index(
                        name = "ix_parent_notification_delivery_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class ParentNotificationDelivery
        extends AuditedTenantEntity {


    @Column(
            name = "notification_id",
            nullable = false
    )
    private UUID notificationId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "delivery_status",
            nullable = false,
            length = 30
    )
    private ParentNotificationDeliveryStatus deliveryStatus;


    @Column(
            name = "channel",
            nullable = false,
            length = 30
    )
    private String channel;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected ParentNotificationDelivery() {
    }


    public ParentNotificationDelivery(
            UUID tenantId,
            UUID notificationId,
            String channel
    ) {

        setTenantId(tenantId);

        this.notificationId = notificationId;
        this.channel = channel;
        this.deliveryStatus =
                ParentNotificationDeliveryStatus.PENDING;
        this.createdAt = Instant.now();
    }


    public UUID getNotificationId() {
        return notificationId;
    }


    public ParentNotificationDeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }


    public String getChannel() {
        return channel;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }


    public void markProcessing() {

        this.deliveryStatus =
                ParentNotificationDeliveryStatus.PROCESSING;
    }


    public void markSent() {

        this.deliveryStatus =
                ParentNotificationDeliveryStatus.SENT;
    }


    public void markFailed() {

        this.deliveryStatus =
                ParentNotificationDeliveryStatus.FAILED;
    }
}
