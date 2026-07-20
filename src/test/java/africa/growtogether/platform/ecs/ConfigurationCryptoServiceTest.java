package africa.growtogether.platform.ecs;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class ConfigurationCryptoServiceTest {
    private final ConfigurationCryptoService crypto = new ConfigurationCryptoService(
        Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes()), "test-key");

    @Test void encryptsAndDecryptsSecrets() {
        var encrypted = crypto.encrypt("super-secret");
        assertNotEquals("super-secret", encrypted.ciphertext());
        assertEquals("super-secret", crypto.decrypt(encrypted.ciphertext(), encrypted.iv()));
        assertEquals("test-key", encrypted.keyId());
    }

    @Test void usesRandomIvForEveryEncryption() {
        var first = crypto.encrypt("same-value");
        var second = crypto.encrypt("same-value");
        assertNotEquals(first.ciphertext(), second.ciphertext());
        assertEquals(first.valueHash(), second.valueHash());
    }
}
