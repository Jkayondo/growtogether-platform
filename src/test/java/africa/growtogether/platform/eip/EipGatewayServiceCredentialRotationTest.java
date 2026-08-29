package africa.growtogether.platform.eip;

import static africa.growtogether.platform.eip.EipGatewayDtos.CredentialRotationCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EipGatewayServiceCredentialRotationTest {

    private EnterpriseIdentityContext identity;
    private ExternalConnectorRepository connectors;
    private IntegrationCredentialCrypto crypto;
    private EipGatewayService service;
    private UUID tenantId;
    private UUID connectorId;

    @BeforeEach
    void setUp() {
        identity = mock(EnterpriseIdentityContext.class);
        connectors = mock(ExternalConnectorRepository.class);
        crypto = new IntegrationCredentialCrypto(
                Base64.getEncoder().encodeToString(new byte[32]),
                "active-key"
        );
        service = new EipGatewayService(
                identity,
                mock(GatewayRouteRepository.class),
                mock(WebhookSubscriptionRepository.class),
                mock(TransformationRuleRepository.class),
                connectors,
                crypto
        );
        tenantId = UUID.randomUUID();
        connectorId = UUID.randomUUID();
        when(identity.tenantId()).thenReturn(tenantId);
        when(connectors.save(any(ExternalConnector.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rotatesCredentialForTenantConnectorAndPreservesMetadata() {
        ExternalConnector connector = new ExternalConnector(
                tenantId,
                "BREVO_PRIMARY_EMAIL",
                "BREVO_EMAIL",
                "https://api.brevo.com",
                "API_KEY",
                crypto.encrypt("old-secret"),
                "active-key",
                "{\"senderName\":\"GrowTogether\"}"
        );
        when(connectors.findByTenantIdAndId(tenantId, connectorId))
                .thenReturn(Optional.of(connector));

        var view = service.rotateConnectorCredential(
                connectorId,
                new CredentialRotationCommand("new-secret")
        );

        verify(connectors).findByTenantIdAndId(tenantId, connectorId);
        verify(connectors).save(connector);

        assertThat(crypto.decrypt(
                connector.credentialCiphertext(),
                connector.credentialKeyId()
        )).isEqualTo("new-secret");
        assertThat(connector.credentialCiphertext()).doesNotContain("new-secret");
        assertThat(connector.credentialKeyId()).isEqualTo("active-key");

        assertThat(view.connectorCode()).isEqualTo("BREVO_PRIMARY_EMAIL");
        assertThat(view.connectorType()).isEqualTo("BREVO_EMAIL");
        assertThat(view.baseUrl()).isEqualTo("https://api.brevo.com");
        assertThat(view.authType()).isEqualTo("API_KEY");
        assertThat(view.providerConfiguration())
                .isEqualTo("{\"senderName\":\"GrowTogether\"}");
        assertThat(view.active()).isTrue();
    }

    @Test
    void missingTenantConnectorIsRejectedWithoutSave() {
        when(connectors.findByTenantIdAndId(tenantId, connectorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotateConnectorCredential(
                connectorId,
                new CredentialRotationCommand("new-secret")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("External connector was not found");

        verify(connectors).findByTenantIdAndId(tenantId, connectorId);
        verify(connectors, never()).save(any());
    }
}
