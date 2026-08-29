package africa.growtogether.platform.ens;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "gt.ens.secure-payload",
        name = "enabled",
        havingValue = "true"
)
@EnableConfigurationProperties(
        NotificationSecurePayloadProperties.class
)
public class NotificationSecurePayloadConfiguration {

    @Bean
    NotificationSecurePayloadCrypto notificationSecurePayloadCrypto(
            NotificationSecurePayloadProperties properties
    ) {
        return new NotificationSecurePayloadCrypto(
                properties.encryptionKey(),
                properties.keyId()
        );
    }
}
