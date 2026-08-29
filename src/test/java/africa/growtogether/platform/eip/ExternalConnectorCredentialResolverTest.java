package africa.growtogether.platform.eip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Modifier;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalConnectorCredentialResolverTest {

    private ExternalConnectorRepository connectors;
    private IntegrationCredentialCrypto crypto;
    private ExternalConnectorCredentialResolver resolver;

    private UUID tenantId;
    private UUID connectorId;

    @BeforeEach
    void setUp() {
        connectors =
                mock(ExternalConnectorRepository.class);

        crypto =
                new IntegrationCredentialCrypto(
                        Base64.getEncoder()
                                .encodeToString(
                                        new byte[32]
                                ),
                        "active-key"
                );

        resolver =
                new ExternalConnectorCredentialResolver(
                        connectors,
                        crypto
                );

        tenantId = UUID.randomUUID();
        connectorId = UUID.randomUUID();
    }

    @Test
    void resolvesAndDecryptsCredentialForTenantConnector() {
        String ciphertext =
                crypto.encrypt(
                        "provider-secret"
                );

        ExternalConnector connector =
                connector(
                        ciphertext,
                        "active-key"
                );

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.of(connector));

        assertThat(
                resolver.resolve(
                        tenantId,
                        connectorId
                )
        ).isEqualTo("provider-secret");

        verify(connectors)
                .findByTenantIdAndId(
                        tenantId,
                        connectorId
                );
    }

    @Test
    void lookupIsExplicitlyTenantScoped() {
        UUID otherTenant =
                UUID.randomUUID();

        when(
                connectors.findByTenantIdAndId(
                        otherTenant,
                        connectorId
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> resolver.resolve(
                        otherTenant,
                        connectorId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "External connector was not found"
                );

        verify(connectors)
                .findByTenantIdAndId(
                        otherTenant,
                        connectorId
                );
    }

    @Test
    void rejectsMissingConnector() {
        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> resolver.resolve(
                        tenantId,
                        connectorId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "External connector was not found"
                );
    }

    @Test
    void connectorWithoutCredentialResolvesToNull() {
        ExternalConnector connector =
                connector(
                        null,
                        null
                );

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.of(connector));

        assertThat(
                resolver.resolve(
                        tenantId,
                        connectorId
                )
        ).isNull();
    }

    @Test
    void rejectsCredentialEncryptedUnderDifferentKeyId() {
        String ciphertext =
                crypto.encrypt("provider-secret");

        ExternalConnector connector =
                connector(
                        ciphertext,
                        "retired-key"
                );

        when(
                connectors.findByTenantIdAndId(
                        tenantId,
                        connectorId
                )
        ).thenReturn(Optional.of(connector));

        assertThatThrownBy(
                () -> resolver.resolve(
                        tenantId,
                        connectorId
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Integration credential key id does not match active key"
                );
    }

    @Test
    void resolverAndResolveMethodRemainNonPublic() throws Exception {
        assertThat(
                Modifier.isPublic(
                        ExternalConnectorCredentialResolver
                                .class
                                .getModifiers()
                )
        ).isFalse();

        var resolveMethod =
                ExternalConnectorCredentialResolver
                        .class
                        .getDeclaredMethod(
                                "resolve",
                                UUID.class,
                                UUID.class
                        );

        assertThat(
                Modifier.isPublic(
                        resolveMethod.getModifiers()
                )
        ).isFalse();
    }

    private ExternalConnector connector(
            String ciphertext,
            String keyId
    ) {
        return new ExternalConnector(
                tenantId,
                "WHATSAPP_PRIMARY",
                "WHATSAPP",
                "https://provider.example.test",
                "BEARER",
                ciphertext,
                keyId
        );
    }
}
