package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "gt_connect_messages",
        indexes = {
                @Index(
                        name = "ix_gt_connect_messages_tenant_space_sent",
                        columnList = "tenant_id,space_id,sent_at"
                ),
                @Index(
                        name = "ix_gt_connect_messages_tenant_sender",
                        columnList = "tenant_id,sender_user_id"
                )
        }
)
public class ConnectMessage extends AuditedTenantEntity {

    @Column(
            name = "space_id",
            nullable = false
    )
    private UUID spaceId;

    @Column(
            name = "sender_user_id"
    )
    private UUID senderUserId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "message_type",
            nullable = false,
            length = 30
    )
    private ConnectMessageType messageType;

    @Column(
            name = "body",
            columnDefinition = "text"
    )
    private String body;

    @Column(
            name = "reply_to_message_id"
    )
    private UUID replyToMessageId;

    @Column(
            name = "sent_at",
            nullable = false
    )
    private Instant sentAt;

    @Column(
            name = "edited_at"
    )
    private Instant editedAt;

    @Column(
            name = "deleted_at"
    )
    private Instant deletedAt;

    @Column(
            name = "source_service",
            length = 80
    )
    private String sourceService;

    @Column(
            name = "source_reference",
            length = 160
    )
    private String sourceReference;

    protected ConnectMessage() {
    }

    public ConnectMessage(
            UUID tenantId,
            UUID spaceId,
            UUID senderUserId,
            ConnectMessageType messageType,
            String body,
            UUID replyToMessageId
    ) {
        setTenantId(required(tenantId, "tenantId"));

        this.spaceId = required(spaceId, "spaceId");
        this.senderUserId = required(
                senderUserId,
                "senderUserId"
        );

        if (messageType == null) {
            throw new IllegalArgumentException(
                    "messageType is required"
            );
        }

        if (
                messageType == ConnectMessageType.TEXT
                        && (
                        body == null
                                || body.isBlank()
                )
        ) {
            throw new IllegalArgumentException(
                    "body is required for TEXT messages"
            );
        }

        this.messageType = messageType;
        this.body = clean(body);
        this.replyToMessageId = replyToMessageId;
        this.sentAt = Instant.now();
    }

    public static ConnectMessage automatedSystem(
            UUID tenantId,
            UUID spaceId,
            String body,
            String sourceService,
            String sourceReference
    ) {

        if (
                body == null
                        || body.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "body is required for automated SYSTEM messages"
            );
        }

        ConnectMessage message =
                new ConnectMessage();

        message.setTenantId(
                required(
                        tenantId,
                        "tenantId"
                )
        );

        message.spaceId =
                required(
                        spaceId,
                        "spaceId"
                );

        message.senderUserId = null;
        message.messageType = ConnectMessageType.SYSTEM;
        message.body = clean(body);
        message.replyToMessageId = null;
        message.sentAt = Instant.now();

        message.sourceService =
                requiredText(
                        sourceService,
                        "sourceService"
                );

        message.sourceReference =
                clean(
                        sourceReference
                );

        return message;
    }

    public void edit(String newBody) {
        if (deletedAt != null) {
            throw new IllegalStateException(
                    "Deleted message cannot be edited"
            );
        }

        if (
                messageType == ConnectMessageType.TEXT
                        && (
                        newBody == null
                                || newBody.isBlank()
                )
        ) {
            throw new IllegalArgumentException(
                    "body is required for TEXT messages"
            );
        }

        this.body = clean(newBody);
        this.editedAt = Instant.now();
    }

    public void delete() {
        if (deletedAt == null) {
            this.deletedAt = Instant.now();
        }
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public UUID getSenderUserId() {
        return senderUserId;
    }

    public ConnectMessageType getMessageType() {
        return messageType;
    }

    public String getBody() {
        return body;
    }

    public UUID getReplyToMessageId() {
        return replyToMessageId;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getEditedAt() {
        return editedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getSourceService() {
        return sourceService;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    private static UUID required(UUID value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    private static String requiredText(
            String value,
            String name
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    name + " is required"
            );
        }

        return value.trim();
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}
