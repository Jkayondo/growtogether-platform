package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.eip.ExternalConnectorReadGateway;
import africa.growtogether.platform.eip.ExternalConnectorSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationRouteResolver {

    private final NotificationChannelRouteRepository routes;
    private final ExternalConnectorReadGateway connectors;

    public NotificationRouteResolver(
            NotificationChannelRouteRepository routes,
            ExternalConnectorReadGateway connectors
    ) {
        this.routes = routes;
        this.connectors = connectors;
    }

    @Transactional(readOnly = true)
    public List<NotificationRouteCandidate> resolve(
            UUID tenantId,
            NotificationChannel channel
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                channel,
                "channel must not be null"
        );

        List<NotificationChannelRoute> configured =
                routes
                        .findByTenantIdAndChannelAndEnabledTrueAndStatusOrderByPriorityAsc(
                                tenantId,
                                channel,
                                EntityStatus.ACTIVE
                        );

        List<NotificationRouteCandidate> candidates =
                new ArrayList<>();

        for (NotificationChannelRoute route : configured) {

            ExternalConnectorSnapshot connector;

            try {
                connector =
                        connectors.require(
                                tenantId,
                                route.connectorId()
                        );
            } catch (IllegalArgumentException exception) {
                /*
                 * A stale or invalid route must never cause ENS to
                 * cross tenant boundaries or expose connector details.
                 * It is simply excluded from the usable route set.
                 */
                continue;
            }

            if (!connector.isUsable()) {
                continue;
            }

            candidates.add(
                    new NotificationRouteCandidate(
                            route.id(),
                            connector.id(),
                            connector.connectorCode(),
                            connector.connectorType(),
                            route.priority(),
                            route.failoverEnabled()
                    )
            );
        }

        return List.copyOf(candidates);
    }

    @Transactional(readOnly = true)
    public NotificationRouteCandidate requirePrimary(
            UUID tenantId,
            NotificationChannel channel
    ) {
        return resolve(
                tenantId,
                channel
        )
                .stream()
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No usable notification provider route configured"
                        )
                );
    }
}
