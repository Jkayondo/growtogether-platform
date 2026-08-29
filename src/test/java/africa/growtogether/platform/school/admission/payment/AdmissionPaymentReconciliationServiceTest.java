package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.eip.payment.PaymentTransactionReadGateway;
import africa.growtogether.platform.eip.payment.PaymentTransactionSnapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionPaymentReconciliationServiceTest {

    @Mock
    private AdmissionPaymentObligationRepository obligations;

    @Mock
    private AdmissionPaymentAllocationRepository allocations;

    @Mock
    private AdmissionPaymentGateHistoryRepository history;

    @Mock
    private PaymentTransactionReadGateway payments;

    @Mock
    private AdmissionPaymentObligation obligation;

    private AdmissionPaymentReconciliationService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionPaymentReconciliationService(
                        obligations,
                        allocations,
                        history,
                        payments
                );
    }

    @Test
    void fullReversalInvalidatesAllocationAndReopensPaymentRequirement() {

        UUID tenantId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        String correlationId = "test-admission-reconciliation-" + UUID.randomUUID();

        AdmissionPaymentAllocation allocation =
                allocation(
                        obligationId,
                        paymentId,
                        "50000.00",
                        actorId
                );

        preparePayment(
                tenantId,
                paymentId,
                "REVERSED"
        );

        prepareLockedObligation(
                tenantId,
                obligationId,
                "50000.00",
                "0.00",
                AdmissionPaymentGateStatus.SATISFIED
        );

        /*
         * First two reads discover/re-confirm the active payment
         * allocation. The final read occurs after it has been marked
         * REVERSED and therefore returns no APPLIED allocations.
         */
        when(
                allocations
                        .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                                tenantId,
                                paymentId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(allocation),
                List.of(allocation)
        );

        when(
                allocations
                        .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                tenantId,
                                obligationId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of()
        );

        AdmissionPaymentReconciliationResult result =
                service.reconcile(
                        tenantId,
                        paymentId,
                        actorId,
                        correlationId
                );

        assertEquals(
                AdmissionPaymentAllocationStatus.REVERSED,
                allocation.getAllocationStatus()
        );

        assertNotNull(
                allocation.getReversedAt()
        );

        assertEquals(
                actorId,
                allocation.getReversedBy()
        );

        verify(
                obligation
        ).updateFromPaymentEvaluation(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                actorId
        );

        assertEquals(
                1,
                result.affectedAllocationCount()
        );

        assertFalse(
                result.manualReviewRequired()
        );
    }

    @Test
    void reversalWithAnotherValidPaymentLeavesPartiallySatisfied() {

        UUID tenantId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AdmissionPaymentAllocation reversed =
                allocation(
                        obligationId,
                        paymentId,
                        "20000.00",
                        actorId
                );

        AdmissionPaymentAllocation remaining =
                allocation(
                        obligationId,
                        UUID.randomUUID(),
                        "30000.00",
                        actorId
                );

        preparePayment(
                tenantId,
                paymentId,
                "REVERSED"
        );

        prepareLockedObligation(
                tenantId,
                obligationId,
                "50000.00",
                "0.00",
                AdmissionPaymentGateStatus.SATISFIED
        );

        when(
                allocations
                        .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                                tenantId,
                                paymentId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(reversed),
                List.of(reversed)
        );

        when(
                allocations
                        .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                tenantId,
                                obligationId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(remaining)
        );

        service.reconcile(
                tenantId,
                paymentId,
                actorId,
                "test-admission-reconciliation-" + UUID.randomUUID()
        );

        verify(
                obligation
        ).updateFromPaymentEvaluation(
                AdmissionPaymentGateStatus.PARTIALLY_SATISFIED,
                actorId
        );
    }

    @Test
    void fullRefundInvalidatesAllocationAsRefunded() {

        UUID tenantId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AdmissionPaymentAllocation allocation =
                allocation(
                        obligationId,
                        paymentId,
                        "50000.00",
                        actorId
                );

        preparePayment(
                tenantId,
                paymentId,
                "REFUNDED"
        );

        prepareLockedObligation(
                tenantId,
                obligationId,
                "50000.00",
                "0.00",
                AdmissionPaymentGateStatus.SATISFIED
        );

        when(
                allocations
                        .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                                tenantId,
                                paymentId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(allocation),
                List.of(allocation)
        );

        when(
                allocations
                        .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                tenantId,
                                obligationId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of()
        );

        AdmissionPaymentReconciliationResult result =
                service.reconcile(
                        tenantId,
                        paymentId,
                        actorId,
                        "test-admission-reconciliation-" + UUID.randomUUID()
                );

        assertEquals(
                AdmissionPaymentAllocationStatus.REFUNDED,
                allocation.getAllocationStatus()
        );

        verify(
                obligation
        ).updateFromPaymentEvaluation(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                actorId
        );

        assertEquals(
                "REFUNDED",
                result.paymentStatus()
        );
    }

    @Test
    void partialRefundWithUnknownAmountRequiresHumanReview() {

        UUID tenantId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        String correlationId = "test-admission-reconciliation-" + UUID.randomUUID();

        AdmissionPaymentAllocation allocation =
                allocation(
                        obligationId,
                        paymentId,
                        "50000.00",
                        actorId
                );

        preparePayment(
                tenantId,
                paymentId,
                "PARTIALLY_REFUNDED"
        );

        prepareLockedObligation(
                tenantId,
                obligationId,
                "50000.00",
                "0.00",
                AdmissionPaymentGateStatus.SATISFIED
        );

        when(
                allocations
                        .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                                tenantId,
                                paymentId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(allocation),
                List.of(allocation)
        );

        AdmissionPaymentReconciliationResult result =
                service.reconcile(
                        tenantId,
                        paymentId,
                        actorId,
                        correlationId
                );

        /*
         * We deliberately preserve the original allocation because the
         * authoritative refunded amount is not currently available.
         */
        assertEquals(
                AdmissionPaymentAllocationStatus.APPLIED,
                allocation.getAllocationStatus()
        );

        verify(
                obligation
        ).markReviewRequired();

        assertTrue(
                result.manualReviewRequired()
        );

        assertEquals(
                1,
                result.affectedAllocationCount()
        );

        ArgumentCaptor<AdmissionPaymentGateHistory> captor =
                ArgumentCaptor.forClass(
                        AdmissionPaymentGateHistory.class
                );

        verify(
                history
        ).save(
                captor.capture()
        );

        assertEquals(
                AdmissionPaymentGateStatus.SATISFIED,
                captor.getValue().getPreviousGateStatus()
        );

        assertEquals(
                AdmissionPaymentGateStatus.REVIEW_REQUIRED,
                captor.getValue().getNewGateStatus()
        );

        assertEquals(
                actorId,
                captor.getValue().getChangedBy()
        );

        assertEquals(
                correlationId,
                captor.getValue().getCorrelationId()
        );
    }

    @Test
    void waiverCanStillFullyCoverObligationAfterPaymentReversal() {

        UUID tenantId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AdmissionPaymentAllocation allocation =
                allocation(
                        obligationId,
                        paymentId,
                        "20000.00",
                        actorId
                );

        preparePayment(
                tenantId,
                paymentId,
                "REVERSED"
        );

        prepareLockedObligation(
                tenantId,
                obligationId,
                "50000.00",
                "50000.00",
                AdmissionPaymentGateStatus.SATISFIED
        );

        when(
                allocations
                        .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                                tenantId,
                                paymentId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(allocation),
                List.of(allocation)
        );

        when(
                allocations
                        .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                tenantId,
                                obligationId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of()
        );

        service.reconcile(
                tenantId,
                paymentId,
                actorId,
                "test-admission-reconciliation-" + UUID.randomUUID()
        );

        verify(
                obligation
        ).markFullyWaived(
                actorId
        );

        verify(
                obligation,
                never()
        ).updateFromPaymentEvaluation(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                actorId
        );
    }

    @Test
    void paymentWithoutActiveAllocationsIsIdempotentNoOp() {

        UUID tenantId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        preparePayment(
                tenantId,
                paymentId,
                "REVERSED"
        );

        when(
                allocations
                        .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                                tenantId,
                                paymentId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(),
                List.of()
        );

        AdmissionPaymentReconciliationResult result =
                service.reconcile(
                        tenantId,
                        paymentId,
                        UUID.randomUUID(),
                        "test-admission-reconciliation-" + UUID.randomUUID()
                );

        assertEquals(
                0,
                result.affectedAllocationCount()
        );

        assertTrue(
                result.affectedObligationIds()
                        .isEmpty()
        );

        verifyNoInteractions(
                obligations,
                history
        );
    }

    @Test
    void succeededPaymentCannotEnterReconciliationWorkflow() {

        UUID tenantId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        when(
                payments.require(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                snapshot(
                        paymentId,
                        "SUCCEEDED"
                )
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.reconcile(
                                        tenantId,
                                        paymentId,
                                        UUID.randomUUID(),
                                        "test-admission-reconciliation-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "Payment transaction does not require admission reconciliation",
                exception.getMessage()
        );

        verifyNoInteractions(
                obligations,
                history
        );

        verify(
                payments,
                never()
        ).requireForUpdate(
                any(),
                any()
        );
    }

    private void preparePayment(
            UUID tenantId,
            UUID paymentId,
            String status
    ) {

        PaymentTransactionSnapshot snapshot =
                snapshot(
                        paymentId,
                        status
                );

        when(
                payments.require(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                snapshot
        );

        when(
                payments.requireForUpdate(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                snapshot
        );
    }

    private void prepareLockedObligation(
            UUID tenantId,
            UUID obligationId,
            String requiredAmount,
            String waivedAmount,
            AdmissionPaymentGateStatus gateStatus
    ) {

        when(
                obligations.findForUpdate(
                        tenantId,
                        obligationId
                )
        ).thenReturn(
                Optional.of(
                        obligation
                )
        );

        when(
                obligation.getId()
        ).thenReturn(
                obligationId
        );

        lenient().when(
                obligation.getRequiredAmount()
        ).thenReturn(
                new BigDecimal(
                        requiredAmount
                )
        );

        lenient().when(
                obligation.getWaivedAmount()
        ).thenReturn(
                new BigDecimal(
                        waivedAmount
                )
        );

        when(
                obligation.getGateStatus()
        ).thenReturn(
                gateStatus
        );

        lenient().when(
                obligations.save(
                        obligation
                )
        ).thenReturn(
                obligation
        );

        lenient().when(
                history.save(
                        any(AdmissionPaymentGateHistory.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );
    }

    private AdmissionPaymentAllocation allocation(
            UUID obligationId,
            UUID paymentId,
            String amount,
            UUID actorId
    ) {

        AdmissionPaymentAllocation allocation =
                new AdmissionPaymentAllocation(
                        obligationId,
                        paymentId,
                        new BigDecimal(
                                amount
                        ),
                        actorId
                );

        return allocation;
    }

    private PaymentTransactionSnapshot snapshot(
            UUID paymentId,
            String status
    ) {

        return new PaymentTransactionSnapshot(
                paymentId,
                "PPIS-2026-ADM-000001",
                new BigDecimal(
                        "50000.00"
                ),
                "UGX",
                status,
                "PROVIDER-TEST",
                Instant.now()
        );
    }
}
