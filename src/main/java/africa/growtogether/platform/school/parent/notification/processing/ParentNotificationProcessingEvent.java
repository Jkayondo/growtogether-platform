package africa.growtogether.platform.school.parent.notification.processing;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "parent_notification_processing_events")
public class ParentNotificationProcessingEvent
        extends AuditedTenantEntity {


    @Column(
            name = "notification_id",
            nullable = false
    )
    private UUID notificationId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "processing_status",
            nullable = false,
            length = 30
    )
    private ParentNotificationProcessingStatus processingStatus;


    @Column(
            name = "processed_at"
    )
    private Instant processedAt;


    protected ParentNotificationProcessingEvent() {
    }


    public ParentNotificationProcessingEvent(
            UUID tenantId,
            UUID notificationId
    ) {

        setTenantId(tenantId);

        this.notificationId = notificationId;
        this.processingStatus =
                ParentNotificationProcessingStatus.RECEIVED;
    }


    public void startProcessing() {

        this.processingStatus =
                ParentNotificationProcessingStatus.PROCESSING;
    }


    public void complete() {

        this.processingStatus =
                ParentNotificationProcessingStatus.COMPLETED;

        this.processedAt = Instant.now();
    }


    public void fail() {

        this.processingStatus =
                ParentNotificationProcessingStatus.FAILED;

        this.processedAt = Instant.now();
    }


    public UUID getNotificationId() {
        return notificationId;
    }


    public ParentNotificationProcessingStatus getProcessingStatus() {
        return processingStatus;
    }
}
