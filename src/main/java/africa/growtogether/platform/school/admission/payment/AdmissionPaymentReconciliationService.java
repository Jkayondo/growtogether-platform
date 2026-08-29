package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.eip.payment.PaymentTransactionReadGateway;
import africa.growtogether.platform.eip.payment.PaymentTransactionSnapshot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AdmissionPaymentReconciliationService {

    private final AdmissionPaymentObligationRepository obligations;
    private final AdmissionPaymentAllocationRepository allocations;
    private final AdmissionPaymentGateHistoryRepository history;
    private final PaymentTransactionReadGateway payments;

    public AdmissionPaymentReconciliationService(
            AdmissionPaymentObligationRepository obligations,
            AdmissionPaymentAllocationRepository allocations,
            AdmissionPaymentGateHistoryRepository history,
            PaymentTransactionReadGateway payments
    ) {
        this.obligations = obligations;
        this.allocations = allocations;
        this.history = history;
        this.payments = payments;
    }

    @Transactional
    public AdmissionPaymentReconciliationResult reconcile(
            UUID tenantId,
            UUID paymentTransactionId,
            UUID actorId,
            String correlationId
    ) {

        requireId(
                tenantId,
                "tenantId"
        );

        requireId(
                paymentTransactionId,
                "paymentTransactionId"
        );

        requireId(
                actorId,
                "actorId"
        );

        /*
         * First read determines which obligations currently reference
         * this payment. We deliberately lock obligations before the
         * payment row so the lock order remains compatible with the
         * normal allocation service:
         *
         * obligation -> payment
         */
        PaymentTransactionSnapshot initialPayment =
                payments.require(
                        tenantId,
                        paymentTransactionId
                );

        requireReconciliationStatus(
                initialPayment.status()
        );

        List<AdmissionPaymentAllocation> initialAllocations =
                activeAppliedAllocationsForPayment(
                        tenantId,
                        paymentTransactionId
                );

        List<UUID> obligationIds =
                initialAllocations
                        .stream()
                        .map(
                                AdmissionPaymentAllocation
                                        ::getAdmissionPaymentObligationId
                        )
                        .distinct()
                        .sorted(
                                Comparator.comparing(
                                        UUID::toString
                                )
                        )
                        .toList();

        Map<UUID, AdmissionPaymentObligation> lockedObligations =
                new LinkedHashMap<>();

        for (UUID obligationId : obligationIds) {

            AdmissionPaymentObligation obligation =
                    obligations
                            .findForUpdate(
                                    tenantId,
                                    obligationId
                            )
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "Admission payment obligation for allocation not found"
                                    )
                            );

            lockedObligations.put(
                    obligationId,
                    obligation
            );
        }

        PaymentTransactionSnapshot payment =
                payments.requireForUpdate(
                        tenantId,
                        paymentTransactionId
                );

        requireReconciliationStatus(
                payment.status()
        );

        /*
         * Re-read after all locks are acquired. This ensures decisions
         * are based on the authoritative allocation set visible inside
         * the locked transaction.
         */
        List<AdmissionPaymentAllocation> activeAllocations =
                activeAppliedAllocationsForPayment(
                        tenantId,
                        paymentTransactionId
                );

        for (
                AdmissionPaymentAllocation allocation
                : activeAllocations
        ) {

            if (
                    !lockedObligations.containsKey(
                            allocation.getAdmissionPaymentObligationId()
                    )
            ) {
                throw new IllegalStateException(
                        "Admission payment allocation set changed during reconciliation"
                );
            }
        }

        if (
                "PARTIALLY_REFUNDED".equals(
                        payment.status()
                )
        ) {

            Set<UUID> affected =
                    new LinkedHashSet<>();

            for (
                    AdmissionPaymentAllocation allocation
                    : activeAllocations
            ) {

                UUID obligationId =
                        allocation
                                .getAdmissionPaymentObligationId();

                AdmissionPaymentObligation obligation =
                        lockedObligations.get(
                                obligationId
                        );

                markReviewRequired(
                        tenantId,
                        obligation,
                        actorId,
                        correlationId,
                        "EIP payment is PARTIALLY_REFUNDED but authoritative refund amount is unavailable"
                );

                affected.add(
                        obligationId
                );
            }

            return new AdmissionPaymentReconciliationResult(
                    paymentTransactionId,
                    payment.status(),
                    activeAllocations.size(),
                    new ArrayList<>(
                            affected
                    ),
                    true
            );
        }

        AdmissionPaymentAllocationStatus invalidStatus =
                "REVERSED".equals(
                        payment.status()
                )
                        ? AdmissionPaymentAllocationStatus.REVERSED
                        : AdmissionPaymentAllocationStatus.REFUNDED;

        Set<UUID> affected =
                new LinkedHashSet<>();

        for (
                AdmissionPaymentAllocation allocation
                : activeAllocations
        ) {

            if (
                    invalidStatus
                            == AdmissionPaymentAllocationStatus.REVERSED
            ) {

                allocation.markReversed(
                        actorId,
                        "Underlying EIP payment transaction was reversed"
                );

            } else {

                allocation.markRefunded(
                        actorId,
                        "Underlying EIP payment transaction was fully refunded"
                );
            }

            allocations.save(
                    allocation
            );

            affected.add(
                    allocation.getAdmissionPaymentObligationId()
            );
        }

        /*
         * The invalidated allocations are no longer APPLIED.
         * Recalculate each affected obligation from the remaining valid
         * allocations plus any approved waiver.
         */
        for (UUID obligationId : affected) {

            AdmissionPaymentObligation obligation =
                    lockedObligations.get(
                            obligationId
                    );

            recalculateObligation(
                    tenantId,
                    obligation,
                    actorId,
                    correlationId,
                    payment.status()
            );
        }

        return new AdmissionPaymentReconciliationResult(
                paymentTransactionId,
                payment.status(),
                activeAllocations.size(),
                new ArrayList<>(
                        affected
                ),
                false
        );
    }

    private void recalculateObligation(
            UUID tenantId,
            AdmissionPaymentObligation obligation,
            UUID actorId,
            String correlationId,
            String paymentStatus
    ) {

        BigDecimal remainingAllocated =
                sum(
                        allocations
                                .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                        tenantId,
                                        obligation.getId(),
                                        AdmissionPaymentAllocationStatus.APPLIED,
                                        EntityStatus.ACTIVE
                                )
                );

        BigDecimal amountDueAfterWaiver =
                obligation
                        .getRequiredAmount()
                        .subtract(
                                obligation.getWaivedAmount()
                        )
                        .max(
                                BigDecimal.ZERO
                        );

        BigDecimal outstanding =
                amountDueAfterWaiver
                        .subtract(
                                remainingAllocated
                        )
                        .max(
                                BigDecimal.ZERO
                        );

        AdmissionPaymentGateStatus nextStatus;

        if (
                outstanding.signum()
                        == 0
        ) {

            if (
                    obligation
                            .getWaivedAmount()
                            .compareTo(
                                    obligation.getRequiredAmount()
                            ) == 0
                    && remainingAllocated.signum()
                            == 0
            ) {

                nextStatus =
                        AdmissionPaymentGateStatus.WAIVED;

            } else {

                nextStatus =
                        AdmissionPaymentGateStatus.SATISFIED;
            }

        } else if (
                remainingAllocated.signum()
                        > 0
        ) {

            nextStatus =
                    AdmissionPaymentGateStatus.PARTIALLY_SATISFIED;

        } else {

            nextStatus =
                    AdmissionPaymentGateStatus.PAYMENT_REQUIRED;
        }

        AdmissionPaymentGateStatus previousStatus =
                obligation.getGateStatus();

        if (
                previousStatus
                        == nextStatus
        ) {
            return;
        }

        if (
                nextStatus
                        == AdmissionPaymentGateStatus.WAIVED
        ) {

            obligation.markFullyWaived(
                    actorId
            );

        } else {

            obligation.updateFromPaymentEvaluation(
                    nextStatus,
                    actorId
            );
        }

        obligations.save(
                obligation
        );

        saveHistory(
                tenantId,
                obligation.getId(),
                previousStatus,
                nextStatus,
                "Admission obligation recalculated after EIP payment became "
                        + paymentStatus,
                actorId,
                correlationId
        );
    }

    private void markReviewRequired(
            UUID tenantId,
            AdmissionPaymentObligation obligation,
            UUID actorId,
            String correlationId,
            String reason
    ) {

        AdmissionPaymentGateStatus previousStatus =
                obligation.getGateStatus();

        if (
                previousStatus
                        == AdmissionPaymentGateStatus.REVIEW_REQUIRED
        ) {
            return;
        }

        obligation.markReviewRequired();

        obligations.save(
                obligation
        );

        saveHistory(
                tenantId,
                obligation.getId(),
                previousStatus,
                AdmissionPaymentGateStatus.REVIEW_REQUIRED,
                reason,
                actorId,
                correlationId
        );
    }

    private void saveHistory(
            UUID tenantId,
            UUID obligationId,
            AdmissionPaymentGateStatus previousStatus,
            AdmissionPaymentGateStatus nextStatus,
            String reason,
            UUID actorId,
            String correlationId
    ) {

        AdmissionPaymentGateHistory evidence =
                new AdmissionPaymentGateHistory(
                        obligationId,
                        previousStatus,
                        nextStatus,
                        reason,
                        actorId,
                        correlationId
                );

        evidence.setTenantId(
                tenantId
        );

        history.save(
                evidence
        );
    }

    private List<AdmissionPaymentAllocation>
    activeAppliedAllocationsForPayment(
            UUID tenantId,
            UUID paymentTransactionId
    ) {

        return allocations
                .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                        tenantId,
                        paymentTransactionId,
                        AdmissionPaymentAllocationStatus.APPLIED,
                        EntityStatus.ACTIVE
                );
    }

    private BigDecimal sum(
            List<AdmissionPaymentAllocation> records
    ) {

        return records
                .stream()
                .map(
                        AdmissionPaymentAllocation::getAllocatedAmount
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private void requireReconciliationStatus(
            String status
    ) {

        if (
                !"REVERSED".equals(status)
                && !"REFUNDED".equals(status)
                && !"PARTIALLY_REFUNDED".equals(status)
        ) {
            throw new IllegalStateException(
                    "Payment transaction does not require admission reconciliation"
            );
        }
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
