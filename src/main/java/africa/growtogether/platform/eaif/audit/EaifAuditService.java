package africa.growtogether.platform.eaif.audit;

import africa.growtogether.platform.eaif.AiEnums;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EaifAuditService {

    private final EaifExecutionAuditRepository audits;

    public EaifAuditService(EaifExecutionAuditRepository audits) {
        this.audits = audits;
    }

    @Transactional
    public EaifExecutionAudit create(
            UUID tenantId,
            UUID requestId,
            String sourceService,
            String modelCode,
            String promptCode,
            AiEnums.RiskLevel riskLevel,
            UUID actorUserId
    ) {
        return audits.save(
            new EaifExecutionAudit(
                tenantId,
                requestId,
                sourceService,
                modelCode,
                promptCode,
                riskLevel,
                actorUserId
            )
        );
    }

    @Transactional
    public EaifExecutionAudit start(UUID tenantId, UUID requestId) {
        EaifExecutionAudit audit = get(tenantId, requestId);
        audit.approve();
        audit.startProcessing();
        return audit;
    }

    @Transactional
    public EaifExecutionAudit complete(
            UUID tenantId,
            UUID requestId,
            String outputReference
    ) {
        EaifExecutionAudit audit = get(tenantId, requestId);
        audit.complete(outputReference);
        return audit;
    }

    @Transactional
    public EaifExecutionAudit fail(
            UUID tenantId,
            UUID requestId
    ) {
        EaifExecutionAudit audit = get(tenantId, requestId);
        audit.fail();
        return audit;
    }

    @Transactional(readOnly = true)
    public EaifExecutionAudit get(
            UUID tenantId,
            UUID requestId
    ) {
        return audits.findByTenantIdAndAiRequestId(
                tenantId,
                requestId
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "AI execution audit not found"
                )
        );
    }
}
