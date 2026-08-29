package africa.growtogether.platform.ens;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationSecurePayloadService {

    private final NotificationRequestRepository notifications;

    private final NotificationSecurePayloadRepository payloads;

    private final Optional<NotificationSecurePayloadCrypto> crypto;

    public NotificationSecurePayloadService(
            NotificationRequestRepository notifications,
            NotificationSecurePayloadRepository payloads,
            Optional<NotificationSecurePayloadCrypto> crypto
    ) {
        this.notifications =
                Objects.requireNonNull(
                        notifications,
                        "notifications must not be null"
                );

        this.payloads =
                Objects.requireNonNull(
                        payloads,
                        "payloads must not be null"
                );

        this.crypto =
                Objects.requireNonNull(
                        crypto,
                        "crypto must not be null"
                );
    }

    @Transactional
    public NotificationSecurePayload attach(
            UUID tenantId,
            UUID notificationRequestId,
            String finalProviderBody,
            Instant expiresAt
    ) {
        requireIdentifiers(
                tenantId,
                notificationRequestId
        );

        if (
                finalProviderBody == null
                        || finalProviderBody.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "finalProviderBody must not be blank"
            );
        }

        Instant now = Instant.now();

        if (
                expiresAt == null
                        || !expiresAt.isAfter(now)
        ) {
            throw new IllegalArgumentException(
                    "expiresAt must be in the future"
            );
        }

        NotificationRequest notification =
                notifications
                        .findByIdAndTenantIdForUpdate(
                                notificationRequestId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Notification request was not found"
                                )
                        );

        if (
                notification.notificationStatus()
                        != NotificationStatus.QUEUED
        ) {
            throw new NotificationSecurePayloadException(
                    "Secure payload may only be attached "
                            + "to a queued notification"
            );
        }

        if (
                payloads.existsByTenantIdAndNotificationRequestId(
                        tenantId,
                        notificationRequestId
                )
        ) {
            throw new NotificationSecurePayloadException(
                    "Secure notification payload already exists"
            );
        }

        NotificationSecurePayloadCrypto secureCrypto =
                requireCrypto();

        NotificationSecurePayloadCrypto.EncryptedPayload encrypted =
                secureCrypto.encrypt(
                        tenantId,
                        notificationRequestId,
                        finalProviderBody
                );

        NotificationSecurePayload payload =
                new NotificationSecurePayload(
                        tenantId,
                        notificationRequestId,
                        encrypted.ciphertext(),
                        encrypted.iv(),
                        encrypted.keyId(),
                        expiresAt
                );

        return payloads.saveAndFlush(payload);
    }

    @Transactional(readOnly = true)
    public Optional<String> resolveIfPresent(
            UUID tenantId,
            UUID notificationRequestId,
            Instant at
    ) {
        requireIdentifiers(
                tenantId,
                notificationRequestId
        );

        Instant effectiveAt =
                Objects.requireNonNull(
                        at,
                        "at must not be null"
                );

        Optional<NotificationSecurePayload> found =
                payloads.findByTenantIdAndNotificationRequestId(
                        tenantId,
                        notificationRequestId
                );

        if (found.isEmpty()) {
            return Optional.empty();
        }

        NotificationSecurePayload payload =
                found.get();

        if (!payload.isUsableAt(effectiveAt)) {
            throw new NotificationSecurePayloadException(
                    "Secure notification payload is unavailable"
            );
        }

        NotificationSecurePayloadCrypto secureCrypto =
                requireCrypto();

        return Optional.of(
                secureCrypto.decrypt(
                        tenantId,
                        notificationRequestId,
                        payload.encryptedPayload(),
                        payload.encryptionIv(),
                        payload.encryptionKeyId()
                )
        );
    }

    @Transactional
    public boolean retireIfPresent(
            UUID tenantId,
            UUID notificationRequestId,
            Instant retiredAt
    ) {
        requireIdentifiers(
                tenantId,
                notificationRequestId
        );

        Instant effectiveRetiredAt =
                Objects.requireNonNull(
                        retiredAt,
                        "retiredAt must not be null"
                );

        Optional<NotificationSecurePayload> found =
                payloads.findByTenantIdAndNotificationRequestId(
                        tenantId,
                        notificationRequestId
                );

        if (found.isEmpty()) {
            return false;
        }

        NotificationSecurePayload payload =
                found.get();

        payload.retire(
                effectiveRetiredAt
        );

        payloads.saveAndFlush(
                payload
        );

        return true;
    }

    public boolean hasSecurePayload(
            UUID tenantId,
            UUID notificationRequestId
    ) {
        requireIdentifiers(
                tenantId,
                notificationRequestId
        );

        return payloads.existsByTenantIdAndNotificationRequestId(
                tenantId,
                notificationRequestId
        );
    }

    private NotificationSecurePayloadCrypto requireCrypto() {
        return crypto.orElseThrow(
                () -> new NotificationSecurePayloadException(
                        "Secure notification payload encryption "
                                + "is not enabled"
                )
        );
    }

    private static void requireIdentifiers(
            UUID tenantId,
            UUID notificationRequestId
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                notificationRequestId,
                "notificationRequestId must not be null"
        );
    }
}
