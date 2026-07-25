package africa.growtogether.platform.eaif.evidence;

import africa.growtogether.platform.eaif.audit.EaifExecutionAudit;
import africa.growtogether.platform.eaif.audit.EaifExecutionAuditRepository;
import africa.growtogether.platform.eaif.audit.ExecutionStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EaifEvidenceGenerationService {

    private final EaifExecutionAuditRepository repository;


    public EaifEvidenceGenerationService(
            EaifExecutionAuditRepository repository
    ) {
        this.repository = repository;
    }


    public EaifEvidenceDtos.EvidencePackage generate(
            UUID tenantId
    ) {

        List<EaifExecutionAudit> audits =
                repository.findAllByTenantIdOrderByCreatedAtDesc(
                        tenantId
                );


        List<EaifEvidenceDtos.EvidenceRecord> records =
                audits.stream()
                        .map(this::toEvidenceRecord)
                        .collect(Collectors.toList());


        Map<ExecutionStatus, Long> statusSummary =
                new EnumMap<>(ExecutionStatus.class);


        for (ExecutionStatus status : ExecutionStatus.values()) {

            statusSummary.put(
                    status,
                    audits.stream()
                            .filter(audit ->
                                    audit.executionStatus() == status
                            )
                            .count()
            );
        }


        return new EaifEvidenceDtos.EvidencePackage(
                tenantId,
                Instant.now(),
                records.size(),
                statusSummary,
                records
        );
    }


    private EaifEvidenceDtos.EvidenceRecord toEvidenceRecord(
            EaifExecutionAudit audit
    ) {

        return new EaifEvidenceDtos.EvidenceRecord(
                audit.getId(),
                audit.aiRequestId(),
                audit.sourceService(),
                audit.modelCode(),
                audit.riskLevel(),
                audit.executionStatus(),
                audit.getCreatedAt()  
        );
    }
}
