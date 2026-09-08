package africa.growtogether.platform.eip;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.execution.AiTextRequest;
import africa.growtogether.platform.eaif.execution.AiTextResult;
import africa.growtogether.platform.eip.integration.EipConfigurationGateway;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class AiProviderExecutionGateway {
    private final ExternalProviderExecutionGate gate;
    private final ExternalConnectorRepository connectors;
    private final ExternalConnectorCredentialResolver credentials;
    private final EipConfigurationGateway configuration;
    private final EnterpriseIdentityContext identity;
    private final Map<String, AiProviderAdapter> adapters;

    public AiProviderExecutionGateway(ExternalProviderExecutionGate gate,
            ExternalConnectorRepository connectors, ExternalConnectorCredentialResolver credentials,
            EipConfigurationGateway configuration, EnterpriseIdentityContext identity,
            List<AiProviderAdapter> adapters) {
        this.gate = gate; this.connectors = connectors; this.credentials = credentials;
        this.configuration = configuration; this.identity = identity;
        Map<String, AiProviderAdapter> indexed = new HashMap<>();
        for (AiProviderAdapter adapter : adapters) {
            if (indexed.putIfAbsent(adapter.connectorType().toUpperCase(Locale.ROOT), adapter) != null)
                throw new IllegalStateException("Duplicate AI provider adapter");
        }
        this.adapters = Map.copyOf(indexed);
    }

    public AiTextResult execute(UUID tenantId, String providerCode, AiEnums.ProviderType type,
            AiTextRequest request) {
        identity.requireTenant(tenantId);
        if (!identity.hasPermission("ai.runtime.execute"))
            throw new org.springframework.security.access.AccessDeniedException("AI execution permission required");
        var matches = connectors.findByTenantIdOrderByConnectorCode(tenantId).stream()
                .filter(c -> c.connectorCode().equals(providerCode)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Exactly one matching AI connector is required");
        ExternalConnector connector = matches.get(0);
        var authorization = gate.authorize(tenantId, connector.id());
        connector = connectors.findByTenantIdAndId(tenantId, connector.id()).orElseThrow();
        if (!connector.active() || connector.getStatus() != EntityStatus.ACTIVE
                || !connector.connectorCode().equals(providerCode)
                || !authorization.connectorType().equalsIgnoreCase(connector.connectorType()))
            throw new IllegalStateException("AI connector changed or is inactive");
        AiProviderAdapter adapter = adapters.get(connector.connectorType().toUpperCase(Locale.ROOT));
        if (adapter == null || adapter.providerType() != type)
            throw new IllegalStateException("No matching AI provider adapter");
        var context = new ExternalProviderExecutionContext(connector.id(), connector.connectorCode(),
                connector.connectorType(), connector.baseUrl(), connector.authType(),
                credentials.resolve(tenantId, connector.id()), connector.providerConfiguration(),
                configuration.requestTimeout(tenantId));
        return Objects.requireNonNull(adapter.execute(context, request), "AI adapter returned no result");
    }
}
