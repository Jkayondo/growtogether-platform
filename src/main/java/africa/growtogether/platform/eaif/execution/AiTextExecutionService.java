package africa.growtogether.platform.eaif.execution;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eaif.*;
import africa.growtogether.platform.eaif.audit.EaifAuditService;
import africa.growtogether.platform.eaif.approval.EaifApprovalService;
import africa.growtogether.platform.eaif.approval.ApprovalStatus;
import africa.growtogether.platform.eaif.governance.policy.AiGovernancePolicyService;
import africa.growtogether.platform.eaif.integration.EaifConfigurationGateway;
import africa.growtogether.platform.eip.AiProviderExecutionGateway;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Synchronous, single-attempt execution. Product-facing API contains no provider DTOs. */
@Service
public class AiTextExecutionService {
    private final EnterpriseIdentityContext identity;
    private final EaifConfigurationGateway configuration;
    private final AiFoundationService foundation;
    private final AiExecutionCatalogue catalogue;
    private final AiGovernancePolicyService governance;
    private final EaifAuditService audit;
    private final EaifApprovalService approvals;
    private final AiProviderExecutionGateway gateway;
    private final AiExecutionOutputStore outputs;
    private final TransactionTemplate transaction;

    public AiTextExecutionService(EnterpriseIdentityContext identity, EaifConfigurationGateway configuration,
            AiFoundationService foundation, AiExecutionCatalogue catalogue,
            AiGovernancePolicyService governance, EaifAuditService audit,
            AiProviderExecutionGateway gateway, AiExecutionOutputStore outputs,
            PlatformTransactionManager manager, EaifApprovalService approvals) {
        this.identity = identity; this.configuration = configuration; this.foundation = foundation;
        this.catalogue = catalogue; this.governance = governance; this.audit = audit;
        this.gateway = gateway; this.outputs = outputs;
        this.approvals = approvals;
        transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public String execute(UUID tenantId, UUID requestId, String input) {
        identity.requireTenant(tenantId);
        if (!identity.hasPermission("ai.runtime.execute"))
            throw new org.springframework.security.access.AccessDeniedException("AI execution permission required");
        if (!configuration.providerExecutionEnabled()) throw new IllegalStateException("AI execution is disabled");
        if (input == null || input.isBlank() || input.length() > configuration.maximumInputCharacters())
            throw new IllegalArgumentException("Invalid AI input size");

        Claim claim = transaction.execute(status -> {
            AiRequest request = foundation.get(tenantId, requestId);
            if (request.getStatus() != africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
                    || request.requestStatus() != AiEnums.RequestStatus.APPROVED)
                throw new IllegalStateException("AI request must be approved and not previously executed");
            if (!AiTextRequest.hash(input).equals(request.inputHash()))
                throw new IllegalArgumentException("Input does not match the governed request hash");
            if (!governance.allows(tenantId, "DEFAULT_AI_POLICY", request.riskLevel()))
                throw new IllegalStateException("Current governance policy denies execution");
            if ((governance.requiresApproval(tenantId, "DEFAULT_AI_POLICY")
                    || configuration.highRiskApprovalRequired()
                    && (request.riskLevel() == AiEnums.RiskLevel.HIGH || request.riskLevel() == AiEnums.RiskLevel.CRITICAL))
                    && approvals.get(tenantId, requestId).approvalStatus() != ApprovalStatus.APPROVED)
                throw new IllegalStateException("Human approval is required");
            var selected = catalogue.resolve(tenantId, request.modelCode());
            var textRequest = new AiTextRequest(selected.model(), input, selected.outputTokens());
            foundation.begin(tenantId, requestId);
            audit.start(tenantId, requestId);
            return new Claim(selected, textRequest);
        });
        // Optimistic @Version update commits before any network call; a losing claimant cannot dispatch.
        try {
            var result = gateway.execute(tenantId, claim.selection().providerCode(),
                    claim.selection().type(), claim.request());
            return transaction.execute(status -> {
                String reference = outputs.save(tenantId, requestId, result);
                foundation.succeed(tenantId, requestId, reference);
                audit.complete(tenantId, requestId, reference);
                return reference;
            });
        } catch (RuntimeException failure) {
            try {
                transaction.executeWithoutResult(status -> {
                    foundation.fail(tenantId, requestId, "AI_EXECUTION_OR_OUTPUT_STORAGE_FAILED");
                    audit.fail(tenantId, requestId);
                });
            } catch (RuntimeException recordingFailure) {
                throw new IllegalStateException("AI execution requires reconciliation; do not retry automatically");
            }
            throw new IllegalStateException("AI execution failed; do not retry automatically");
        }
    }
    private record Claim(AiExecutionCatalogue.Selection selection, AiTextRequest request) {}
}
