package africa.growtogether.platform.eaif.audit;

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

    long countByTenantIdAndExecutionStatus(
            UUID tenantId,
            ExecutionStatus executionStatus
    );
}
