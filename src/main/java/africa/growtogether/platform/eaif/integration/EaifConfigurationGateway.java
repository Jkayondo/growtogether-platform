package africa.growtogether.platform.eaif.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationService;
import org.springframework.stereotype.Component;

/** Central ECS adapter for AI provider, safety, cost and retention policy. */
@Component
public class EaifConfigurationGateway {
    private final ConfigurationService configuration;
    private final EnterpriseIdentityContext identity;

    public EaifConfigurationGateway(ConfigurationService configuration, EnterpriseIdentityContext identity) {
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

    public boolean providerExecutionEnabled() { return Boolean.parseBoolean(value("EAIF_PROVIDER_EXECUTION_ENABLED", "false")); }
    public boolean highRiskApprovalRequired() { return Boolean.parseBoolean(value("EAIF_HIGH_RISK_APPROVAL_REQUIRED", "true")); }
    public int maximumInputCharacters() { return integer("EAIF_MAX_INPUT_CHARACTERS", 100000); }
    public int requestRetentionDays() { return integer("EAIF_REQUEST_RETENTION_DAYS", 90); }
    public String defaultModelCode() { return value("EAIF_DEFAULT_MODEL_CODE", ""); }

    private int integer(String code, int fallback) {
        try { return Integer.parseInt(value(code, Integer.toString(fallback))); }
        catch (Exception ex) { return fallback; }
    }
}
