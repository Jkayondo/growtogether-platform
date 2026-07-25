package africa.growtogether.platform.eaif.governance;

import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.audit.EaifExecutionAudit;
import africa.growtogether.platform.eaif.audit.EaifExecutionAuditRepository;
import africa.growtogether.platform.eaif.audit.ExecutionStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EaifGovernanceReportingService {

    private final EaifExecutionAuditRepository repository;


    public EaifGovernanceReportingService(
            EaifExecutionAuditRepository repository
    ) {
        this.repository = repository;
    }


    public EaifGovernanceDtos.Summary summary(UUID tenantId) {

        List<EaifExecutionAudit> audits =
                repository.findAllByTenantIdOrderByCreatedAtDesc(tenantId);


        long completed =
                countByStatus(audits, ExecutionStatus.COMPLETED);

        long failed =
                countByStatus(audits, ExecutionStatus.FAILED);


        long rejected =
                countByStatus(audits, ExecutionStatus.REJECTED);


        Map<ExecutionStatus, Long> statusDistribution =
                new EnumMap<>(ExecutionStatus.class);


        for (ExecutionStatus status : ExecutionStatus.values()) {
            statusDistribution.put(
                    status,
                    countByStatus(audits, status)
            );
        }


        Map<AiEnums.RiskLevel, Long> riskDistribution =
                new EnumMap<>(AiEnums.RiskLevel.class);


        for (AiEnums.RiskLevel risk : AiEnums.RiskLevel.values()) {

            long count = audits.stream()
                    .filter(audit -> audit.riskLevel() == risk)
                    .count();

            riskDistribution.put(risk, count);
        }


        return new EaifGovernanceDtos.Summary(
                audits.size(),
                completed,
                failed,
                rejected,
                statusDistribution,
                riskDistribution
        );
    }


    private long countByStatus(
            List<EaifExecutionAudit> audits,
            ExecutionStatus status
    ) {

        return audits.stream()
                .filter(audit ->
                        audit.executionStatus() == status
                )
                .count();
    }
}
