package africa.growtogether.platform.eiam.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gt.eiam.bootstrap.first-admin")
public record FirstAdminBootstrapProperties(
        boolean enabled,
        String tenantId,
        String tokenHash
) {}
