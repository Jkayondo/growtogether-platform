package africa.growtogether.platform.eip;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalConnectorReadGateway {

    private final ExternalConnectorRepository connectors;

    public ExternalConnectorReadGateway(
            ExternalConnectorRepository connectors
    ) {
        this.connectors = connectors;
    }

    @Transactional(readOnly = true)
    public ExternalConnectorSnapshot require(
            UUID tenantId,
            UUID connectorId
    ) {
        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (connectorId == null) {
            throw new IllegalArgumentException(
                    "connectorId must not be null"
            );
        }

        ExternalConnector connector =
                connectors
                        .findByTenantIdAndId(
                                tenantId,
                                connectorId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "External connector not found for tenant"
                                )
                        );

        return snapshot(connector);
    }

    private ExternalConnectorSnapshot snapshot(
            ExternalConnector connector
    ) {
        return new ExternalConnectorSnapshot(
                connector.id(),
                connector.connectorCode(),
                connector.connectorType(),
                connector.active(),
                connector.getStatus().name()
        );
    }
}
