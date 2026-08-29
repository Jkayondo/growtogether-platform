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
        name = "gt_connect_announcements",
        indexes = {
                @Index(
                        name = "ix_gt_connect_announcements_tenant_space_published",
                        columnList = "tenant_id,space_id,published_at"
                ),
                @Index(
                        name = "ix_gt_connect_announcements_tenant_publisher",
                        columnList = "tenant_id,published_by_user_id"
                )
        }
)
public class ConnectAnnouncement
        extends AuditedTenantEntity {

    @Column(
            name = "space_id",
            nullable = false
    )
    private UUID spaceId;

    @Column(
            name = "message_id",
            nullable = false
    )
    private UUID messageId;

    @Column(
            name = "published_by_user_id",
            nullable = false
    )
    private UUID publishedByUserId;

    @Column(
            name = "title",
            nullable = false,
            length = 200
    )
    private String title;

    @Column(
            name = "body",
            nullable = false,
            columnDefinition = "text"
    )
    private String body;

    @Column(
            name = "published_at",
            nullable = false
    )
    private Instant publishedAt;

    protected ConnectAnnouncement() {
    }

    public ConnectAnnouncement(
            UUID tenantId,
            UUID spaceId,
            UUID messageId,
            UUID publishedByUserId,
            String title,
            String body
    ) {
        setTenantId(
                required(
                        tenantId,
                        "tenantId"
                )
        );

        this.spaceId =
                required(
                        spaceId,
                        "spaceId"
                );

        this.messageId =
                required(
                        messageId,
                        "messageId"
                );

        this.publishedByUserId =
                required(
                        publishedByUserId,
                        "publishedByUserId"
                );

        this.title =
                requiredText(
                        title,
                        "title"
                );

        this.body =
                requiredText(
                        body,
                        "body"
                );

        this.publishedAt =
                Instant.now();
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public UUID getPublishedByUserId() {
        return publishedByUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Instant getPublishedAt() {
        return publishedAt;
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
}
