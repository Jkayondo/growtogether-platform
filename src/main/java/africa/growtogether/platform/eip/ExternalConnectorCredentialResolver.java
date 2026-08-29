package africa.growtogether.platform.eip;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ExternalConnectorCredentialResolver {

    private final ExternalConnectorRepository connectors;
    private final IntegrationCredentialCrypto crypto;

    ExternalConnectorCredentialResolver(
            ExternalConnectorRepository connectors,
            IntegrationCredentialCrypto crypto
    ) {
        this.connectors = connectors;
        this.crypto = crypto;
    }

    @Transactional(readOnly = true)
    String resolve(
            UUID tenantId,
            UUID connectorId
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                connectorId,
                "connectorId must not be null"
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

        return crypto.decrypt(
                connector.credentialCiphertext(),
                connector.credentialKeyId()
        );
    }
}
