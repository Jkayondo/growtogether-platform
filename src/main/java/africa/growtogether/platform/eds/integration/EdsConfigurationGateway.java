package africa.growtogether.platform.eds.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationService;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Central ECS adapter for document policy. EDS never reads configuration tables directly. */
@Component
public class EdsConfigurationGateway {
    private final ConfigurationService configuration;
    private final EnterpriseIdentityContext identity;
    public EdsConfigurationGateway(ConfigurationService configuration, EnterpriseIdentityContext identity) {
        this.configuration = configuration; this.identity = identity;
    }
    private String value(String code, String fallback) {
        try {
            var resolved = configuration.resolve(new ResolveRequest(code, null, null, identity.tenantId()));
            return resolved.value() == null ? fallback : resolved.value();
        } catch (RuntimeException ex) { return fallback; }
    }
    public long maximumUploadBytes() { try { return Long.parseLong(value("EDS_MAX_UPLOAD_BYTES", "52428800")); } catch (Exception ex) { return 52428800L; } }
    public long defaultShareExpirySeconds() { try { return Long.parseLong(value("EDS_DEFAULT_SHARE_EXPIRY_SECONDS", "86400")); } catch (Exception ex) { return 86400L; } }
    public Set<String> allowedMimeTypes() { return Set.of(value("EDS_ALLOWED_MIME_TYPES", "application/pdf,image/png,image/jpeg,text/plain").split(",")); }
}
