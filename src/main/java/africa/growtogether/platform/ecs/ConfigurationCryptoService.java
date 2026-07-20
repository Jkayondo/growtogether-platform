package africa.growtogether.platform.ecs;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationCryptoService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final SecretKeySpec key;
    private final String keyId;

    public ConfigurationCryptoService(
            @Value("${gt.ecs.encryption.key}") String encodedKey,
            @Value("${gt.ecs.encryption.key-id:ecs-primary}") String keyId) {
        byte[] raw = Base64.getDecoder().decode(encodedKey);
        if (raw.length != 32) throw new IllegalArgumentException("GT ECS encryption key must be 32 bytes.");
        this.key = new SecretKeySpec(raw, "AES");
        this.keyId = keyId;
    }

    public EncryptedValue encrypt(String plaintext) {
        try {
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptedValue(Base64.getEncoder().encodeToString(ciphertext), Base64.getEncoder().encodeToString(iv), keyId, sha256(plaintext));
        } catch (Exception e) {
            throw new ConfigurationException("Configuration secret encryption failed.");
        }
    }

    public String decrypt(String ciphertext, String iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.getDecoder().decode(iv)));
            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ConfigurationException("Configuration secret decryption failed.");
        }
    }

    public String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public record EncryptedValue(String ciphertext, String iv, String keyId, String valueHash) {}
}
