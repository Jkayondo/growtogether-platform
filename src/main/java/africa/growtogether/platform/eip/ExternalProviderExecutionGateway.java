package africa.growtogether.platform.eip;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.eip.integration.EipConfigurationGateway;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ExternalProviderExecutionGateway {

    private final ExternalProviderExecutionGate executionGate;
    private final ExternalConnectorRepository connectors;
    private final ExternalConnectorCredentialResolver credentials;
    private final ExternalProviderAdapterRegistry adapters;
    private final EipConfigurationGateway configuration;

    public ExternalProviderExecutionGateway(
            ExternalProviderExecutionGate executionGate,
            ExternalConnectorRepository connectors,
            ExternalConnectorCredentialResolver credentials,
            ExternalProviderAdapterRegistry adapters,
            EipConfigurationGateway configuration
    ) {
        this.executionGate = executionGate;
        this.connectors = connectors;
        this.credentials = credentials;
        this.adapters = adapters;
        this.configuration = configuration;
    }

    public ExternalProviderDispatchResult dispatch(
            UUID tenantId,
            UUID connectorId,
            ExternalProviderDispatchRequest request
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                connectorId,
                "connectorId must not be null"
        );

        Objects.requireNonNull(
                request,
                "request must not be null"
        );

        ExternalProviderExecutionAuthorization authorization =
                executionGate.authorize(
                        tenantId,
                        connectorId
                );

        ExternalConnector connector =
                connectors
                        .findByTenantIdAndId(
                                tenantId,
                                connectorId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "External connector was not found"
                                )
                        );

        if (
                !connector.active()
                || connector.getStatus() != EntityStatus.ACTIVE
        ) {
            throw new IllegalStateException(
                    "External connector is no longer active"
            );
        }

        if (
                !authorization.connectorType()
                        .equalsIgnoreCase(
                                connector.connectorType()
                        )
        ) {
            throw new IllegalStateException(
                    "External connector type changed after authorization"
            );
        }

        String credential =
                credentials.resolve(
                        tenantId,
                        connectorId
                );

        Duration requestTimeout =
                configuration.requestTimeout(
                        tenantId
                );

        ExternalProviderExecutionContext context =
                new ExternalProviderExecutionContext(
                        connector.id(),
                        connector.connectorCode(),
                        connector.connectorType(),
                        connector.baseUrl(),
                        connector.authType(),
                        credential,
                        connector.providerConfiguration(),
                        requestTimeout
                );

        ExternalProviderAdapter adapter =
                adapters.require(
                        connector.connectorType()
                );

        ExternalProviderDispatchResult result =
                adapter.dispatch(
                        context,
                        request
                );

        if (result == null) {
            throw new IllegalStateException(
                    "External provider adapter returned no result"
            );
        }

        return result;
    }
}
