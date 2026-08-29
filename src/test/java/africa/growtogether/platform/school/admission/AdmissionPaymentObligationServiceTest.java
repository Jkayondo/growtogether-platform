package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.school.admission.payment.AdmissionFeeConfiguration;
import africa.growtogether.platform.school.admission.payment.AdmissionFeeConfigurationGateway;
import africa.growtogether.platform.school.admission.payment.AdmissionPaymentGateStatus;
import africa.growtogether.platform.school.admission.payment.AdmissionPaymentObligation;
import africa.growtogether.platform.school.admission.payment.AdmissionPaymentObligationRepository;
import africa.growtogether.platform.school.admission.payment.AdmissionPaymentObligationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
class AdmissionPaymentObligationServiceTest {

    @Mock
    private AdmissionPaymentObligationRepository repository;

    @Mock
    private AdmissionApplicationRepository applications;

    @Mock
    private AdmissionFeeConfigurationGateway feeConfigurations;

    @Mock
    private AdmissionApplication application;

    private AdmissionPaymentObligationService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionPaymentObligationService(
                        repository,
                        applications,
                        feeConfigurations
                );
    }

    @Test
    void createsObligationFromConfiguredAdmissionFee() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID feeItemId = UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                feeConfigurations.resolve(
                        tenantId,
                        application
                )
        ).thenReturn(
                List.of(
                        configuration(
                                feeItemId,
                                "50000.00",
                                "1"
                        )
                )
        );

        when(
                repository.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of()
        );

        when(
                repository.save(
                        any(AdmissionPaymentObligation.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        List<AdmissionPaymentObligation> result =
                service.ensureForApplication(
                        tenantId,
                        applicationId
                );

        assertEquals(
                1,
                result.size()
        );

        AdmissionPaymentObligation obligation =
                result.get(0);

        assertEquals(
                tenantId,
                obligation.getTenantId()
        );

        assertEquals(
                applicationId,
                obligation.getAdmissionApplicationId()
        );

        assertEquals(
                feeItemId,
                obligation.getFeeItemId()
        );

        assertEquals(
                "UGX",
                obligation.getCurrencyCode()
        );

        assertEquals(
                0,
                new BigDecimal("50000.00")
                        .compareTo(
                                obligation.getRequiredAmount()
                        )
        );

        assertEquals(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED,
                obligation.getGateStatus()
        );

        assertTrue(
                obligation.isRequiredForOnboarding()
        );

        verify(
                repository
        ).save(
                obligation
        );
    }

    @Test
    void calculatesConfiguredAmountUsingQuantity() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID feeItemId = UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                feeConfigurations.resolve(
                        tenantId,
                        application
                )
        ).thenReturn(
                List.of(
                        configuration(
                                feeItemId,
                                "25000.00",
                                "2"
                        )
                )
        );

        when(
                repository.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of()
        );

        when(
                repository.save(
                        any(AdmissionPaymentObligation.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionPaymentObligation obligation =
                service
                        .ensureForApplication(
                                tenantId,
                                applicationId
                        )
                        .get(0);

        assertEquals(
                0,
                new BigDecimal("50000.00")
                        .compareTo(
                                obligation.getRequiredAmount()
                        )
        );
    }

    @Test
    void doesNotDuplicateExistingObligation() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID feeItemId = UUID.randomUUID();

        AdmissionPaymentObligation existing =
                mock(
                        AdmissionPaymentObligation.class
                );

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                feeConfigurations.resolve(
                        tenantId,
                        application
                )
        ).thenReturn(
                List.of(
                        configuration(
                                feeItemId,
                                "50000.00",
                                "1"
                        )
                )
        );

        when(
                existing.getFeeItemId()
        ).thenReturn(
                feeItemId
        );

        when(
                repository.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of(
                        existing
                )
        );

        List<AdmissionPaymentObligation> result =
                service.ensureForApplication(
                        tenantId,
                        applicationId
                );

        assertEquals(
                1,
                result.size()
        );

        assertSame(
                existing,
                result.get(0)
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void createsMultipleConfiguredAdmissionObligations() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        UUID admissionFeeId =
                UUID.randomUUID();

        UUID secondAdmissionItemId =
                UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                feeConfigurations.resolve(
                        tenantId,
                        application
                )
        ).thenReturn(
                List.of(
                        configuration(
                                admissionFeeId,
                                "50000.00",
                                "1"
                        ),
                        configuration(
                                secondAdmissionItemId,
                                "20000.00",
                                "1"
                        )
                )
        );

        when(
                repository.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of()
        );

        when(
                repository.save(
                        any(AdmissionPaymentObligation.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        List<AdmissionPaymentObligation> result =
                service.ensureForApplication(
                        tenantId,
                        applicationId
                );

        assertEquals(
                2,
                result.size()
        );

        verify(
                repository,
                times(2)
        ).save(
                any(AdmissionPaymentObligation.class)
        );
    }

    @Test
    void zeroValueConfiguredFeeIsImmediatelySatisfied() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                feeConfigurations.resolve(
                        tenantId,
                        application
                )
        ).thenReturn(
                List.of(
                        configuration(
                                UUID.randomUUID(),
                                "0.00",
                                "1"
                        )
                )
        );

        when(
                repository.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of()
        );

        when(
                repository.save(
                        any(AdmissionPaymentObligation.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionPaymentObligation obligation =
                service
                        .ensureForApplication(
                                tenantId,
                                applicationId
                        )
                        .get(0);

        assertEquals(
                AdmissionPaymentGateStatus.SATISFIED,
                obligation.getGateStatus()
        );

        assertNotNull(
                obligation.getSatisfiedAt()
        );
    }

    @Test
    void noConfiguredAdmissionFeeCreatesNoObligation() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                feeConfigurations.resolve(
                        tenantId,
                        application
                )
        ).thenReturn(
                List.of()
        );

        when(
                repository.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of()
        );

        List<AdmissionPaymentObligation> result =
                service.ensureForApplication(
                        tenantId,
                        applicationId
                );

        assertTrue(
                result.isEmpty()
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void rejectsApplicationOutsideTenant() {

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
                                service.ensureForApplication(
                                        tenantId,
                                        applicationId
                                )
                );

        assertEquals(
                "Admission application not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                feeConfigurations,
                repository
        );
    }

    @Test
    void rejectsObligationCreationAfterApplicationLeavesDraft() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

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

        when(
                application.getAdmissionStatus()
        ).thenReturn(
                "SUBMITTED"
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.ensureForApplication(
                                        tenantId,
                                        applicationId
                                )
                );

        assertEquals(
                "Admission payment obligations can only be established while application is DRAFT",
                exception.getMessage()
        );

        verifyNoInteractions(
                feeConfigurations,
                repository
        );
    }

    @Test
    void rejectsMissingTenantBeforeRepositoryAccess() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.ensureForApplication(
                                null,
                                UUID.randomUUID()
                        )
        );

        verifyNoInteractions(
                applications,
                feeConfigurations,
                repository
        );
    }

    private void prepareDraftApplication(
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

        when(
                application.getAdmissionStatus()
        ).thenReturn(
                "DRAFT"
        );
    }

    private AdmissionFeeConfiguration configuration(
            UUID feeItemId,
            String unitAmount,
            String quantity
    ) {

        return new AdmissionFeeConfiguration(
                feeItemId,
                "ADM-FEE",
                "Admission Fee",
                "UGX",
                new BigDecimal(
                        unitAmount
                ),
                new BigDecimal(
                        quantity
                ),
                false,
                false,
                null
        );
    }
}
