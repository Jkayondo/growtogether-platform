package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AdmissionPaymentWaiverService {

    private final AdmissionPaymentObligationRepository obligations;
    private final AdmissionPaymentAllocationRepository allocations;
    private final AdmissionPaymentGateHistoryRepository history;

    public AdmissionPaymentWaiverService(
            AdmissionPaymentObligationRepository obligations,
            AdmissionPaymentAllocationRepository allocations,
            AdmissionPaymentGateHistoryRepository history
    ) {
        this.obligations = obligations;
        this.allocations = allocations;
        this.history = history;
    }

    @Transactional
    public AdmissionPaymentWaiverResult waive(
            UUID tenantId,
            UUID obligationId,
            BigDecimal waiverAmount,
            String reason,
            UUID actorId,
            String correlationId
    ) {

        requireId(
                tenantId,
                "tenantId"
        );

        requireId(
                obligationId,
                "obligationId"
        );

        requireId(
                actorId,
                "actorId"
        );

        if (
                waiverAmount == null
                || waiverAmount.signum() <= 0
        ) {
            throw new IllegalArgumentException(
                    "waiverAmount must be positive"
            );
        }

        if (
                reason == null
                || reason.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "waiver reason must not be blank"
            );
        }

        AdmissionPaymentObligation obligation =
                obligations
                        .findForUpdate(
                                tenantId,
                                obligationId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Admission payment obligation not found for tenant"
                                )
                        );

        if (
                obligation.getGateStatus()
                        == AdmissionPaymentGateStatus.CANCELLED
        ) {
            throw new IllegalStateException(
                    "Cancelled admission payment obligation cannot be waived"
            );
        }

        if (
                obligation.getWaivedAmount()
                        .signum() > 0
        ) {
            throw new IllegalStateException(
                    "Admission payment obligation already has a waiver"
            );
        }

        List<AdmissionPaymentAllocation> activeAllocations =
                allocations
                        .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                tenantId,
                                obligationId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        );

        BigDecimal allocatedAmount =
                activeAllocations
                        .stream()
                        .map(
                                AdmissionPaymentAllocation::getAllocatedAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal outstandingBeforeWaiver =
                obligation
                        .getRequiredAmount()
                        .subtract(
                                allocatedAmount
                        )
                        .max(
                                BigDecimal.ZERO
                        );

        if (
                outstandingBeforeWaiver.signum()
                        == 0
        ) {
            throw new IllegalStateException(
                    "Admission payment obligation has no outstanding amount to waive"
            );
        }

        if (
                waiverAmount.compareTo(
                        outstandingBeforeWaiver
                ) > 0
        ) {
            throw new IllegalArgumentException(
                    "waiverAmount cannot exceed outstanding admission balance"
            );
        }

        AdmissionPaymentGateStatus previousStatus =
                obligation.getGateStatus();

        obligation.applyWaiver(
                waiverAmount,
                reason,
                actorId
        );

        BigDecimal outstandingAfterWaiver =
                outstandingBeforeWaiver
                        .subtract(
                                waiverAmount
                        )
                        .max(
                                BigDecimal.ZERO
                        );

        AdmissionPaymentGateStatus nextStatus;

        if (
                outstandingAfterWaiver.signum()
                        == 0
        ) {

            if (
                    allocatedAmount.signum()
                            == 0
                    && waiverAmount.compareTo(
                            obligation.getRequiredAmount()
                    ) == 0
            ) {

                obligation.markFullyWaived(
                        actorId
                );

                nextStatus =
                        AdmissionPaymentGateStatus.WAIVED;

            } else {

                obligation.updateFromPaymentEvaluation(
                        AdmissionPaymentGateStatus.SATISFIED,
                        actorId
                );

                nextStatus =
                        AdmissionPaymentGateStatus.SATISFIED;
            }

        } else {

            AdmissionPaymentGateStatus partialStatus =
                    allocatedAmount.signum() > 0
                            ? AdmissionPaymentGateStatus.PARTIALLY_SATISFIED
                            : AdmissionPaymentGateStatus.PAYMENT_REQUIRED;

            obligation.updateFromPaymentEvaluation(
                    partialStatus,
                    actorId
            );

            nextStatus =
                    partialStatus;
        }

        obligations.save(
                obligation
        );

        /*
         * IMPROVEMENT:
         * Every approved waiver is consequential financial evidence,
         * even when it does not change the gate status.
         *
         * Example:
         * PAYMENT_REQUIRED -> PAYMENT_REQUIRED after a partial waiver.
         */
        AdmissionPaymentGateHistory evidence =
                new AdmissionPaymentGateHistory(
                        obligationId,
                        previousStatus,
                        nextStatus,
                        "Admission payment waiver approved: "
                                + reason.trim(),
                        actorId,
                        correlationId
                );

        evidence.setTenantId(
                tenantId
        );

        history.save(
                evidence
        );

        return new AdmissionPaymentWaiverResult(
                obligation,
                obligation.getRequiredAmount(),
                allocatedAmount,
                waiverAmount,
                outstandingAfterWaiver,
                obligation.getGateStatus()
        );
    }

    private void requireId(
            UUID value,
            String field
    ) {

        if (value == null) {
            throw new IllegalArgumentException(
                    field + " must not be null"
            );
        }
    }
}
