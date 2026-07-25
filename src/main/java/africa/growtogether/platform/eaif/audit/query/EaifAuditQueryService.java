package africa.growtogether.platform.eaif.audit.query;

import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.audit.EaifExecutionAudit;
import africa.growtogether.platform.eaif.audit.EaifExecutionAuditRepository;
import africa.growtogether.platform.eaif.audit.ExecutionStatus;
import africa.growtogether.platform.eaif.audit.reporting.AuditReportDtos;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EaifAuditQueryService {

    private final EaifExecutionAuditRepository repository;

    public EaifAuditQueryService(
            EaifExecutionAuditRepository repository
    ) {
        this.repository = repository;
    }


    public List<AuditReportDtos.AuditItem> findAll(UUID tenantId) {

        return repository
                .findAllByTenantIdOrderByCreatedAtDesc (tenantId)
                .stream()
                .map(this::map)
                .toList();
    }


    public List<AuditReportDtos.AuditItem> findByStatus(
            UUID tenantId,
            ExecutionStatus status
    ) {

        return repository
                .findByTenantIdAndExecutionStatusOrderByCreatedAtDesc(
                        tenantId,
                        status
                )
                .stream()
                .map(this::map)
                .toList();
    }


    public List<AuditReportDtos.AuditItem> findByRisk(
            UUID tenantId,
            AiEnums.RiskLevel riskLevel
    ) {

        return repository
                .findByTenantIdAndRiskLevelOrderByCreatedAtDesc(
                        tenantId,
                        riskLevel
                )
                .stream()
                .map(this::map)
                .toList();
    }


    public List<AuditReportDtos.AuditItem> findByModel(
            UUID tenantId,
            String modelCode
    ) {

        return repository
                .findByTenantIdAndModelCodeOrderByCreatedAtDesc(
                        tenantId,
                        modelCode
                )
                .stream()
                .map(this::map)
                .toList();
    }


    private AuditReportDtos.AuditItem map(
            EaifExecutionAudit audit
    ) {

return new AuditReportDtos.AuditItem(
        audit.getId(),
        audit.aiRequestId(),
        audit.sourceService(),
        audit.modelCode(),
        audit.promptCode(),
        audit.riskLevel(),
        audit.executionStatus(),
        audit.actorUserId(),
        audit.startedAt(),
        audit.completedAt(),
        audit.outputReference(),
        audit.getCreatedAt()
      );
    }
}
