package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.school.admission.AdmissionApplication;
import africa.growtogether.platform.school.admission.AdmissionApplicationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionOnboardingGateServiceTest {

    @Mock
    private AdmissionApplicationRepository applications;

    @Mock
    private AdmissionPaymentObligationRepository obligations;

    @Mock
    private AdmissionApplication application;

    private AdmissionOnboardingGateService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionOnboardingGateService(
                        applications,
                        obligations
                );
    }

    @Test
    void noAdmissionPaymentObligationsMeansUnlocked() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        prepareApplication(
                tenantId,
                applicationId
        );

        when(
                obligations.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of()
        );

        AdmissionOnboardingGateDecision decision =
                service.evaluate(
                        tenantId,
                        applicationId
                );

        assertTrue(
                decision.unlocked()
        );

        assertEquals(
                0,
                decision.requiredObligationCount()
        );

        assertEquals(
                0,
                decision.satisfiedObligationCount()
        );

        assertTrue(
                decision.blockingObligationIds()
                        .isEmpty()
        );
    }

    @Test
    void allMandatorySatisfiedOrWaivedMeansUnlocked() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        AdmissionPaymentObligation paid =
                mock(
                        AdmissionPaymentObligation.class
                );

        AdmissionPaymentObligation waived =
                mock(
                        AdmissionPaymentObligation.class
                );

        when(
                paid.isRequiredForOnboarding()
        ).thenReturn(true);

        when(
                paid.getGateStatus()
        ).thenReturn(
                AdmissionPaymentGateStatus.SATISFIED
        );

        when(
                waived.isRequiredForOnboarding()
        ).thenReturn(true);

        when(
                waived.getGateStatus()
        ).thenReturn(
                AdmissionPaymentGateStatus.WAIVED
        );

        prepareApplication(
                tenantId,
                applicationId
        );

        when(
                obligations.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of(
                        paid,
                        waived
                )
        );

        AdmissionOnboardingGateDecision decision =
                service.evaluate(
                        tenantId,
                        applicationId
                );

        assertTrue(
                decision.unlocked()
        );

        assertEquals(
                2,
                decision.requiredObligationCount()
        );

        assertEquals(
                2,
                decision.satisfiedObligationCount()
        );

        assertTrue(
                decision.blockingObligationIds()
                        .isEmpty()
        );
    }

    @Test
    void oneUnpaidMandatoryChargeKeepsOnboardingLocked() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID blockingId = UUID.randomUUID();

        AdmissionPaymentObligation paid =
                mock(
                        AdmissionPaymentObligation.class
                );

        AdmissionPaymentObligation unpaid =
                mock(
                        AdmissionPaymentObligation.class
                );

        when(
                paid.isRequiredForOnboarding()
        ).thenReturn(true);

        when(
                paid.getGateStatus()
        ).thenReturn(
                AdmissionPaymentGateStatus.SATISFIED
        );

        when(
                unpaid.isRequiredForOnboarding()
        ).thenReturn(true);

        when(
                unpaid.getGateStatus()
        ).thenReturn(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED
        );

        when(
                unpaid.getId()
        ).thenReturn(
                blockingId
        );

        prepareApplication(
                tenantId,
                applicationId
        );

        when(
                obligations.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of(
                        paid,
                        unpaid
                )
        );

        AdmissionOnboardingGateDecision decision =
                service.evaluate(
                        tenantId,
                        applicationId
                );

        assertFalse(
                decision.unlocked()
        );

        assertEquals(
                2,
                decision.requiredObligationCount()
        );

        assertEquals(
                1,
                decision.satisfiedObligationCount()
        );

        assertEquals(
                List.of(
                        blockingId
                ),
                decision.blockingObligationIds()
        );
    }

    @Test
    void optionalUnpaidChargeDoesNotBlockAdmissionOnboarding() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        AdmissionPaymentObligation mandatoryPaid =
                mock(
                        AdmissionPaymentObligation.class
                );

        AdmissionPaymentObligation optionalUnpaid =
                mock(
                        AdmissionPaymentObligation.class
                );

        when(
                mandatoryPaid.isRequiredForOnboarding()
        ).thenReturn(true);

        when(
                mandatoryPaid.getGateStatus()
        ).thenReturn(
                AdmissionPaymentGateStatus.SATISFIED
        );

        when(
                optionalUnpaid.isRequiredForOnboarding()
        ).thenReturn(false);

        prepareApplication(
                tenantId,
                applicationId
        );

        when(
                obligations.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of(
                        mandatoryPaid,
                        optionalUnpaid
                )
        );

        AdmissionOnboardingGateDecision decision =
                service.evaluate(
                        tenantId,
                        applicationId
                );

        assertTrue(
                decision.unlocked()
        );

        assertEquals(
                1,
                decision.requiredObligationCount()
        );

        assertEquals(
                1,
                decision.satisfiedObligationCount()
        );

        verify(
                optionalUnpaid,
                never()
        ).getGateStatus();
    }

    @Test
    void reviewRequiredAlwaysBlocksOnboarding() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID obligationId = UUID.randomUUID();

        AdmissionPaymentObligation reviewRequired =
                mock(
                        AdmissionPaymentObligation.class
                );

        when(
                reviewRequired.isRequiredForOnboarding()
        ).thenReturn(true);

        when(
                reviewRequired.getGateStatus()
        ).thenReturn(
                AdmissionPaymentGateStatus.REVIEW_REQUIRED
        );

        when(
                reviewRequired.getId()
        ).thenReturn(
                obligationId
        );

        prepareApplication(
                tenantId,
                applicationId
        );

        when(
                obligations.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of(
                        reviewRequired
                )
        );

        AdmissionOnboardingGateDecision decision =
                service.evaluate(
                        tenantId,
                        applicationId
                );

        assertFalse(
                decision.unlocked()
        );

        assertEquals(
                List.of(
                        obligationId
                ),
                decision.blockingObligationIds()
        );
    }

    @Test
    void applicationOutsideTenantCannotBeEvaluated() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.evaluate(
                                        tenantId,
                                        applicationId
                                )
                );

        assertEquals(
                "Admission application not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                obligations
        );
    }

    @Test
    void missingTenantIsRejectedBeforeRepositoryAccess() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.evaluate(
                                        null,
                                        UUID.randomUUID()
                                )
                );

        assertEquals(
                "tenantId must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(
                applications,
                obligations
        );
    }

    private void prepareApplication(
            UUID tenantId,
            UUID applicationId
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
    }
}
