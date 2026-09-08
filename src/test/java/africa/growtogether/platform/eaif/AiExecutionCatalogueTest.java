package africa.growtogether.platform.eaif;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.persistence.EntityStatus;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AiExecutionCatalogueTest {
    @Test void resolvesExistingCatalogueAndRejectsInactiveProvider() {
        UUID tenant = UUID.randomUUID();
        var models = mock(AiModelRepository.class);
        var providers = mock(AiProviderRepository.class);
        var identity = mock(EnterpriseIdentityContext.class);
        var model = new AiModel(tenant, "MODEL", "PROVIDER", "configured-model", AiEnums.Capability.CHAT);
        var provider = new AiProvider(tenant, "PROVIDER", "Initial provider", AiEnums.ProviderType.OPENAI_COMPATIBLE);
        when(models.findByTenantIdAndCode(tenant, "MODEL")).thenReturn(Optional.of(model));
        when(providers.findByTenantIdAndCode(tenant, "PROVIDER")).thenReturn(Optional.of(provider));
        var catalogue = new AiExecutionCatalogue(models, providers, identity);
        assertEquals("configured-model", catalogue.resolve(tenant, "MODEL").model());
        provider.setStatus(EntityStatus.INACTIVE);
        assertThrows(IllegalStateException.class, () -> catalogue.resolve(tenant, "MODEL"));
    }
}
