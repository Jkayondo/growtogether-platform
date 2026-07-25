package africa.growtogether.platform.eaif.evidence;

import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.audit.ExecutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EaifEvidenceDtos {

    private EaifEvidenceDtos() {
    }


    public record EvidenceRecord(
            UUID auditId,
            UUID aiRequestId,
            String sourceService,
            String modelCode,
            AiEnums.RiskLevel riskLevel,
            ExecutionStatus executionStatus,
            Instant createdAt
    ) {
    }


    public record EvidencePackage(
            UUID tenantId,
            Instant generatedAt,
            long totalRecords,
            Map<ExecutionStatus, Long> statusSummary,
            List<EvidenceRecord> records
    ) {
    }
}
