package africa.growtogether.platform.eip.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationService;
import java.time.Duration;
import org.springframework.stereotype.Component;

/** Central ECS adapter. EIP never reads ECS persistence tables or hard-codes tenant policy. */
@Component
public class EipConfigurationGateway {
    private final ConfigurationService configuration;
    private final EnterpriseIdentityContext identity;
    public EipConfigurationGateway(ConfigurationService configuration, EnterpriseIdentityContext identity) {
        this.configuration = configuration; this.identity = identity;
    }
    private String value(String code, String fallback) {
        try {
            var resolved = configuration.resolve(new ResolveRequest(code, null, null, identity.tenantId()));
            return resolved.value() == null ? fallback : resolved.value();
        } catch (RuntimeException ex) { return fallback; }
    }
    public int maximumAttempts() { try { return Integer.parseInt(value("EIP_MAX_ATTEMPTS", "5")); } catch (Exception ex) { return 5; } }
    public Duration requestTimeout() { try { return Duration.ofSeconds(Long.parseLong(value("EIP_REQUEST_TIMEOUT_SECONDS", "30"))); } catch (Exception ex) { return Duration.ofSeconds(30); } }
    public int circuitFailureThreshold() { try { return Integer.parseInt(value("EIP_CIRCUIT_FAILURE_THRESHOLD", "5")); } catch (Exception ex) { return 5; } }
    public boolean externalDeliveryEnabled() { return Boolean.parseBoolean(value("EIP_EXTERNAL_DELIVERY_ENABLED", "false")); }
}
