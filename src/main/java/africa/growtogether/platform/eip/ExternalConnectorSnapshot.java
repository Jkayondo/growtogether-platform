package africa.growtogether.platform.eip;

import java.util.UUID;

public record ExternalConnectorSnapshot(

        UUID id,

        String connectorCode,

        String connectorType,

        boolean active,

        String entityStatus

) {

    public boolean isUsable() {
        return active
                && "ACTIVE".equals(entityStatus);
    }
}
