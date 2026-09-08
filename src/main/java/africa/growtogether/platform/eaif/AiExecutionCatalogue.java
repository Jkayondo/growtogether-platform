package africa.growtogether.platform.eaif;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bridge to existing package-private repositories; no second provider catalogue. */
@Service
public class AiExecutionCatalogue {
    private final AiModelRepository models;
    private final AiProviderRepository providers;
    private final EnterpriseIdentityContext identity;
    public AiExecutionCatalogue(AiModelRepository models, AiProviderRepository providers,
            EnterpriseIdentityContext identity) {
        this.models = models; this.providers = providers; this.identity = identity;
    }
    @Transactional(readOnly = true)
    public Selection resolve(UUID tenantId, String modelCode) {
        identity.requireTenant(tenantId);
        AiModel model = models.findByTenantIdAndCode(tenantId, modelCode).orElseThrow();
        AiProvider provider = providers.findByTenantIdAndCode(tenantId, model.providerCode()).orElseThrow();
        if (!model.enabled() || !provider.enabled() || model.getStatus() != EntityStatus.ACTIVE
                || provider.getStatus() != EntityStatus.ACTIVE)
            throw new IllegalStateException("AI provider/model is inactive");
        if (model.capability() != AiEnums.Capability.COMPLETION && model.capability() != AiEnums.Capability.CHAT
                && model.capability() != AiEnums.Capability.SUMMARIZATION)
            throw new IllegalStateException("Model is not configured for supported text execution");
        return new Selection(provider.code(), provider.type(), model.providerModel(),
                model.maxOutputTokens() == null ? 2048 : model.maxOutputTokens());
    }
    public record Selection(String providerCode, AiEnums.ProviderType type, String model, int outputTokens) {}
}
