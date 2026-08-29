package africa.growtogether.platform.ens;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
 * Purpose-bound cryptographic primitive for ENS transient secure payloads.
 *
 * This class is deliberately not a Spring bean yet. Configuration and key
 * lifecycle wiring are established separately so that GT never introduces
 * an insecure default notification encryption key merely to satisfy
 * application-context startup.
 */
public final class NotificationSecurePayloadCrypto {

    private static final int KEY_BYTES = 32;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private static final String AAD_PREFIX =
            "GT|ENS|SECURE_NOTIFICATION_PAYLOAD|V1|";

    private final SecretKey key;
    private final String keyId;
    private final SecureRandom random;

    public NotificationSecurePayloadCrypto(
            String encodedKey,
            String keyId
    ) {
        this(
                encodedKey,
                keyId,
                new SecureRandom()
        );
    }

    NotificationSecurePayloadCrypto(
            String encodedKey,
            String keyId,
            SecureRandom random
    ) {
        if (
                encodedKey == null
                        || encodedKey.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "ENS secure payload encryption key is required"
            );
        }

        byte[] raw;

        try {
            raw = Base64.getDecoder()
                    .decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "ENS secure payload encryption key is invalid"
            );
        }

        if (raw.length != KEY_BYTES) {
            throw new IllegalArgumentException(
                    "ENS secure payload encryption key must be 32 bytes"
            );
        }

        if (
                keyId == null
                        || keyId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "ENS secure payload encryption key id is required"
            );
        }

        this.key =
                new SecretKeySpec(
                        raw,
                        "AES"
                );

        this.keyId = keyId.trim();

        this.random =
                Objects.requireNonNull(
                        random,
                        "random must not be null"
                );
    }

    public EncryptedPayload encrypt(
            UUID tenantId,
            UUID notificationRequestId,
            String plaintext
    ) {
        requireIdentifiers(
                tenantId,
                notificationRequestId
        );

        if (
                plaintext == null
                        || plaintext.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "secure notification plaintext must not be blank"
            );
        }

        try {
            byte[] iv =
                    new byte[IV_BYTES];

            random.nextBytes(iv);

            Cipher cipher =
                    Cipher.getInstance(
                            "AES/GCM/NoPadding"
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    new GCMParameterSpec(
                            TAG_BITS,
                            iv
                    )
            );

            cipher.updateAAD(
                    aad(
                            tenantId,
                            notificationRequestId
                    )
            );

            byte[] ciphertext =
                    cipher.doFinal(
                            plaintext.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return new EncryptedPayload(
                    Base64.getEncoder()
                            .encodeToString(ciphertext),
                    Base64.getEncoder()
                            .encodeToString(iv),
                    keyId
            );

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Secure notification payload encryption failed"
            );
        }
    }

    public String decrypt(
            UUID tenantId,
            UUID notificationRequestId,
            String ciphertext,
            String iv,
            String storedKeyId
    ) {
        requireIdentifiers(
                tenantId,
                notificationRequestId
        );

        if (
                storedKeyId == null
                        || storedKeyId.isBlank()
                        || !keyId.equals(storedKeyId.trim())
        ) {
            throw new IllegalStateException(
                    "Secure notification payload key is unavailable"
            );
        }

        if (
                ciphertext == null
                        || ciphertext.isBlank()
                        || iv == null
                        || iv.isBlank()
        ) {
            throw new IllegalStateException(
                    "Secure notification payload is invalid"
            );
        }

        try {
            byte[] decodedIv =
                    Base64.getDecoder()
                            .decode(iv);

            if (decodedIv.length != IV_BYTES) {
                throw new IllegalArgumentException(
                        "invalid IV length"
                );
            }

            Cipher cipher =
                    Cipher.getInstance(
                            "AES/GCM/NoPadding"
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    new GCMParameterSpec(
                            TAG_BITS,
                            decodedIv
                    )
            );

            cipher.updateAAD(
                    aad(
                            tenantId,
                            notificationRequestId
                    )
            );

            byte[] plaintext =
                    cipher.doFinal(
                            Base64.getDecoder()
                                    .decode(ciphertext)
                    );

            return new String(
                    plaintext,
                    StandardCharsets.UTF_8
            );

        } catch (
                GeneralSecurityException
                        | IllegalArgumentException exception
        ) {
            throw new IllegalStateException(
                    "Secure notification payload decryption failed"
            );
        }
    }

    public String keyId() {
        return keyId;
    }

    private static byte[] aad(
            UUID tenantId,
            UUID notificationRequestId
    ) {
        return (
                AAD_PREFIX
                        + tenantId
                        + "|"
                        + notificationRequestId
        ).getBytes(
                StandardCharsets.UTF_8
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

    public record EncryptedPayload(
            String ciphertext,
            String iv,
            String keyId
    ) {
        public EncryptedPayload {
            if (
                    ciphertext == null
                            || ciphertext.isBlank()
            ) {
                throw new IllegalArgumentException(
                        "ciphertext must not be blank"
                );
            }

            if (
                    iv == null
                            || iv.isBlank()
            ) {
                throw new IllegalArgumentException(
                        "iv must not be blank"
                );
            }

            if (
                    keyId == null
                            || keyId.isBlank()
            ) {
                throw new IllegalArgumentException(
                        "keyId must not be blank"
                );
            }

            ciphertext = ciphertext.trim();
            iv = iv.trim();
            keyId = keyId.trim();
        }
    }
}
