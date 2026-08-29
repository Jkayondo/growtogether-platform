package africa.growtogether.platform.ens;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "gt.ens.secure-payload"
)
public record NotificationSecurePayloadProperties(
        String encryptionKey,
        String keyId
) {
}
