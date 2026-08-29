package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.eip.payment.PaymentTransactionReadGateway;
import africa.growtogether.platform.eip.payment.PaymentTransactionSnapshot;

import africa.growtogether.platform.school.admission.AdmissionApplication;
import africa.growtogether.platform.school.admission.AdmissionApplicationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionPaymentAllocationServiceTest {

    @Mock
    private AdmissionPaymentObligationRepository obligations;

    @Mock
    private AdmissionPaymentAllocationRepository allocations;

    @Mock
    private AdmissionPaymentGateHistoryRepository history;

    @Mock
    private AdmissionApplicationRepository applications;

    @Mock
    private PaymentTransactionReadGateway payments;

    @Mock
    private AdmissionApplication application;

    private AdmissionPaymentAllocationService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionPaymentAllocationService(
                        obligations,
                        allocations,
                        history,
                        applications,
                        payments
                );
    }

    @Test
    void partialPaymentLeavesOutstandingBalance() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                prepareObligation(
                        tenantId,
                        obligationId,
                        applicationId,
                        "50000.00",
                        "0.00"
                );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000001"
        );

        when(
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentId
                        )
        ).thenReturn(false);

        when(
                payments.requireForUpdate(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                payment(
                        paymentId,
                        "PPIS-2026-ADM-000001",
                        "20000.00",
                        "UGX",
                        "SUCCEEDED"
                )
        );

        when(
                allocations
                        .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                tenantId,
                                obligationId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(List.of());

        when(
                allocations
                        .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                                tenantId,
                                paymentId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(List.of());

        when(
                allocations.save(
                        any(AdmissionPaymentAllocation.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(
                history.save(
                        any(AdmissionPaymentGateHistory.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionPaymentAllocationResult result =
                service.applySucceededPayment(
                        tenantId,
                        obligationId,
                        paymentId,
                        actorId,
                                        "test-admission-payment-" + UUID.randomUUID()
                );

        assertMoney(
                "20000.00",
                result.allocation().getAllocatedAmount()
        );

        assertMoney(
                "20000.00",
                result.totalAllocatedAmount()
        );

        assertMoney(
                "30000.00",
                result.outstandingAmount()
        );

        assertEquals(
                AdmissionPaymentGateStatus.PARTIALLY_SATISFIED,
                result.gateStatus()
        );

        verify(
                obligations
        ).save(
                obligation
        );

        verify(
                history
        ).save(
                any(AdmissionPaymentGateHistory.class)
        );
    }

    @Test
    void fullPaymentSatisfiesObligation() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                prepareObligation(
                        tenantId,
                        obligationId,
                        applicationId,
                        "50000.00",
                        "0.00"
                );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000002"
        );

        prepareSuccessfulAllocation(
                tenantId,
                obligationId,
                paymentId,
                "PPIS-2026-ADM-000002",
                "50000.00"
        );

        when(
                allocations.save(
                        any(AdmissionPaymentAllocation.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(
                history.save(
                        any(AdmissionPaymentGateHistory.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionPaymentAllocationResult result =
                service.applySucceededPayment(
                        tenantId,
                        obligationId,
                        paymentId,
                        actorId,
                                        "test-admission-payment-" + UUID.randomUUID()
                );

        assertMoney(
                "50000.00",
                result.totalAllocatedAmount()
        );

        assertMoney(
                "0.00",
                result.outstandingAmount()
        );

        assertEquals(
                AdmissionPaymentGateStatus.SATISFIED,
                result.gateStatus()
        );

        verify(
                obligation
        ).updateFromPaymentEvaluation(
                AdmissionPaymentGateStatus.SATISFIED,
                actorId
        );
    }

    @Test
    void oversizedPaymentAllocatesOnlyOutstandingAmount() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        prepareObligation(
                tenantId,
                obligationId,
                applicationId,
                "50000.00",
                "0.00"
        );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000003"
        );

        prepareSuccessfulAllocation(
                tenantId,
                obligationId,
                paymentId,
                "PPIS-2026-ADM-000003",
                "70000.00"
        );

        when(
                allocations.save(
                        any(AdmissionPaymentAllocation.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(
                history.save(
                        any(AdmissionPaymentGateHistory.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionPaymentAllocationResult result =
                service.applySucceededPayment(
                        tenantId,
                        obligationId,
                        paymentId,
                        UUID.randomUUID(),
                                        "test-admission-payment-" + UUID.randomUUID()
                );

        assertMoney(
                "50000.00",
                result.allocation().getAllocatedAmount()
        );

        assertMoney(
                "0.00",
                result.outstandingAmount()
        );
    }

    @Test
    void pendingPaymentCannotBeAllocated() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        prepareObligation(
                tenantId,
                obligationId,
                applicationId,
                "50000.00",
                "0.00"
        );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000004"
        );

        when(
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentId
                        )
        ).thenReturn(false);

        when(
                payments.requireForUpdate(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                payment(
                        paymentId,
                        "PPIS-2026-ADM-000004",
                        "50000.00",
                        "UGX",
                        "PENDING_PROVIDER"
                )
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.applySucceededPayment(
                                        tenantId,
                                        obligationId,
                                        paymentId,
                                        UUID.randomUUID(),
                                        "test-admission-payment-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "Only SUCCEEDED payment transactions can be allocated",
                exception.getMessage()
        );

        verify(
                allocations,
                never()
        ).save(
                any()
        );
    }

    @Test
    void currencyMismatchIsRejected() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        prepareObligation(
                tenantId,
                obligationId,
                applicationId,
                "50000.00",
                "0.00"
        );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000005"
        );

        when(
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentId
                        )
        ).thenReturn(false);

        when(
                payments.requireForUpdate(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                payment(
                        paymentId,
                        "PPIS-2026-ADM-000005",
                        "50000.00",
                        "USD",
                        "SUCCEEDED"
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.applySucceededPayment(
                                        tenantId,
                                        obligationId,
                                        paymentId,
                                        UUID.randomUUID(),
                                        "test-admission-payment-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "Payment currency does not match admission obligation",
                exception.getMessage()
        );

        verify(
                allocations,
                never()
        ).save(
                any()
        );
    }

    @Test
    void paymentForDifferentAdmissionApplicationIsRejected() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        prepareObligation(
                tenantId,
                obligationId,
                applicationId,
                "50000.00",
                "0.00"
        );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000006"
        );

        when(
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentId
                        )
        ).thenReturn(false);

        when(
                payments.requireForUpdate(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                payment(
                        paymentId,
                        "PPIS-2026-ADM-999999",
                        "50000.00",
                        "UGX",
                        "SUCCEEDED"
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.applySucceededPayment(
                                        tenantId,
                                        obligationId,
                                        paymentId,
                                        UUID.randomUUID(),
                                        "test-admission-payment-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "Payment merchant reference does not match admission application",
                exception.getMessage()
        );

        verify(
                allocations,
                never()
        ).save(
                any()
        );
    }

    @Test
    void duplicatePaymentLinkIsRejectedBeforeAllocation() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        prepareObligation(
                tenantId,
                obligationId,
                applicationId,
                "50000.00",
                "0.00"
        );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000007"
        );

        when(
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentId
                        )
        ).thenReturn(true);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.applySucceededPayment(
                                        tenantId,
                                        obligationId,
                                        paymentId,
                                        UUID.randomUUID(),
                                        "test-admission-payment-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "Payment transaction is already linked to this admission obligation",
                exception.getMessage()
        );

        verifyNoInteractions(
                payments
        );

        verify(
                allocations,
                never()
        ).save(
                any()
        );
    }

    @Test
    void existingAllocationsLimitRemainingPaymentAmount() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        prepareObligation(
                tenantId,
                obligationId,
                applicationId,
                "50000.00",
                "0.00"
        );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000008"
        );

        when(
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentId
                        )
        ).thenReturn(false);

        when(
                payments.requireForUpdate(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                payment(
                        paymentId,
                        "PPIS-2026-ADM-000008",
                        "50000.00",
                        "UGX",
                        "SUCCEEDED"
                )
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

        AdmissionPaymentAllocation priorAllocation =
                mock(
                        AdmissionPaymentAllocation.class
                );

        when(
                priorAllocation.getAllocatedAmount()
        ).thenReturn(
                new BigDecimal("40000.00")
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
                List.of(
                        priorAllocation
                )
        );

        when(
                allocations.save(
                        any(AdmissionPaymentAllocation.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(
                history.save(
                        any(AdmissionPaymentGateHistory.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionPaymentAllocationResult result =
                service.applySucceededPayment(
                        tenantId,
                        obligationId,
                        paymentId,
                        UUID.randomUUID(),
                                        "test-admission-payment-" + UUID.randomUUID()
                );

        /*
         * Payment was UGX 50,000 but UGX 40,000 is already allocated
         * elsewhere, so only UGX 10,000 remains available.
         */
        assertMoney(
                "10000.00",
                result.allocation().getAllocatedAmount()
        );

        assertMoney(
                "40000.00",
                result.outstandingAmount()
        );
    }

    @Test
    void alreadyFullyFundedObligationRejectsWithoutMutation() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        String correlationId =
                "test-admission-payment-" + UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                prepareObligation(
                        tenantId,
                        obligationId,
                        applicationId,
                        "50000.00",
                        "0.00"
                );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000010"
        );

        when(
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentId
                        )
        ).thenReturn(false);

        when(
                payments.requireForUpdate(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                payment(
                        paymentId,
                        "PPIS-2026-ADM-000010",
                        "50000.00",
                        "UGX",
                        "SUCCEEDED"
                )
        );

        AdmissionPaymentAllocation priorAllocation =
                mock(
                        AdmissionPaymentAllocation.class
                );

        when(
                priorAllocation.getAllocatedAmount()
        ).thenReturn(
                new BigDecimal("50000.00")
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
                List.of(
                        priorAllocation
                )
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
                List.of()
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.applySucceededPayment(
                                        tenantId,
                                        obligationId,
                                        paymentId,
                                        actorId,
                                        correlationId
                                )
                );

        assertEquals(
                "Admission payment obligation is already satisfied",
                exception.getMessage()
        );

        assertEquals(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                obligation.getGateStatus()
        );

        verify(
                allocations,
                never()
        ).save(
                any()
        );

        verify(
                obligation,
                never()
        ).updateFromPaymentEvaluation(
                any(AdmissionPaymentGateStatus.class),
                any()
        );

        verify(
                obligations,
                never()
        ).save(
                any()
        );

        verifyNoInteractions(
                history
        );
    }


    @Test
    void fullyAllocatedPaymentCannotBeAllocatedAgainElsewhere() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        prepareObligation(
                tenantId,
                obligationId,
                applicationId,
                "50000.00",
                "0.00"
        );

        prepareApplication(
                tenantId,
                applicationId,
                "PPIS-2026-ADM-000009"
        );

        when(
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentId
                        )
        ).thenReturn(false);

        when(
                payments.requireForUpdate(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                payment(
                        paymentId,
                        "PPIS-2026-ADM-000009",
                        "50000.00",
                        "UGX",
                        "SUCCEEDED"
                )
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

        AdmissionPaymentAllocation priorAllocation =
                mock(
                        AdmissionPaymentAllocation.class
                );

        when(
                priorAllocation.getAllocatedAmount()
        ).thenReturn(
                new BigDecimal("50000.00")
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
                List.of(
                        priorAllocation
                )
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.applySucceededPayment(
                                        tenantId,
                                        obligationId,
                                        paymentId,
                                        UUID.randomUUID(),
                                        "test-admission-payment-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "Payment transaction has no unallocated amount remaining",
                exception.getMessage()
        );

        verify(
                allocations,
                never()
        ).save(
                any()
        );
    }

    private AdmissionPaymentObligation prepareObligation(
            UUID tenantId,
            UUID obligationId,
            UUID applicationId,
            String requiredAmount,
            String waivedAmount
    ) {

        AdmissionPaymentObligation obligation =
                mock(
                        AdmissionPaymentObligation.class
                );

        AtomicReference<AdmissionPaymentGateStatus> gateStatus =
                new AtomicReference<>(
                        AdmissionPaymentGateStatus.PAYMENT_REQUIRED
                );

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

        lenient().when(
                obligation.getId()
        ).thenReturn(
                obligationId
        );

        when(
                obligation.getAdmissionApplicationId()
        ).thenReturn(
                applicationId
        );

        lenient().when(
                obligation.getCurrencyCode()
        ).thenReturn(
                "UGX"
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
        ).thenAnswer(
                invocation ->
                        gateStatus.get()
        );

        lenient().doAnswer(
                invocation -> {

                    gateStatus.set(
                            invocation.getArgument(0)
                    );

                    return null;
                }
        ).when(
                obligation
        ).updateFromPaymentEvaluation(
                any(AdmissionPaymentGateStatus.class),
                any()
        );

        return obligation;
    }

    private void prepareApplication(
            UUID tenantId,
            UUID applicationId,
            String applicationNumber
    ) {

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.of(
                        application
                )
        );

        lenient().when(
                application.getApplicationNumber()
        ).thenReturn(
                applicationNumber
        );
    }

    private void prepareSuccessfulAllocation(
            UUID tenantId,
            UUID obligationId,
            UUID paymentId,
            String merchantReference,
            String amount
    ) {

        when(
                allocations
                        .existsByTenantIdAndAdmissionPaymentObligationIdAndEipPaymentTransactionId(
                                tenantId,
                                obligationId,
                                paymentId
                        )
        ).thenReturn(false);

        when(
                payments.requireForUpdate(
                        tenantId,
                        paymentId
                )
        ).thenReturn(
                payment(
                        paymentId,
                        merchantReference,
                        amount,
                        "UGX",
                        "SUCCEEDED"
                )
        );

        when(
                allocations
                        .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                tenantId,
                                obligationId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(List.of());

        when(
                allocations
                        .findByTenantIdAndEipPaymentTransactionIdAndAllocationStatusAndStatus(
                                tenantId,
                                paymentId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(List.of());
    }

    private PaymentTransactionSnapshot payment(
            UUID id,
            String merchantReference,
            String amount,
            String currency,
            String status
    ) {

        return new PaymentTransactionSnapshot(
                id,
                merchantReference,
                new BigDecimal(
                        amount
                ),
                currency,
                status,
                "PROVIDER-REF",
                null
        );
    }

    private void assertMoney(
            String expected,
            BigDecimal actual
    ) {

        assertEquals(
                0,
                new BigDecimal(
                        expected
                ).compareTo(
                        actual
                )
        );
    }
}
