package africa.growtogether.platform.ens;

import africa.growtogether.platform.eip.ExternalProviderDispatchRequest;
import africa.growtogether.platform.eip.ExternalProviderDispatchResult;
import africa.growtogether.platform.eip.ExternalProviderExecutionGateway;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationExternalProviderExecutionService {

    private final ExternalProviderExecutionGateway gateway;

    public NotificationExternalProviderExecutionService(
            ExternalProviderExecutionGateway gateway
    ) {
        this.gateway = gateway;
    }

    /*
     * External provider traffic must never execute while ENS holds
     * a database transaction or notification-row lock.
     *
     * NOT_SUPPORTED suspends an existing transaction, if one exists,
     * for the duration of the external provider operation.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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

        return gateway.dispatch(
                tenantId,
                connectorId,
                request
        );
    }
}
