package africa.growtogether.platform.ewe.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationService;
import java.time.Duration;
import org.springframework.stereotype.Component;

/** Central EWE-to-ECS contract. Workflow code must not read ECS tables directly. */
@Component
public class EweConfigurationGateway {
    private final ConfigurationService configuration;
    private final EnterpriseIdentityContext identity;

    public EweConfigurationGateway(ConfigurationService configuration, EnterpriseIdentityContext identity) {
        this.configuration = configuration;
        this.identity = identity;
    }

    public String stringValue(String code, String fallback) {
        try {
            var resolved = configuration.resolve(new ResolveRequest(code, null, null, identity.tenantId()));
            return resolved.value() == null ? fallback : resolved.value();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public int integerValue(String code, int fallback) {
        try { return Integer.parseInt(stringValue(code, Integer.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    public Duration durationSeconds(String code, long fallbackSeconds) {
        return Duration.ofSeconds(integerValue(code, Math.toIntExact(fallbackSeconds)));
    }
}
