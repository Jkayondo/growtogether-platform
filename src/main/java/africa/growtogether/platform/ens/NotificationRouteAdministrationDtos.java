package africa.growtogether.platform.ens;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public final class NotificationRouteAdministrationDtos {

    private NotificationRouteAdministrationDtos() {
    }

    public record CreateRouteCommand(
            @NotNull NotificationChannel channel,
            @NotNull UUID connectorId,
            @Min(1) int priority,
            Boolean failoverEnabled
    ) {
    }

    public record RouteView(
            UUID id,
            NotificationChannel channel,
            UUID connectorId,
            int priority,
            boolean enabled,
            boolean failoverEnabled
    ) {
        static RouteView from(
                NotificationChannelRoute route
        ) {
            return new RouteView(
                    route.id(),
                    route.channel(),
                    route.connectorId(),
                    route.priority(),
                    route.enabled(),
                    route.failoverEnabled()
            );
        }
    }
}
