package africa.growtogether.platform.eip;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.execution.*;
import africa.growtogether.platform.eip.integration.EipConfigurationGateway;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AiProviderExecutionGatewayTest {
    final UUID tenant = UUID.randomUUID(), id = UUID.randomUUID();
    final ExternalProviderExecutionGate gate = mock(ExternalProviderExecutionGate.class);
    final ExternalConnectorRepository connectors = mock(ExternalConnectorRepository.class);
    final ExternalConnectorCredentialResolver credentials = mock(ExternalConnectorCredentialResolver.class);
    final EipConfigurationGateway config = mock(EipConfigurationGateway.class);
    final EnterpriseIdentityContext identity = mock(EnterpriseIdentityContext.class);
    final AiProviderAdapter adapter = mock(AiProviderAdapter.class);
    final AiTextRequest request = new AiTextRequest("model", "evidence", 100);
    AiProviderExecutionGateway gateway;
    @BeforeEach void setup() {
        when(adapter.connectorType()).thenReturn("OPENAI_RESPONSES");
        when(adapter.providerType()).thenReturn(AiEnums.ProviderType.OPENAI_COMPATIBLE);
        when(identity.hasPermission("ai.runtime.execute")).thenReturn(true);
        var connector = new ExternalConnector(tenant, "PROVIDER", "OPENAI_RESPONSES",
                "https://api.openai.com", "API_KEY", "encrypted", "key-id");
        ReflectionTestUtils.setField(connector, "id", id);
        when(connectors.findByTenantIdOrderByConnectorCode(tenant)).thenReturn(List.of(connector));
        when(connectors.findByTenantIdAndId(tenant, id)).thenReturn(Optional.of(connector));
        when(gate.authorize(tenant, id)).thenReturn(new ExternalProviderExecutionAuthorization(
                id, "PROVIDER", "OPENAI_RESPONSES", "TEST", UUID.randomUUID()));
        when(config.requestTimeout(tenant)).thenReturn(Duration.ofSeconds(5));
        gateway = new AiProviderExecutionGateway(gate, connectors, credentials, config, identity, List.of(adapter));
    }
    @Test void resolvesCredentialsOnlyAfterCertificationGate() {
        when(credentials.resolve(tenant, id)).thenReturn("test-key");
        var result = new AiTextResult("resp_1", "output");
        when(adapter.execute(any(), eq(request))).thenReturn(result);
        assertSame(result, gateway.execute(tenant, "PROVIDER", AiEnums.ProviderType.OPENAI_COMPATIBLE, request));
        var order = inOrder(gate, credentials, adapter);
        order.verify(gate).authorize(tenant, id);
        order.verify(adapter).providerType();
        order.verify(credentials).resolve(tenant, id);
        order.verify(adapter).execute(any(), eq(request));
    }
    @Test void deniedCertificationNeverResolvesCredentialOrExecutes() {
        when(gate.authorize(tenant, id)).thenThrow(new IllegalStateException("Uncertified"));
        assertThrows(IllegalStateException.class, () -> gateway.execute(tenant, "PROVIDER", AiEnums.ProviderType.OPENAI_COMPATIBLE, request));
        verifyNoInteractions(credentials);
        verify(adapter, never()).execute(any(), any());
    }
    @Test void rejectsProviderTypeMismatch() {
        assertThrows(IllegalStateException.class, () -> gateway.execute(tenant, "PROVIDER", AiEnums.ProviderType.ANTHROPIC, request));
        verifyNoInteractions(credentials);
    }
    @Test void failsStartupForDuplicateAdapterType() {
        assertThrows(IllegalStateException.class, () -> new AiProviderExecutionGateway(
                gate, connectors, credentials, config, identity, List.of(adapter, adapter)));
    }
}
