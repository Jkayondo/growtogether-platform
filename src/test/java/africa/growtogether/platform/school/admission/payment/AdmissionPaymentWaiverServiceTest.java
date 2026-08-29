package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionPaymentWaiverServiceTest {

    @Mock
    private AdmissionPaymentObligationRepository obligations;

    @Mock
    private AdmissionPaymentAllocationRepository allocations;

    @Mock
    private AdmissionPaymentGateHistoryRepository history;

    private AdmissionPaymentWaiverService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionPaymentWaiverService(
                        obligations,
                        allocations,
                        history
                );
    }

    @Test
    void fullWaiverWithoutPaymentMarksObligationWaived() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                obligation(
                        tenantId,
                        "50000.00"
                );

        prepare(
                tenantId,
                obligationId,
                obligation,
                List.of()
        );

        AdmissionPaymentWaiverResult result =
                service.waive(
                        tenantId,
                        obligationId,
                        new BigDecimal("50000.00"),
                        "Approved full bursary",
                        actorId,
                        "test-admission-waiver-" + UUID.randomUUID()
                );

        assertMoney(
                "50000.00",
                result.waivedAmount()
        );

        assertMoney(
                "0.00",
                result.outstandingAmount()
        );

        assertEquals(
                AdmissionPaymentGateStatus.WAIVED,
                result.gateStatus()
        );

        assertEquals(
                actorId,
                obligation.getWaivedBy()
        );

        assertNotNull(
                obligation.getWaivedAt()
        );

        assertNotNull(
                obligation.getSatisfiedAt()
        );

        assertEquals(
                actorId,
                obligation.getSatisfiedBy()
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
    void paymentPlusWaiverOfRemainderSatisfiesObligation() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                obligation(
                        tenantId,
                        "50000.00"
                );

        AdmissionPaymentAllocation allocation =
                allocation(
                        "30000.00"
                );

        prepare(
                tenantId,
                obligationId,
                obligation,
                List.of(
                        allocation
                )
        );

        AdmissionPaymentWaiverResult result =
                service.waive(
                        tenantId,
                        obligationId,
                        new BigDecimal("20000.00"),
                        "Approved balance waiver",
                        actorId,
                        "test-admission-waiver-" + UUID.randomUUID()
                );

        assertMoney(
                "30000.00",
                result.allocatedAmount()
        );

        assertMoney(
                "20000.00",
                result.waivedAmount()
        );

        assertMoney(
                "0.00",
                result.outstandingAmount()
        );

        assertEquals(
                AdmissionPaymentGateStatus.SATISFIED,
                result.gateStatus()
        );

        assertNotNull(
                obligation.getSatisfiedAt()
        );
    }

    @Test
    void partialWaiverWithoutPaymentKeepsPaymentRequired() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                obligation(
                        tenantId,
                        "50000.00"
                );

        prepare(
                tenantId,
                obligationId,
                obligation,
                List.of()
        );

        AdmissionPaymentWaiverResult result =
                service.waive(
                        tenantId,
                        obligationId,
                        new BigDecimal("20000.00"),
                        "Approved partial waiver",
                        UUID.randomUUID(),
                        "test-admission-waiver-" + UUID.randomUUID()
                );

        assertMoney(
                "20000.00",
                result.waivedAmount()
        );

        assertMoney(
                "30000.00",
                result.outstandingAmount()
        );

        assertEquals(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                result.gateStatus()
        );

        assertNull(
                obligation.getSatisfiedAt()
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

        AdmissionPaymentGateHistory evidence =
                captor.getValue();

        assertEquals(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                evidence.getPreviousGateStatus()
        );

        assertEquals(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                evidence.getNewGateStatus()
        );
    }

    @Test
    void partialPaymentAndPartialWaiverRemainPartiallySatisfied() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                obligation(
                        tenantId,
                        "50000.00"
                );

        AdmissionPaymentAllocation allocation =
                allocation(
                        "15000.00"
                );

        prepare(
                tenantId,
                obligationId,
                obligation,
                List.of(
                        allocation
                )
        );

        AdmissionPaymentWaiverResult result =
                service.waive(
                        tenantId,
                        obligationId,
                        new BigDecimal("10000.00"),
                        "Partial support approved",
                        UUID.randomUUID(),
                        "test-admission-waiver-" + UUID.randomUUID()
                );

        assertMoney(
                "15000.00",
                result.allocatedAmount()
        );

        assertMoney(
                "10000.00",
                result.waivedAmount()
        );

        assertMoney(
                "25000.00",
                result.outstandingAmount()
        );

        assertEquals(
                AdmissionPaymentGateStatus.PARTIALLY_SATISFIED,
                result.gateStatus()
        );
    }

    @Test
    void waiverCannotExceedOutstandingBalance() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                obligation(
                        tenantId,
                        "50000.00"
                );

        AdmissionPaymentAllocation allocation =
                allocation(
                        "30000.00"
                );

        prepare(
                tenantId,
                obligationId,
                obligation,
                List.of(
                        allocation
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.waive(
                                        tenantId,
                                        obligationId,
                                        new BigDecimal("25000.00"),
                                        "Too much waiver",
                                        UUID.randomUUID(),
                                        "test-admission-waiver-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "waiverAmount cannot exceed outstanding admission balance",
                exception.getMessage()
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
    void fullyPaidObligationHasNothingToWaive() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                obligation(
                        tenantId,
                        "50000.00"
                );

        AdmissionPaymentAllocation allocation =
                allocation(
                        "50000.00"
                );

        prepare(
                tenantId,
                obligationId,
                obligation,
                List.of(
                        allocation
                )
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.waive(
                                        tenantId,
                                        obligationId,
                                        new BigDecimal("10000.00"),
                                        "Should not apply",
                                        UUID.randomUUID(),
                                        "test-admission-waiver-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "Admission payment obligation has no outstanding amount to waive",
                exception.getMessage()
        );
    }

    @Test
    void secondWaiverIsRejected() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                obligation(
                        tenantId,
                        "50000.00"
                );

        obligation.applyWaiver(
                new BigDecimal("10000.00"),
                "Existing approved waiver",
                actorId
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

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.waive(
                                        tenantId,
                                        obligationId,
                                        new BigDecimal("5000.00"),
                                        "Second waiver",
                                        UUID.randomUUID(),
                                        "test-admission-waiver-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "Admission payment obligation already has a waiver",
                exception.getMessage()
        );

        verifyNoInteractions(
                allocations,
                history
        );
    }

    @Test
    void crossTenantObligationIsRejected() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();

        when(
                obligations.findForUpdate(
                        tenantId,
                        obligationId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.waive(
                                        tenantId,
                                        obligationId,
                                        new BigDecimal("5000.00"),
                                        "Approved waiver",
                                        UUID.randomUUID(),
                                        "test-admission-waiver-" + UUID.randomUUID()
                                )
                );

        assertEquals(
                "Admission payment obligation not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                allocations,
                history
        );
    }

    @Test
    void cannotMarkFullyWaivedWithoutFullWaiverEvidence() {

        AdmissionPaymentObligation obligation =
                obligation(
                        UUID.randomUUID(),
                        "50000.00"
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                obligation.markFullyWaived(
                                        UUID.randomUUID()
                                )
                );

        assertEquals(
                "Admission payment obligation cannot be marked fully waived without full waiver evidence",
                exception.getMessage()
        );

        assertEquals(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                obligation.getGateStatus()
        );
    }


    @Test
    void historyRecordsHumanActorAndReason() {

        UUID tenantId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        String correlationId = "test-admission-waiver-" + UUID.randomUUID();

        AdmissionPaymentObligation obligation =
                obligation(
                        tenantId,
                        "50000.00"
                );

        prepare(
                tenantId,
                obligationId,
                obligation,
                List.of()
        );

        service.waive(
                tenantId,
                obligationId,
                new BigDecimal("50000.00"),
                "Head teacher approved bursary",
                actorId,
                correlationId
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

        AdmissionPaymentGateHistory evidence =
                captor.getValue();

        assertEquals(
                tenantId,
                evidence.getTenantId()
        );

        assertEquals(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                evidence.getPreviousGateStatus()
        );

        assertEquals(
                AdmissionPaymentGateStatus.WAIVED,
                evidence.getNewGateStatus()
        );

        assertEquals(
                actorId,
                evidence.getChangedBy()
        );

        assertEquals(
                correlationId,
                evidence.getCorrelationId()
        );

        assertTrue(
                evidence.getReason()
                        .contains(
                                "Head teacher approved bursary"
                        )
        );
    }

    private AdmissionPaymentObligation obligation(
            UUID tenantId,
            String requiredAmount
    ) {

        AdmissionPaymentObligation obligation =
                new AdmissionPaymentObligation(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "UGX",
                        new BigDecimal(
                                requiredAmount
                        ),
                        true
                );

        obligation.setTenantId(
                tenantId
        );

        return obligation;
    }

    private AdmissionPaymentAllocation allocation(
            String amount
    ) {

        return new AdmissionPaymentAllocation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal(
                        amount
                ),
                UUID.randomUUID()
        );
    }

    private void prepare(
            UUID tenantId,
            UUID obligationId,
            AdmissionPaymentObligation obligation,
            List<AdmissionPaymentAllocation> activeAllocations
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
                allocations
                        .findByTenantIdAndAdmissionPaymentObligationIdAndAllocationStatusAndStatus(
                                tenantId,
                                obligationId,
                                AdmissionPaymentAllocationStatus.APPLIED,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                activeAllocations
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
                invocation -> invocation.getArgument(0)
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
