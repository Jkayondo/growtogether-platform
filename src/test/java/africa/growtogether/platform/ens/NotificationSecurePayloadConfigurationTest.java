package africa.growtogether.platform.ens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class NotificationSecurePayloadConfigurationTest {

    private static final String KEY =
            Base64.getEncoder()
                    .encodeToString(
                            "0123456789ABCDEF0123456789ABCDEF"
                                    .getBytes(StandardCharsets.UTF_8)
                    );

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            NotificationSecurePayloadConfiguration.class
                    );

    @Test
    void disabledDoesNotCreateCryptoBean() {
        contextRunner
                .withPropertyValues(
                        "gt.ens.secure-payload.enabled=false"
                )
                .run(
                        context -> {
                            assertNull(
                                    context.getStartupFailure()
                            );

                            assertEquals(
                                    0,
                                    context.getBeansOfType(
                                            NotificationSecurePayloadCrypto.class
                                    ).size()
                            );
                        }
                );
    }

    @Test
    void enabledWithExplicitKeyCreatesCryptoBean() {
        contextRunner
                .withPropertyValues(
                        "gt.ens.secure-payload.enabled=true",
                        "gt.ens.secure-payload.encryption-key="
                                + KEY,
                        "gt.ens.secure-payload.key-id="
                                + "ens-test-key"
                )
                .run(
                        context -> {
                            assertNull(
                                    context.getStartupFailure()
                            );

                            NotificationSecurePayloadCrypto crypto =
                                    context.getBean(
                                            NotificationSecurePayloadCrypto.class
                                    );

                            assertEquals(
                                    "ens-test-key",
                                    crypto.keyId()
                            );
                        }
                );
    }

    @Test
    void enabledWithoutEncryptionKeyFailsClosed() {
        contextRunner
                .withPropertyValues(
                        "gt.ens.secure-payload.enabled=true",
                        "gt.ens.secure-payload.key-id="
                                + "ens-test-key"
                )
                .run(
                        context -> assertNotNull(
                                context.getStartupFailure()
                        )
                );
    }
}
