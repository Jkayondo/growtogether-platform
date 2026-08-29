package africa.growtogether.platform.eip;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.eip.integration.EipConfigurationGateway;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalProviderExecutionGate {

    private final ExternalConnectorReadGateway connectors;
    private final ConnectorCertificationRepository certifications;
    private final EipConfigurationGateway configuration;

    public ExternalProviderExecutionGate(
            ExternalConnectorReadGateway connectors,
            ConnectorCertificationRepository certifications,
            EipConfigurationGateway configuration
    ) {
        this.connectors = connectors;
        this.certifications = certifications;
        this.configuration = configuration;
    }

    @Transactional(readOnly = true)
    public ExternalProviderExecutionAuthorization authorize(
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

        if (!configuration.externalDeliveryEnabled(tenantId)) {
            throw new IllegalStateException(
                    "External provider delivery is disabled"
            );
        }

        ExternalConnectorSnapshot connector =
                connectors.require(
                        tenantId,
                        connectorId
                );

        if (!connector.isUsable()) {
            throw new IllegalStateException(
                    "External connector is not active"
            );
        }

        String environment =
                configuration.executionEnvironment(tenantId);

        ConnectorCertification certification =
                certifications
                        .findByTenantIdAndConnectorIdAndEnvironmentIgnoreCase(
                                tenantId,
                                connectorId,
                                environment
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "External connector is not certified for "
                                                + environment
                                )
                        );

        if (certification.getStatus() != EntityStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Connector certification record is not active"
            );
        }

        if (!certification.isCertified()) {
            throw new IllegalStateException(
                    "External connector certification is not CERTIFIED"
            );
        }

        if (!certification.hasCertificationTimestamp()) {
            throw new IllegalStateException(
                    "External connector certification has no certification timestamp"
            );
        }

        if (certification.isExpiredAt(Instant.now())) {
            throw new IllegalStateException(
                    "External connector certification has expired"
            );
        }

        return new ExternalProviderExecutionAuthorization(
                connector.id(),
                connector.connectorCode(),
                connector.connectorType(),
                environment,
                certification.id()
        );
    }
}
