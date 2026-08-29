package africa.growtogether.platform.ens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class NotificationSecurePayloadCryptoTest {

    private static final String KEY =
            Base64.getEncoder()
                    .encodeToString(
                            "0123456789ABCDEF0123456789ABCDEF"
                                    .getBytes(StandardCharsets.UTF_8)
                    );

    @Test
    void encryptDecryptRoundTripPreservesExactBody() {
        UUID tenantId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        NotificationSecurePayloadCrypto crypto =
                new NotificationSecurePayloadCrypto(
                        KEY,
                        "ens-test-key"
                );

        String body =
                "Activate your GrowTogether account: "
                        + "https://example.test/activate?token=secret-value";

        NotificationSecurePayloadCrypto.EncryptedPayload encrypted =
                crypto.encrypt(
                        tenantId,
                        notificationId,
                        body
                );

        String decrypted =
                crypto.decrypt(
                        tenantId,
                        notificationId,
                        encrypted.ciphertext(),
                        encrypted.iv(),
                        encrypted.keyId()
                );

        assertEquals(
                body,
                decrypted
        );
    }

    @Test
    void ciphertextCannotBeMovedToAnotherNotification() {
        UUID tenantId = UUID.randomUUID();

        UUID notificationA =
                UUID.randomUUID();

        UUID notificationB =
                UUID.randomUUID();

        NotificationSecurePayloadCrypto crypto =
                new NotificationSecurePayloadCrypto(
                        KEY,
                        "ens-test-key"
                );

        NotificationSecurePayloadCrypto.EncryptedPayload encrypted =
                crypto.encrypt(
                        tenantId,
                        notificationA,
                        "sensitive activation body"
                );

        assertThrows(
                IllegalStateException.class,
                () -> crypto.decrypt(
                        tenantId,
                        notificationB,
                        encrypted.ciphertext(),
                        encrypted.iv(),
                        encrypted.keyId()
                )
        );
    }

    @Test
    void storedKeyIdMustMatchActiveKey() {
        NotificationSecurePayloadCrypto crypto =
                new NotificationSecurePayloadCrypto(
                        KEY,
                        "ens-test-key"
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID notificationId =
                UUID.randomUUID();

        NotificationSecurePayloadCrypto.EncryptedPayload encrypted =
                crypto.encrypt(
                        tenantId,
                        notificationId,
                        "sensitive activation body"
                );

        assertThrows(
                IllegalStateException.class,
                () -> crypto.decrypt(
                        tenantId,
                        notificationId,
                        encrypted.ciphertext(),
                        encrypted.iv(),
                        "different-key"
                )
        );
    }
}
