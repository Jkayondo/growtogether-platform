package africa.growtogether.platform.eaif.approval;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface EaifApprovalRecordRepository
        extends JpaRepository<EaifApprovalRecord, UUID> {


    Optional<EaifApprovalRecord> findByTenantIdAndAiRequestId(
            UUID tenantId,
            UUID aiRequestId
    );


    boolean existsByTenantIdAndAiRequestIdAndApprovalStatus(
            UUID tenantId,
            UUID aiRequestId,
            ApprovalStatus approvalStatus
    );
}
