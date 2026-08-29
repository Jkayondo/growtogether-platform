package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gts_admission_payment_allocation")
public class AdmissionPaymentAllocation
        extends AuditedTenantEntity {

    @Column(
            name = "admission_payment_obligation_id",
            nullable = false
    )
    private UUID admissionPaymentObligationId;

    @Column(
            name = "eip_payment_transaction_id",
            nullable = false
    )
    private UUID eipPaymentTransactionId;

    @Column(
            name = "allocated_amount",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal allocatedAmount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "allocation_status",
            nullable = false,
            length = 30
    )
    private AdmissionPaymentAllocationStatus allocationStatus =
            AdmissionPaymentAllocationStatus.APPLIED;

    @Column(
            name = "applied_at",
            nullable = false
    )
    private Instant appliedAt;

    @Column(name = "applied_by")
    private UUID appliedBy;

    @Column(name = "reversed_at")
    private Instant reversedAt;

    @Column(name = "reversed_by")
    private UUID reversedBy;

    @Column(
            name = "reversal_reason",
            length = 1000
    )
    private String reversalReason;

    protected AdmissionPaymentAllocation() {
    }

    public AdmissionPaymentAllocation(
            UUID admissionPaymentObligationId,
            UUID eipPaymentTransactionId,
            BigDecimal allocatedAmount,
            UUID appliedBy
    ) {

        if (admissionPaymentObligationId == null) {
            throw new IllegalArgumentException(
                    "admissionPaymentObligationId must not be null"
            );
        }

        if (eipPaymentTransactionId == null) {
            throw new IllegalArgumentException(
                    "eipPaymentTransactionId must not be null"
            );
        }

        if (
                allocatedAmount == null
                || allocatedAmount.signum() <= 0
        ) {
            throw new IllegalArgumentException(
                    "allocatedAmount must be positive"
            );
        }

        this.admissionPaymentObligationId =
                admissionPaymentObligationId;

        this.eipPaymentTransactionId =
                eipPaymentTransactionId;

        this.allocatedAmount =
                allocatedAmount;

        this.allocationStatus =
                AdmissionPaymentAllocationStatus.APPLIED;

        this.appliedAt =
                Instant.now();

        this.appliedBy =
                appliedBy;
    }

    public void markReversed(
            UUID actorId,
            String reason
    ) {

        invalidate(
                AdmissionPaymentAllocationStatus.REVERSED,
                actorId,
                reason
        );
    }

    public void markRefunded(
            UUID actorId,
            String reason
    ) {

        invalidate(
                AdmissionPaymentAllocationStatus.REFUNDED,
                actorId,
                reason
        );
    }

    private void invalidate(
            AdmissionPaymentAllocationStatus newStatus,
            UUID actorId,
            String reason
    ) {

        if (
                allocationStatus
                        != AdmissionPaymentAllocationStatus.APPLIED
        ) {
            throw new IllegalStateException(
                    "Only APPLIED admission payment allocation can be invalidated"
            );
        }

        if (
                newStatus
                        != AdmissionPaymentAllocationStatus.REVERSED
                && newStatus
                        != AdmissionPaymentAllocationStatus.REFUNDED
        ) {
            throw new IllegalArgumentException(
                    "Invalid allocation lifecycle status"
            );
        }

        if (actorId == null) {
            throw new IllegalArgumentException(
                    "actorId must not be null"
            );
        }

        if (
                reason == null
                || reason.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "reason must not be blank"
            );
        }

        this.allocationStatus =
                newStatus;

        this.reversedAt =
                Instant.now();

        this.reversedBy =
                actorId;

        this.reversalReason =
                reason.trim();
    }

    public UUID getAdmissionPaymentObligationId() {
        return admissionPaymentObligationId;
    }

    public UUID getEipPaymentTransactionId() {
        return eipPaymentTransactionId;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public AdmissionPaymentAllocationStatus getAllocationStatus() {
        return allocationStatus;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public UUID getAppliedBy() {
        return appliedBy;
    }

    public Instant getReversedAt() {
        return reversedAt;
    }

    public UUID getReversedBy() {
        return reversedBy;
    }

    public String getReversalReason() {
        return reversalReason;
    }
}
