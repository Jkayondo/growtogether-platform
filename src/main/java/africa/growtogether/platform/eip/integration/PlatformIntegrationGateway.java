package africa.growtogether.platform.eip.integration;

import africa.growtogether.platform.eip.IntegrationDtos.PublishCommand;
import africa.growtogether.platform.eip.IntegrationProtocol;
import africa.growtogether.platform.eip.IntegrationRuntimeService;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Reusable event gateway for EWE, ENS, EDS and future GT services. */
@Component
public class PlatformIntegrationGateway {
    private final IntegrationRuntimeService runtime;
    public PlatformIntegrationGateway(IntegrationRuntimeService runtime) { this.runtime = runtime; }

    public UUID publish(String eventType, String sourceService, String destination, String payload,
                        String correlationId, String idempotencyKey) {
        var result = runtime.publish(new PublishCommand(eventType, "1.0", sourceService, destination,
            IntegrationProtocol.INTERNAL_EVENT, payload, "{}", correlationId, idempotencyKey, 5));
        return result.id();
    }
}
