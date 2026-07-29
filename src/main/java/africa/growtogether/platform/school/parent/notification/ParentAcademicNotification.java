package africa.growtogether.platform.school.parent.notification;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
        name = "parent_academic_notifications",
        indexes = {
                @Index(
                        name = "ix_parent_academic_notification_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class ParentAcademicNotification
        extends AuditedTenantEntity {


    @Column(
            name = "parent_id",
            nullable = false
    )
    private UUID parentId;


    @Column(
            name = "learner_id"
    )
    private UUID learnerId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "notification_type",
            nullable = false,
            length = 50
    )
    private ParentAcademicNotificationType notificationType;


    @Column(
            name = "message",
            nullable = false,
            length = 500
    )
    private String message;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected ParentAcademicNotification() {
    }


    public ParentAcademicNotification(
            UUID tenantId,
            UUID parentId,
            UUID learnerId,
            ParentAcademicNotificationType notificationType,
            String message
    ) {

        setTenantId(tenantId);

        this.parentId = parentId;
        this.learnerId = learnerId;
        this.notificationType = notificationType;
        this.message = message;
        this.createdAt = Instant.now();
    }


    public UUID getParentId() {
        return parentId;
    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public ParentAcademicNotificationType getNotificationType() {
        return notificationType;
    }


    public String getMessage() {
        return message;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }
}
