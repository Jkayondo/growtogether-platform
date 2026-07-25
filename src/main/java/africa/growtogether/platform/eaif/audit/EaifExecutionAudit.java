package africa.growtogether.platform.eaif.audit;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.eaif.AiEnums;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "eaif_execution_audits",
    indexes = {
        @Index(
            name = "ix_eaif_audit_request",
            columnList = "tenant_id,ai_request_id"
        ),
        @Index(
            name = "ix_eaif_audit_status",
            columnList = "tenant_id,execution_status"
        ),
        @Index(
            name = "ix_eaif_audit_created",
            columnList = "tenant_id,created_at"
        )
    }
)
public class EaifExecutionAudit extends AuditedTenantEntity {

    @Column(name = "ai_request_id", nullable = false)
    private UUID aiRequestId;

    @Column(name = "source_service", nullable = false, length = 100)
    private String sourceService;

    @Column(name = "model_code", nullable = false, length = 100)
    private String modelCode;

    @Column(name = "prompt_code", length = 100)
    private String promptCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private AiEnums.RiskLevel riskLevel;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 30)
    private ExecutionStatus executionStatus;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "output_reference", length = 500)
    private String outputReference;

    protected EaifExecutionAudit() {}

    public EaifExecutionAudit(
            UUID tenantId,
            UUID aiRequestId,
            String sourceService,
            String modelCode,
            String promptCode,
            AiEnums.RiskLevel riskLevel,
            UUID actorUserId
    ) {
        setTenantId(tenantId);
        this.aiRequestId = aiRequestId;
        this.sourceService = sourceService;
        this.modelCode = modelCode;
        this.promptCode = promptCode;
        this.riskLevel = riskLevel;
        this.actorUserId = actorUserId;
        this.executionStatus = ExecutionStatus.RECEIVED;
    }

   public void approve() {
       if (executionStatus != ExecutionStatus.RECEIVED) {
           throw new IllegalStateException(
               "Audit cannot be approved from " + executionStatus
           );
       }

    executionStatus = ExecutionStatus.APPROVED;
   }

    public void startProcessing() {
        if (executionStatus != ExecutionStatus.APPROVED) {
            throw new IllegalStateException(
                "Audit cannot start processing from " + executionStatus
            );
        }

        executionStatus = ExecutionStatus.PROCESSING;
        startedAt = Instant.now();
    }

    public void complete(String outputReference) {
        if (executionStatus != ExecutionStatus.PROCESSING) {
            throw new IllegalStateException(
                "Audit is not processing"
            );
        }

        executionStatus = ExecutionStatus.COMPLETED;
        this.outputReference = outputReference;
        completedAt = Instant.now();
    }

    public void fail() {
        executionStatus = ExecutionStatus.FAILED;
        completedAt = Instant.now();
    }

    public UUID aiRequestId() {
        return aiRequestId;
    }

    public ExecutionStatus executionStatus() {
        return executionStatus;
    }

    public String modelCode() {
        return modelCode;
    }

    public String promptCode() {
        return promptCode;
    }
    
    public String sourceService() {
    return sourceService;
    }

    public AiEnums.RiskLevel riskLevel() {
    return riskLevel;
    }

    public UUID actorUserId() {
    return actorUserId;
    }

    public Instant startedAt() {
    return startedAt;
    }

    public Instant completedAt() {
    return completedAt;
    }

    public String outputReference() {
    return outputReference;
    }
}
