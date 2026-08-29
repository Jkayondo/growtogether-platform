package africa.growtogether.platform.ens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

class NotificationSecurePayloadServiceTest {

    private static final String KEY =
            Base64.getEncoder()
                    .encodeToString(
                            "0123456789ABCDEF0123456789ABCDEF"
                                    .getBytes(StandardCharsets.UTF_8)
                    );

    private NotificationRequestRepository notifications;

    private NotificationSecurePayloadRepository payloads;

    @BeforeEach
    void setUp() {
        notifications =
                mock(NotificationRequestRepository.class);

        payloads =
                mock(NotificationSecurePayloadRepository.class);
    }

    @Test
    void attachEncryptsFinalBodyBeforePersistence() {
        UUID tenantId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        NotificationRequest notification =
                queuedNotification(
                        tenantId,
                        notificationId
                );

        when(
                payloads.existsByTenantIdAndNotificationRequestId(
                        tenantId,
                        notificationId
                )
        ).thenReturn(false);

        when(
                payloads.saveAndFlush(
                        any(NotificationSecurePayload.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        NotificationSecurePayloadCrypto crypto =
                crypto();

        NotificationSecurePayloadService service =
                service(Optional.of(crypto));

        String plaintext =
                "Activate your GrowTogether account "
                        + "with secret-token-value";

        service.attach(
                tenantId,
                notificationId,
                plaintext,
                Instant.now().plusSeconds(600)
        );

        ArgumentCaptor<NotificationSecurePayload> captor =
                ArgumentCaptor.forClass(
                        NotificationSecurePayload.class
                );

        verify(payloads).saveAndFlush(
                captor.capture()
        );

        NotificationSecurePayload persisted =
                captor.getValue();

        assertFalse(
                plaintext.equals(
                        persisted.encryptedPayload()
                )
        );

        assertEquals(
                plaintext,
                crypto.decrypt(
                        tenantId,
                        notificationId,
                        persisted.encryptedPayload(),
                        persisted.encryptionIv(),
                        persisted.encryptionKeyId()
                )
        );

        verify(
                notifications
        ).findByIdAndTenantIdForUpdate(
                notificationId,
                tenantId
        );
    }

    @Test
    void attachFailsClosedWhenCryptoIsDisabled() {
        UUID tenantId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        queuedNotification(
                tenantId,
                notificationId
        );

        when(
                payloads.existsByTenantIdAndNotificationRequestId(
                        tenantId,
                        notificationId
                )
        ).thenReturn(false);

        NotificationSecurePayloadService service =
                service(Optional.empty());

        assertThrows(
                NotificationSecurePayloadException.class,
                () -> service.attach(
                        tenantId,
                        notificationId,
                        "secret body",
                        Instant.now().plusSeconds(600)
                )
        );

        verify(
                payloads,
                never()
        ).saveAndFlush(
                any(NotificationSecurePayload.class)
        );
    }

    @Test
    void resolveDecryptsUsablePayload() {
        UUID tenantId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        NotificationSecurePayloadCrypto crypto =
                crypto();

        String plaintext =
                "one-time activation secret";

        NotificationSecurePayload payload =
                securePayload(
                        crypto,
                        tenantId,
                        notificationId,
                        plaintext,
                        Instant.now().plusSeconds(600)
                );

        when(
                payloads.findByTenantIdAndNotificationRequestId(
                        tenantId,
                        notificationId
                )
        ).thenReturn(
                Optional.of(payload)
        );

        Optional<String> resolved =
                service(
                        Optional.of(crypto)
                ).resolveIfPresent(
                        tenantId,
                        notificationId,
                        Instant.now()
                );

        assertTrue(
                resolved.isPresent()
        );

        assertEquals(
                plaintext,
                resolved.orElseThrow()
        );
    }

    @Test
    void expiredPayloadFailsWithoutReturningPlaintext() {
        UUID tenantId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        NotificationSecurePayloadCrypto crypto =
                crypto();

        NotificationSecurePayload payload =
                securePayload(
                        crypto,
                        tenantId,
                        notificationId,
                        "expired secret",
                        Instant.now().minusSeconds(1)
                );

        when(
                payloads.findByTenantIdAndNotificationRequestId(
                        tenantId,
                        notificationId
                )
        ).thenReturn(
                Optional.of(payload)
        );

        assertThrows(
                NotificationSecurePayloadException.class,
                () -> service(
                        Optional.of(crypto)
                ).resolveIfPresent(
                        tenantId,
                        notificationId,
                        Instant.now()
                )
        );
    }

    @Test
    void absentPayloadReturnsEmptyWithoutCrypto() {
        UUID tenantId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(
                payloads.findByTenantIdAndNotificationRequestId(
                        tenantId,
                        notificationId
                )
        ).thenReturn(
                Optional.empty()
        );

        Optional<String> resolved =
                service(
                        Optional.empty()
                ).resolveIfPresent(
                        tenantId,
                        notificationId,
                        Instant.now()
                );

        assertTrue(
                resolved.isEmpty()
        );
    }

    @Test
    void retireIsIdempotentAtServiceBoundary() {
        UUID tenantId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        NotificationSecurePayloadCrypto crypto =
                crypto();

        NotificationSecurePayload payload =
                securePayload(
                        crypto,
                        tenantId,
                        notificationId,
                        "retirement secret",
                        Instant.now().plusSeconds(600)
                );

        when(
                payloads.findByTenantIdAndNotificationRequestId(
                        tenantId,
                        notificationId
                )
        ).thenReturn(
                Optional.of(payload)
        );

        when(
                payloads.saveAndFlush(
                        any(NotificationSecurePayload.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        NotificationSecurePayloadService service =
                service(
                        Optional.of(crypto)
                );

        Instant firstRetirement =
                Instant.now();

        assertTrue(
                service.retireIfPresent(
                        tenantId,
                        notificationId,
                        firstRetirement
                )
        );

        assertTrue(
                service.retireIfPresent(
                        tenantId,
                        notificationId,
                        firstRetirement.plusSeconds(5)
                )
        );

        assertEquals(
                firstRetirement,
                payload.retiredAt()
        );
    }

    private NotificationRequest queuedNotification(
            UUID tenantId,
            UUID notificationId
    ) {
        NotificationRequest notification =
                mock(NotificationRequest.class);

        when(
                notifications.findByIdAndTenantIdForUpdate(
                        notificationId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(notification)
        );

        when(
                notification.notificationStatus()
        ).thenReturn(
                NotificationStatus.QUEUED
        );

        return notification;
    }

    private NotificationSecurePayload securePayload(
            NotificationSecurePayloadCrypto crypto,
            UUID tenantId,
            UUID notificationId,
            String plaintext,
            Instant expiresAt
    ) {
        NotificationSecurePayloadCrypto.EncryptedPayload encrypted =
                crypto.encrypt(
                        tenantId,
                        notificationId,
                        plaintext
                );

        return new NotificationSecurePayload(
                tenantId,
                notificationId,
                encrypted.ciphertext(),
                encrypted.iv(),
                encrypted.keyId(),
                expiresAt
        );
    }

    private NotificationSecurePayloadService service(
            Optional<NotificationSecurePayloadCrypto> crypto
    ) {
        return new NotificationSecurePayloadService(
                notifications,
                payloads,
                crypto
        );
    }

    private static NotificationSecurePayloadCrypto crypto() {
        return new NotificationSecurePayloadCrypto(
                KEY,
                "ens-service-test-key"
        );
    }
}
