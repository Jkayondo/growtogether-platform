package africa.growtogether.platform.eaif.audit;

import africa.growtogether.platform.eaif.AiEnums;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EaifExecutionAuditRepository
        extends JpaRepository<EaifExecutionAudit, UUID> {

    Optional<EaifExecutionAudit>
    findByTenantIdAndAiRequestId(
            UUID tenantId,
            UUID aiRequestId
    );

    List<EaifExecutionAudit>
    findAllByTenantIdOrderByCreatedAtDesc(
            UUID tenantId
    );

    List<EaifExecutionAudit>
    findByTenantIdAndExecutionStatusOrderByCreatedAtDesc(
            UUID tenantId,
            ExecutionStatus executionStatus
    );

    List<EaifExecutionAudit>
    findByTenantIdAndRiskLevelOrderByCreatedAtDesc(
            UUID tenantId,
            AiEnums.RiskLevel riskLevel
    );

    List<EaifExecutionAudit>
    findByTenantIdAndModelCodeOrderByCreatedAtDesc(
            UUID tenantId,
            String modelCode
    );

    long countByTenantIdAndExecutionStatus(
            UUID tenantId,
            ExecutionStatus executionStatus
    );
}
