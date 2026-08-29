package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "gt_connect_message_receipts",
        indexes = {
                @Index(
                        name = "ix_gt_connect_receipts_tenant_message",
                        columnList = "tenant_id,message_id"
                ),
                @Index(
                        name = "ix_gt_connect_receipts_tenant_user",
                        columnList = "tenant_id,user_id"
                ),
                @Index(
                        name = "ix_gt_connect_receipts_user_unread",
                        columnList = "tenant_id,user_id,read_at"
                )
        }
)
public class ConnectMessageReceipt
        extends AuditedTenantEntity {

    @Column(
            name = "message_id",
            nullable = false
    )
    private UUID messageId;

    @Column(
            name = "user_id",
            nullable = false
    )
    private UUID userId;

    @Column(
            name = "delivered_at"
    )
    private Instant deliveredAt;

    @Column(
            name = "read_at"
    )
    private Instant readAt;

    protected ConnectMessageReceipt() {
    }

    public ConnectMessageReceipt(
            UUID tenantId,
            UUID messageId,
            UUID userId
    ) {
        setTenantId(
                required(
                        tenantId,
                        "tenantId"
                )
        );

        this.messageId =
                required(
                        messageId,
                        "messageId"
                );

        this.userId =
                required(
                        userId,
                        "userId"
                );
    }

    public void markDelivered() {

        if (deliveredAt == null) {
            deliveredAt =
                    Instant.now();
        }
    }

    public void markRead() {

        if (readAt != null) {
            return;
        }

        Instant now =
                Instant.now();

        if (deliveredAt == null) {
            deliveredAt =
                    now;
        }

        readAt =
                now;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    private static UUID required(
            UUID value,
            String name
    ) {

        if (value == null) {
            throw new IllegalArgumentException(
                    name + " is required"
            );
        }

        return value;
    }
}
