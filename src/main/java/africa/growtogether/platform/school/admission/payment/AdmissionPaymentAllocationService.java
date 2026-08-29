package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.eip.payment.PaymentTransactionReadGateway;
import africa.growtogether.platform.eip.payment.PaymentTransactionSnapshot;

import africa.growtogether.platform.school.admission.AdmissionApplication;
import africa.growtogether.platform.school.admission.AdmissionApplicationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AdmissionPaymentAllocationService {

    private final AdmissionPaymentObligationRepository obligations;
    private final AdmissionPaymentAllocationRepository allocations;
    private final AdmissionPaymentGateHistoryRepository history;
    private final AdmissionApplicationRepository applications;
    private final PaymentTransactionReadGateway payments;

    public AdmissionPaymentAllocationService(
            AdmissionPaymentObligationRepository obligations,
            AdmissionPaymentAllocationRepository allocations,
            AdmissionPaymentGateHistoryRepository history,
            AdmissionApplicationRepository applications,
            PaymentTransactionReadGateway payments
    ) {
        this.obligations = obligations;
        this.allocations = allocations;
        this.history = history;
        this.applications = applications;
        this.payments = payments;
    }

    @Transactional
    public AdmissionPaymentAllocationResult applySucceededPayment(
            UUID tenantId,
            UUID obligationId,
            UUID paymentTransactionId,
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
                paymentTransactionId,
                "paymentTransactionId"
        );

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

        AdmissionApplication application =
                applications
                        .findByTenantIdAndId(
                                tenantId,
                                obligation.getAdmissionApplicationId()
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Admission application for obligation not found"
                                )
                        );

        if (
                obligation.getGateStatus()
                        == AdmissionPaymentGateStatus.WAIVED
                || obligation.getGateStatus()
                        == AdmissionPaymentGateStatus.CANCELLED
        ) {
            throw new IllegalStateException(
                    "Admission payment obligation cannot receive payment in status "
                            + obligation.getGateStatus()
            );
        }

        if (
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentTransactionId
                        )
        ) {
            throw new IllegalStateException(
                    "Payment transaction is already linked to this admission obligation"
            );
        }

        PaymentTransactionSnapshot payment =
                payments.requireForUpdate(
                        tenantId,
                        paymentTransactionId
                );

        if (!payment.isSucceeded()) {
            throw new IllegalStateException(
                    "Only SUCCEEDED payment transactions can be allocated"
            );
        }

        if (
                !obligation
                        .getCurrencyCode()
                        .equalsIgnoreCase(
                                payment.currency()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Payment currency does not match admission obligation"
            );
        }

        /*
         * Admission payment transactions use the authoritative
         * admission application number as their merchant reference.
         * This prevents a payment for Application A being attached
         * to Application B.
         */
        if (
                payment.merchantReference() == null
                || !application
                        .getApplicationNumber()
                        .equalsIgnoreCase(
                                payment.merchantReference().trim()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Payment merchant reference does not match admission application"
            );
        }

        BigDecimal obligationAlreadyAllocated =
                sum(
                        allocations
                                .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                        tenantId,
                                        obligationId,
                                        AdmissionPaymentAllocationStatus.APPLIED,
                                        EntityStatus.ACTIVE
                                )
                );

        BigDecimal paymentAlreadyAllocated =
                sum(
                        allocations
                                .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                                        tenantId,
                                        paymentTransactionId,
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

        BigDecimal outstandingBefore =
                amountDueAfterWaiver
                        .subtract(
                                obligationAlreadyAllocated
                        )
                        .max(
                                BigDecimal.ZERO
                        );

        if (
                outstandingBefore.signum()
                        == 0
        ) {
            /*
             * IMPROVEMENT:
             * This is a rejected allocation attempt, so it must not
             * perform a corrective state mutation that would be rolled
             * back by the exception below.
             *
             * Historical gate-state inconsistencies belong to the
             * reconciliation workflow.
             */
            throw new IllegalStateException(
                    "Admission payment obligation is already satisfied"
            );
        }

        BigDecimal paymentAvailable =
                payment
                        .amount()
                        .subtract(
                                paymentAlreadyAllocated
                        )
                        .max(
                                BigDecimal.ZERO
                        );

        if (
                paymentAvailable.signum()
                        == 0
        ) {
            throw new IllegalStateException(
                    "Payment transaction has no unallocated amount remaining"
            );
        }

        BigDecimal allocatedAmount =
                outstandingBefore.min(
                        paymentAvailable
                );

        AdmissionPaymentAllocation allocation =
                new AdmissionPaymentAllocation(
                        obligationId,
                        paymentTransactionId,
                        allocatedAmount,
                        actorId
                );

        allocation.setTenantId(
                tenantId
        );

        AdmissionPaymentAllocation savedAllocation =
                allocations.save(
                        allocation
                );

        BigDecimal totalAllocated =
                obligationAlreadyAllocated.add(
                        allocatedAmount
                );

        BigDecimal outstandingAfter =
                amountDueAfterWaiver
                        .subtract(
                                totalAllocated
                        )
                        .max(
                                BigDecimal.ZERO
                        );

        AdmissionPaymentGateStatus nextStatus =
                outstandingAfter.signum() == 0
                        ? AdmissionPaymentGateStatus.SATISFIED
                        : AdmissionPaymentGateStatus.PARTIALLY_SATISFIED;

        ensureStatus(
                tenantId,
                obligation,
                nextStatus,
                actorId,
                correlationId,
                "Succeeded EIP payment allocated to admission obligation"
        );

        return new AdmissionPaymentAllocationResult(
                savedAllocation,
                obligation.getRequiredAmount(),
                obligation.getWaivedAmount(),
                totalAllocated,
                outstandingAfter,
                obligation.getGateStatus()
        );
    }

    private void ensureStatus(
            UUID tenantId,
            AdmissionPaymentObligation obligation,
            AdmissionPaymentGateStatus nextStatus,
            UUID actorId,
            String correlationId,
            String reason
    ) {

        AdmissionPaymentGateStatus previous =
                obligation.getGateStatus();

        if (previous == nextStatus) {
            return;
        }

        obligation.updateFromPaymentEvaluation(
                nextStatus,
                actorId
        );

        obligations.save(
                obligation
        );

        AdmissionPaymentGateHistory evidence =
                new AdmissionPaymentGateHistory(
                        obligation.getId(),
                        previous,
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
