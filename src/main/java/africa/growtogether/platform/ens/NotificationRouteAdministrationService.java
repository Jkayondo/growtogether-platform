package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eip.ExternalConnectorReadGateway;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static africa.growtogether.platform.ens.NotificationRouteAdministrationDtos.*;

@Service
public class NotificationRouteAdministrationService {

    private final EnterpriseIdentityContext identity;
    private final NotificationChannelRouteRepository routes;
    private final ExternalConnectorReadGateway connectors;

    public NotificationRouteAdministrationService(
            EnterpriseIdentityContext identity,
            NotificationChannelRouteRepository routes,
            ExternalConnectorReadGateway connectors
    ) {
        this.identity = identity;
        this.routes = routes;
        this.connectors = connectors;
    }

    @Transactional
    public RouteView create(
            CreateRouteCommand command
    ) {
        UUID tenantId = identity.tenantId();

        if (
                routes
                        .findByTenantIdAndChannelAndConnectorId(
                                tenantId,
                                command.channel(),
                                command.connectorId()
                        )
                        .isPresent()
        ) {
            throw new IllegalArgumentException(
                    "Notification provider route already exists"
            );
        }

        if (
                routes.existsByTenantIdAndChannelAndPriority(
                        tenantId,
                        command.channel(),
                        command.priority()
                )
        ) {
            throw new IllegalArgumentException(
                    "Notification provider route priority is already in use"
            );
        }

        /*
         * Reuse the authoritative EIP connector boundary.
         * This proves the connector belongs to the authenticated tenant
         * and prevents cross-tenant provider routing.
         */
        var connector =
                connectors.require(
                        tenantId,
                        command.connectorId()
                );

        if (!connector.isUsable()) {
            throw new IllegalArgumentException(
                    "External connector is not active and usable"
            );
        }

        NotificationChannelRoute route =
                new NotificationChannelRoute(
                        tenantId,
                        command.channel(),
                        command.connectorId(),
                        command.priority()
                );

        if (Boolean.FALSE.equals(command.failoverEnabled())) {
            route.disableFailover();
        }

        return RouteView.from(
                routes.save(route)
        );
    }

    @Transactional(readOnly = true)
    public List<RouteView> list() {
        UUID tenantId = identity.tenantId();

        return routes
                .findByTenantIdOrderByChannelAscPriorityAsc(
                        tenantId
                )
                .stream()
                .map(RouteView::from)
                .toList();
    }
}
