package africa.growtogether.platform.eaif.audit.reporting;

import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.audit.ExecutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AuditReportDtos {

    private AuditReportDtos() {
    }

    public record AuditItem(
            UUID auditId,
            UUID aiRequestId,
            String sourceService,
            String modelCode,
            String promptCode,
            AiEnums.RiskLevel riskLevel,
            ExecutionStatus executionStatus,
            UUID actorUserId,
            Instant startedAt,
            Instant completedAt,
            String outputReference,
            Instant createdAt
    ) {
    }


    public record AuditSummary(
            long totalExecutions,
            long received,
            long approved,
            long processing,
            long completed,
            long failed,
            long rejected,
            long lowRisk,
            long mediumRisk,
            long highRisk,
            long criticalRisk,
            Instant generatedAt
    ) {
    }


    public record ModelUsage(
            String modelCode,
            long executionCount,
            long successfulExecutions,
            long failedExecutions
    ) {
    }


    public record SourceServiceUsage(
            String sourceService,
            long executionCount
    ) {
    }


    public record AuditReport(
            AuditSummary summary,
            List<AuditItem> executions,
            List<ModelUsage> modelUsage,
            List<SourceServiceUsage> sourceUsage
    ) {
    }
}
