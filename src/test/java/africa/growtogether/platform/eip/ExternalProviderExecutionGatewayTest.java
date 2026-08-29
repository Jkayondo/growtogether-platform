package africa.growtogether.platform.eip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.eip.integration.EipConfigurationGateway;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ExternalProviderExecutionGatewayTest {

    private ExternalProviderExecutionGate executionGate;
    private ExternalConnectorRepository connectors;
    private ExternalConnectorCredentialResolver credentials;
    private ExternalProviderAdapterRegistry adapters;
    private EipConfigurationGateway configuration;
    private ExternalProviderExecutionGateway gateway;

    private UUID tenantId;
    private UUID connectorId;
    private UUID certificationId;
    private ExternalProviderDispatchRequest request;

    @BeforeEach
    void setUp() {
        executionGate =
                mock(ExternalProviderExecutionGate.class);

        connectors =
                mock(ExternalConnectorRepository.class);

        credentials =
                mock(ExternalConnectorCredentialResolver.class);

        adapters =
                mock(ExternalProviderAdapterRegistry.class);

        configuration =
                mock(EipConfigurationGateway.class);

        gateway =
                new ExternalProviderExecutionGateway(
                        executionGate,
                        connectors,
                        credentials,
                        adapters,
                        configuration
                );

        tenantId = UUID.randomUUID();
        connectorId = UUID.randomUUID();
        certificationId = UUID.randomUUID();

        request =
                new ExternalProviderDispatchRequest(
                        "WHATSAPP",
                        "+256700000000",
                        null,
                        "Welcome to GrowTogether",
                        "corr-001",
                        "notification-001",
                        Map.of(
                                "language",
                                "en"
                        )
                );
    }

    @Test
    void dispatchesThroughAuthorizedProviderAdapter() {
        ExternalConnector connector =
                activeConnector("WHATSAPP");

        ExternalProviderAdapter adapter =
                mock(ExternalProviderAdapter.class);

        ExternalProviderDispatchResult expected =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.ACCEPTED,
                        "provider-request-001",
                        "provider-reference-001",
                        "200",
                        "Accepted"
                );

        authorize("WHATSAPP");

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.of(connector));

        when(
                credentials.resolve(
                        tenantId,
                        connectorId
                )
        ).thenReturn("provider-secret");

        when(configuration.requestTimeout(tenantId))
                .thenReturn(Duration.ofSeconds(30));

        when(adapters.require("WHATSAPP"))
                .thenReturn(adapter);

        when(
                adapter.dispatch(
                        any(ExternalProviderExecutionContext.class),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(expected);

        ExternalProviderDispatchResult actual =
                gateway.dispatch(
                        tenantId,
                        connectorId,
                        request
                );

        assertThat(actual)
                .isSameAs(expected);

        ArgumentCaptor<ExternalProviderExecutionContext> context =
                ArgumentCaptor.forClass(
                        ExternalProviderExecutionContext.class
                );

        verify(adapter)
                .dispatch(
                        context.capture(),
                        org.mockito.ArgumentMatchers.same(request)
                );

        assertThat(context.getValue().connectorId())
                .isEqualTo(connectorId);

        assertThat(context.getValue().connectorCode())
                .isEqualTo("PRIMARY");

        assertThat(context.getValue().connectorType())
                .isEqualTo("WHATSAPP");

        assertThat(context.getValue().baseUrl())
                .isEqualTo(
                        "https://provider.example.test"
                );

        assertThat(context.getValue().authType())
                .isEqualTo("BEARER");

        assertThat(context.getValue().credential())
                .isEqualTo("provider-secret");

        assertThat(context.getValue().requestTimeout())
                .isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void authorizationFailureStopsExecutionBeforeConnectorLookup() {
        when(
                executionGate.authorize(
                        tenantId,
                        connectorId
                )
        ).thenThrow(
                new IllegalStateException(
                        "External provider delivery is disabled"
                )
        );

        assertThatThrownBy(
                () -> gateway.dispatch(
                        tenantId,
                        connectorId,
                        request
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "External provider delivery is disabled"
                );

        verify(connectors, never())
                .findByTenantIdAndId(
                        tenantId,
                        connectorId
                );

        verify(credentials, never())
                .resolve(
                        tenantId,
                        connectorId
                );
    }

    @Test
    void rejectsConnectorMissingAfterAuthorization() {
        authorize("WHATSAPP");

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> gateway.dispatch(
                        tenantId,
                        connectorId,
                        request
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "External connector was not found"
                );

        verify(credentials, never())
                .resolve(
                        tenantId,
                        connectorId
                );
    }

    @Test
    void rejectsConnectorDisabledAfterAuthorization() {
        ExternalConnector connector =
                mock(ExternalConnector.class);

        when(connector.active())
                .thenReturn(false);

        when(connector.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        authorize("WHATSAPP");

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.of(connector));

        assertThatThrownBy(
                () -> gateway.dispatch(
                        tenantId,
                        connectorId,
                        request
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "External connector is no longer active"
                );

        verify(credentials, never())
                .resolve(
                        tenantId,
                        connectorId
                );
    }

    @Test
    void rejectsConnectorEntityThatBecameInactive() {
        ExternalConnector connector =
                mock(ExternalConnector.class);

        when(connector.active())
                .thenReturn(true);

        when(connector.getStatus())
                .thenReturn(EntityStatus.INACTIVE);

        authorize("WHATSAPP");

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.of(connector));

        assertThatThrownBy(
                () -> gateway.dispatch(
                        tenantId,
                        connectorId,
                        request
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "External connector is no longer active"
                );
    }

    @Test
    void rejectsConnectorTypeChangedAfterAuthorization() {
        ExternalConnector connector =
                activeConnector("SMS");

        authorize("WHATSAPP");

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.of(connector));

        assertThatThrownBy(
                () -> gateway.dispatch(
                        tenantId,
                        connectorId,
                        request
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "External connector type changed after authorization"
                );

        verify(credentials, never())
                .resolve(
                        tenantId,
                        connectorId
                );
    }

    @Test
    void missingAdapterFailsWithoutProviderDispatch() {
        ExternalConnector connector =
                activeConnector("WHATSAPP");

        authorize("WHATSAPP");

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.of(connector));

        when(
                credentials.resolve(
                        tenantId,
                        connectorId
                )
        ).thenReturn("provider-secret");

        when(configuration.requestTimeout(tenantId))
                .thenReturn(Duration.ofSeconds(30));

        when(adapters.require("WHATSAPP"))
                .thenThrow(
                        new IllegalStateException(
                                "No external provider adapter registered for WHATSAPP"
                        )
                );

        assertThatThrownBy(
                () -> gateway.dispatch(
                        tenantId,
                        connectorId,
                        request
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "No external provider adapter registered for WHATSAPP"
                );
    }

    @Test
    void rejectsNullProviderAdapterResult() {
        ExternalConnector connector =
                activeConnector("WHATSAPP");

        ExternalProviderAdapter adapter =
                mock(ExternalProviderAdapter.class);

        authorize("WHATSAPP");

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.of(connector));

        when(
                credentials.resolve(
                        tenantId,
                        connectorId
                )
        ).thenReturn("provider-secret");

        when(configuration.requestTimeout(tenantId))
                .thenReturn(Duration.ofSeconds(30));

        when(adapters.require("WHATSAPP"))
                .thenReturn(adapter);

        when(
                adapter.dispatch(
                        any(ExternalProviderExecutionContext.class),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(null);

        assertThatThrownBy(
                () -> gateway.dispatch(
                        tenantId,
                        connectorId,
                        request
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "External provider adapter returned no result"
                );
    }

    private void authorize(
            String connectorType
    ) {
        when(
                executionGate.authorize(
                        tenantId,
                        connectorId
                )
        ).thenReturn(
                new ExternalProviderExecutionAuthorization(
                        connectorId,
                        "PRIMARY",
                        connectorType,
                        "PRODUCTION",
                        certificationId
                )
        );
    }

    private ExternalConnector activeConnector(
            String connectorType
    ) {
        ExternalConnector connector =
                mock(ExternalConnector.class);

        when(connector.id())
                .thenReturn(connectorId);

        when(connector.connectorCode())
                .thenReturn("PRIMARY");

        when(connector.connectorType())
                .thenReturn(connectorType);

        when(connector.baseUrl())
                .thenReturn(
                        "https://provider.example.test"
                );

        when(connector.authType())
                .thenReturn("BEARER");

        when(connector.active())
                .thenReturn(true);

        when(connector.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        return connector;
    }
}
