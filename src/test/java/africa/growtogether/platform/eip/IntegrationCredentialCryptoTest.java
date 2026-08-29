package africa.growtogether.platform.eip;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class IntegrationCredentialCryptoTest {

    @Test
    void encryptsWithRandomIvAndDecryptsBothCiphertexts() {
        IntegrationCredentialCrypto crypto =
                crypto(new byte[32], "test-key");

        String first = crypto.encrypt("secret");
        String second = crypto.encrypt("secret");

        assertNotEquals(first, second);
        assertFalse(first.contains("secret"));
        assertFalse(second.contains("secret"));

        assertEquals(
                "secret",
                crypto.decrypt(first, "test-key")
        );

        assertEquals(
                "secret",
                crypto.decrypt(second, "test-key")
        );
    }

    @Test
    void decryptsCredentialRoundTrip() {
        IntegrationCredentialCrypto crypto =
                crypto(new byte[32], "active-key");

        String ciphertext =
                crypto.encrypt(
                        "Bearer provider-secret"
                );

        assertEquals(
                "Bearer provider-secret",
                crypto.decrypt(
                        ciphertext,
                        "active-key"
                )
        );
    }

    @Test
    void rejectsMissingStoredKeyId() {
        IntegrationCredentialCrypto crypto =
                crypto(new byte[32], "active-key");

        String ciphertext =
                crypto.encrypt("secret");

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> crypto.decrypt(
                                ciphertext,
                                null
                        )
                );

        assertEquals(
                "Integration credential key id is missing",
                error.getMessage()
        );
    }

    @Test
    void rejectsMismatchedStoredKeyId() {
        IntegrationCredentialCrypto crypto =
                crypto(new byte[32], "active-key");

        String ciphertext =
                crypto.encrypt("secret");

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> crypto.decrypt(
                                ciphertext,
                                "old-key"
                        )
                );

        assertEquals(
                "Integration credential key id does not match active key",
                error.getMessage()
        );
    }

    @Test
    void rejectsTamperedCiphertext() {
        IntegrationCredentialCrypto crypto =
                crypto(new byte[32], "active-key");

        String ciphertext =
                crypto.encrypt("secret");

        byte[] bytes =
                Base64.getUrlDecoder()
                        .decode(ciphertext);

        bytes[bytes.length - 1] ^= 0x01;

        String tampered =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> crypto.decrypt(
                                tampered,
                                "active-key"
                        )
                );

        assertEquals(
                "Unable to decrypt integration credential",
                error.getMessage()
        );

        assertNotNull(error.getCause());
    }

    @Test
    void rejectsDifferentCryptographicKeyEvenWithSameKeyId() {
        byte[] firstKey = new byte[32];
        byte[] secondKey = new byte[32];

        secondKey[0] = 1;

        IntegrationCredentialCrypto encryptor =
                crypto(
                        firstKey,
                        "shared-key-id"
                );

        IntegrationCredentialCrypto decryptor =
                crypto(
                        secondKey,
                        "shared-key-id"
                );

        String ciphertext =
                encryptor.encrypt("secret");

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> decryptor.decrypt(
                                ciphertext,
                                "shared-key-id"
                        )
                );

        assertEquals(
                "Unable to decrypt integration credential",
                error.getMessage()
        );
    }

    @Test
    void emptyCredentialProducesAndDecryptsAsNull() {
        IntegrationCredentialCrypto crypto =
                crypto(new byte[32], "active-key");

        assertNull(crypto.encrypt(null));
        assertNull(crypto.encrypt(""));
        assertNull(crypto.encrypt("   "));

        assertNull(
                crypto.decrypt(
                        null,
                        null
                )
        );

        assertNull(
                crypto.decrypt(
                        "",
                        null
                )
        );
    }

    @Test
    void allowsStartupWithoutKeyButRejectsCredentialEncryption() {
        IntegrationCredentialCrypto crypto =
                new IntegrationCredentialCrypto(
                        "",
                        ""
                );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> crypto.encrypt(
                                "real-provider-secret"
                        )
                );

        assertEquals(
                "GT EIP integration credential encryption is not configured",
                error.getMessage()
        );
    }

    @Test
    void allowsStartupWithoutKeyButRejectsCredentialDecryption() {
        IntegrationCredentialCrypto crypto =
                new IntegrationCredentialCrypto(
                        "",
                        ""
                );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> crypto.decrypt(
                                "encrypted-provider-secret",
                                "provider-key"
                        )
                );

        assertEquals(
                "GT EIP integration credential encryption is not configured",
                error.getMessage()
        );
    }

    private IntegrationCredentialCrypto crypto(
            byte[] rawKey,
            String keyId
    ) {
        return new IntegrationCredentialCrypto(
                Base64.getEncoder()
                        .encodeToString(rawKey),
                keyId
        );
    }
}
