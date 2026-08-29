package africa.growtogether.platform.ens;

import java.util.UUID;

public record NotificationRouteCandidate(

        UUID routeId,

        UUID connectorId,

        String connectorCode,

        String connectorType,

        int priority,

        boolean failoverEnabled

) {
}
