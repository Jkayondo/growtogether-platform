package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdmissionPaymentAllocationRepository
        extends JpaRepository<AdmissionPaymentAllocation, UUID> {

    Optional<AdmissionPaymentAllocation> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<AdmissionPaymentAllocation>
    findByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
            UUID tenantId,
            UUID admissionPaymentObligationId,
            UUID eipPaymentTransactionId
    );

    boolean
    existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
            UUID tenantId,
            UUID admissionPaymentObligationId,
            UUID eipPaymentTransactionId
    );

    List<AdmissionPaymentAllocation>
    findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
            UUID tenantId,
            UUID admissionPaymentObligationId,
            AdmissionPaymentAllocationStatus allocationStatus,
            EntityStatus status
    );

    List<AdmissionPaymentAllocation>
    findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
            UUID tenantId,
            UUID eipPaymentTransactionId,
            AdmissionPaymentAllocationStatus allocationStatus,
            EntityStatus status
    );
}
