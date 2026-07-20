package africa.growtogether.platform.eaif.integration;

import africa.growtogether.platform.eip.integration.PlatformIntegrationGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Publishes AI orchestration intent through EIP instead of calling providers directly. */
@Component
public class EaifPlatformIntegrationGateway {
    private final PlatformIntegrationGateway integration;
    public EaifPlatformIntegrationGateway(PlatformIntegrationGateway integration) { this.integration = integration; }

    public UUID publishProviderExecution(UUID requestId, String modelCode, String capability, String inputReference) {
        String payload = "{\"requestId\":\"" + requestId + "\",\"modelCode\":\"" + escape(modelCode)
            + "\",\"capability\":\"" + escape(capability) + "\",\"inputReference\":\"" + escape(inputReference) + "\"}";
        return integration.publish("AiProviderExecutionRequested", "EAIF", "AI_PROVIDER", payload,
            requestId.toString(), "eaif-provider-" + requestId);
    }

    public UUID publishAnalytics(UUID requestId, String status, String modelCode, String riskLevel) {
        String payload = "{\"requestId\":\"" + requestId + "\",\"status\":\"" + escape(status)
            + "\",\"modelCode\":\"" + escape(modelCode) + "\",\"riskLevel\":\"" + escape(riskLevel) + "\"}";
        return integration.publish("AiRequestLifecycleChanged", "EAIF", "EAP", payload,
            requestId.toString(), "eaif-analytics-" + requestId + "-" + status);
    }

    public UUID publishApprovalRequest(UUID requestId, String useCase, String riskLevel) {
        String payload = "{\"requestId\":\"" + requestId + "\",\"useCase\":\"" + escape(useCase)
            + "\",\"riskLevel\":\"" + escape(riskLevel) + "\"}";
        return integration.publish("AiApprovalRequested", "EAIF", "EWE", payload,
            requestId.toString(), "eaif-approval-" + requestId);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
