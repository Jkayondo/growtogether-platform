package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "ens_notification_secure_payloads",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_ens_secure_payload_notification",
                        columnNames = {
                                "tenant_id",
                                "notification_request_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "ix_ens_secure_payload_notification",
                        columnList = "tenant_id,notification_request_id"
                )
        }
)
public class NotificationSecurePayload
        extends AuditedTenantEntity {

    @Column(
            name = "notification_request_id",
            nullable = false,
            updatable = false
    )
    private UUID notificationRequestId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payload_kind",
            nullable = false,
            length = 40,
            updatable = false
    )
    private NotificationSecurePayloadKind payloadKind =
            NotificationSecurePayloadKind.FINAL_PROVIDER_BODY;

    @Column(
            name = "encrypted_payload",
            nullable = false,
            columnDefinition = "text",
            updatable = false
    )
    private String encryptedPayload;

    @Column(
            name = "encryption_iv",
            nullable = false,
            length = 100,
            updatable = false
    )
    private String encryptionIv;

    @Column(
            name = "encryption_key_id",
            nullable = false,
            length = 100,
            updatable = false
    )
    private String encryptionKeyId;

    @Column(
            name = "expires_at",
            nullable = false,
            updatable = false
    )
    private Instant expiresAt;

    @Column(name = "retired_at")
    private Instant retiredAt;

    protected NotificationSecurePayload() {
    }

    public NotificationSecurePayload(
            UUID tenantId,
            UUID notificationRequestId,
            String encryptedPayload,
            String encryptionIv,
            String encryptionKeyId,
            Instant expiresAt
    ) {
        setTenantId(
                Objects.requireNonNull(
                        tenantId,
                        "tenantId must not be null"
                )
        );

        this.notificationRequestId =
                Objects.requireNonNull(
                        notificationRequestId,
                        "notificationRequestId must not be null"
                );

        this.encryptedPayload =
                required(
                        encryptedPayload,
                        "encryptedPayload"
                );

        this.encryptionIv =
                required(
                        encryptionIv,
                        "encryptionIv"
                );

        this.encryptionKeyId =
                required(
                        encryptionKeyId,
                        "encryptionKeyId"
                );

        this.expiresAt =
                Objects.requireNonNull(
                        expiresAt,
                        "expiresAt must not be null"
                );
    }

    public void retire(Instant retiredAt) {
        Instant value =
                Objects.requireNonNull(
                        retiredAt,
                        "retiredAt must not be null"
                );

        if (this.retiredAt != null) {
            return;
        }

        if (
                getCreatedAt() != null
                        && value.isBefore(getCreatedAt())
        ) {
            throw new IllegalArgumentException(
                    "retiredAt must not precede creation"
            );
        }

        this.retiredAt = value;
    }

    public boolean isUsableAt(Instant instant) {
        Objects.requireNonNull(
                instant,
                "instant must not be null"
        );

        return getStatus() == EntityStatus.ACTIVE
                && retiredAt == null
                && instant.isBefore(expiresAt);
    }

    public UUID id() {
        return getId();
    }

    public UUID notificationRequestId() {
        return notificationRequestId;
    }

    public NotificationSecurePayloadKind payloadKind() {
        return payloadKind;
    }

    String encryptedPayload() {
        return encryptedPayload;
    }

    String encryptionIv() {
        return encryptionIv;
    }

    String encryptionKeyId() {
        return encryptionKeyId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant retiredAt() {
        return retiredAt;
    }

    private static String required(
            String value,
            String field
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value.trim();
    }
}
