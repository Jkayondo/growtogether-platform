package africa.growtogether.platform.eaif.integration;

import africa.growtogether.platform.common.api.*;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eaif.*;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/integration")
public class EaifIntegrationController {
    private final AiFoundationService service;
    private final EaifConfigurationGateway configuration;
    private final EaifPlatformIntegrationGateway platform;
    private final EnterpriseIdentityContext identity;
    private final ApiResponses responses;

    public EaifIntegrationController(AiFoundationService service, EaifConfigurationGateway configuration,
                                     EaifPlatformIntegrationGateway platform, EnterpriseIdentityContext identity,
                                     ApiResponses responses) {
        this.service = service; this.configuration = configuration; this.platform = platform;
        this.identity = identity; this.responses = responses;
    }

    @PostMapping("/requests/{id}/provider-execution")
    @PreAuthorize("hasAuthority('ai.runtime.execute')")
    public ApiResponse<Map<String,Object>> requestProviderExecution(@PathVariable UUID id,
        @RequestParam(defaultValue="GENERAL") String capability,
        @RequestParam(required=false) String inputReference) {
        if (!configuration.providerExecutionEnabled()) {
            throw new IllegalStateException("AI provider execution is disabled by ECS policy.");
        }
        AiRequest request = service.get(identity.tenantId(), id);
        UUID messageId = platform.publishProviderExecution(id, request.modelCode(), capability, inputReference);
        return responses.success("GT-EAIF-INT-001", "Provider execution intent published.",
            Map.of("requestId", id, "integrationMessageId", messageId));
    }

    @PostMapping("/requests/{id}/approval")
    @PreAuthorize("hasAuthority('ai.request.approval')")
    public ApiResponse<Map<String,Object>> requestApproval(@PathVariable UUID id) {
        AiRequest request = service.get(identity.tenantId(), id);
        UUID messageId = platform.publishApprovalRequest(id, request.useCase(), request.riskLevel().name());
        return responses.success("GT-EAIF-INT-002", "AI approval intent published.",
            Map.of("requestId", id, "integrationMessageId", messageId));
    }
}
