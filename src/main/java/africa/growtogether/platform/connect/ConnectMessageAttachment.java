package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "gt_connect_message_attachments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_gt_connect_message_attachment",
                        columnNames = {
                                "tenant_id",
                                "message_id",
                                "document_id",
                                "document_version"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "ix_gt_connect_attachment_message",
                        columnList = "tenant_id,message_id"
                ),
                @Index(
                        name = "ix_gt_connect_attachment_document",
                        columnList =
                                "tenant_id,document_id,document_version"
                )
        }
)
public class ConnectMessageAttachment
        extends AuditedTenantEntity {

    @Column(
            name = "message_id",
            nullable = false
    )
    private UUID messageId;

    @Column(
            name = "document_id",
            nullable = false
    )
    private UUID documentId;

    @Column(
            name = "document_version",
            nullable = false
    )
    private int documentVersion;

    protected ConnectMessageAttachment() {
    }

    public ConnectMessageAttachment(
            UUID tenantId,
            UUID messageId,
            UUID documentId,
            int documentVersion
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId is required"
            );
        }

        if (messageId == null) {
            throw new IllegalArgumentException(
                    "messageId is required"
            );
        }

        if (documentId == null) {
            throw new IllegalArgumentException(
                    "documentId is required"
            );
        }

        if (documentVersion <= 0) {
            throw new IllegalArgumentException(
                    "documentVersion must be greater than zero"
            );
        }

        setTenantId(
                tenantId
        );

        this.messageId =
                messageId;

        this.documentId =
                documentId;

        this.documentVersion =
                documentVersion;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public int getDocumentVersion() {
        return documentVersion;
    }
}
