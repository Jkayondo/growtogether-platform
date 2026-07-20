package africa.growtogether.platform.eap.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationService;
import java.time.Duration;
import org.springframework.stereotype.Component;

/** Central ECS adapter for analytics runtime policies. */
@Component
public class EapConfigurationGateway {
    private final ConfigurationService configuration;
    private final EnterpriseIdentityContext identity;

    public EapConfigurationGateway(ConfigurationService configuration, EnterpriseIdentityContext identity) {
        this.configuration = configuration;
        this.identity = identity;
    }

    private String value(String code, String fallback) {
        try {
            var resolved = configuration.resolve(new ResolveRequest(code, null, null, identity.tenantId()));
            return resolved.value() == null ? fallback : resolved.value();
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public int eventBatchSize() { return parseInt("EAP_EVENT_BATCH_SIZE", 100); }
    public int maximumAttempts() { return parseInt("EAP_MAX_PROCESSING_ATTEMPTS", 5); }
    public Duration reportRetention() { return Duration.ofDays(parseInt("EAP_REPORT_RETENTION_DAYS", 90)); }
    public boolean alertDeliveryEnabled() { return Boolean.parseBoolean(value("EAP_ALERT_DELIVERY_ENABLED", "true")); }
    public boolean externalDataSourcesEnabled() { return Boolean.parseBoolean(value("EAP_EXTERNAL_DATA_SOURCES_ENABLED", "false")); }

    private int parseInt(String code, int fallback) {
        try { return Integer.parseInt(value(code, Integer.toString(fallback))); }
        catch (Exception ex) { return fallback; }
    }
}
