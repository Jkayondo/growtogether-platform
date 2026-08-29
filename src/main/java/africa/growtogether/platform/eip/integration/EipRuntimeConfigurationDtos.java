package africa.growtogether.platform.eip.integration;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class EipRuntimeConfigurationDtos {

    private EipRuntimeConfigurationDtos() {
    }

    public record SetExternalDeliveryCommand(
            @NotNull Boolean enabled,
            @Size(max = 500) String reason
    ) {
    }

    public record ExternalDeliveryView(
            boolean enabled
    ) {
    }
}
