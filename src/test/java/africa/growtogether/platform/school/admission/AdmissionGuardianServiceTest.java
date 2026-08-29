package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionGuardianServiceTest {

    @Mock
    private AdmissionGuardianRepository repository;

    @Mock
    private AdmissionApplicationRepository applications;

    @Mock
    private AdmissionApplication application;

    private AdmissionGuardianService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionGuardianService(
                        repository,
                        applications
                );
    }

    @Test
    void createsTenantScopedPrimaryGuardian() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.of(application)
        );

        when(
                application.getAdmissionStatus()
        ).thenReturn(
                "DRAFT"
        );

        when(
                repository
                        .existsByTenantIdAndAdmissionApplicationIdAndPrimaryGuardianTrueAndStatus(
                                tenantId,
                                applicationId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                false
        );

        when(
                repository.save(
                        any(AdmissionGuardian.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionGuardian result =
                service.create(
                        tenantId,
                        applicationId,
                        command(
                                true,
                                true,
                                true,
                                true,
                                true
                        )
                );

        assertEquals(
                tenantId,
                result.getTenantId()
        );

        assertEquals(
                applicationId,
                result.getAdmissionApplicationId()
        );

        assertEquals(
                "MOTHER",
                result.getRelationshipType()
        );

        assertEquals(
                "Jane",
                result.getFirstName()
        );

        assertEquals(
                "Guardian",
                result.getLastName()
        );

        assertEquals(
                "+256700000001",
                result.getPhoneNumber()
        );

        assertTrue(
                result.isPrimaryGuardian()
        );

        assertTrue(
                result.isEmergencyContact()
        );

        assertTrue(
                result.isAuthorizedToCollect()
        );

        assertTrue(
                result.isReceivesCommunications()
        );

        assertTrue(
                result.isFinancialResponsibility()
        );

        verify(
                repository
        ).save(
                result
        );
    }

    @Test
    void preservesV033DefaultsWhenOptionalFlagsAreNotSupplied() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                repository.save(
                        any(AdmissionGuardian.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionGuardian result =
                service.create(
                        tenantId,
                        applicationId,
                        command(
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                );

        assertFalse(
                result.isPrimaryGuardian()
        );

        assertFalse(
                result.isEmergencyContact()
        );

        assertTrue(
                result.isAuthorizedToCollect()
        );

        assertTrue(
                result.isReceivesCommunications()
        );

        assertFalse(
                result.isFinancialResponsibility()
        );
    }

    @Test
    void allowsAdditionalNonPrimaryGuardianWhenPrimaryAlreadyExists() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                repository.save(
                        any(AdmissionGuardian.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionGuardian result =
                service.create(
                        tenantId,
                        applicationId,
                        command(
                                false,
                                true,
                                false,
                                true,
                                false
                        )
                );

        assertFalse(
                result.isPrimaryGuardian()
        );

        assertTrue(
                result.isEmergencyContact()
        );

        verify(
                repository,
                never()
        )
                .existsByTenantIdAndAdmissionApplicationIdAndPrimaryGuardianTrueAndStatus(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void rejectsSecondActivePrimaryGuardian() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                repository
                        .existsByTenantIdAndAdmissionApplicationIdAndPrimaryGuardianTrueAndStatus(
                                tenantId,
                                applicationId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                true
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.create(
                                        tenantId,
                                        applicationId,
                                        command(
                                                true,
                                                false,
                                                true,
                                                true,
                                                false
                                        )
                                )
                );

        assertEquals(
                "Active primary guardian already exists for application",
                exception.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void allowsPrimaryGuardianWhenNoActivePrimaryExists() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        when(
                repository
                        .existsByTenantIdAndAdmissionApplicationIdAndPrimaryGuardianTrueAndStatus(
                                tenantId,
                                applicationId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                false
        );

        when(
                repository.save(
                        any(AdmissionGuardian.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionGuardian result =
                service.create(
                        tenantId,
                        applicationId,
                        command(
                                true,
                                false,
                                true,
                                true,
                                false
                        )
                );

        assertTrue(
                result.isPrimaryGuardian()
        );

        verify(
                repository
        )
                .existsByTenantIdAndAdmissionApplicationIdAndPrimaryGuardianTrueAndStatus(
                        tenantId,
                        applicationId,
                        EntityStatus.ACTIVE
                );
    }

    @Test
    void rejectsGuardianForApplicationOutsideTenant() {

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
                                service.create(
                                        tenantId,
                                        applicationId,
                                        command(
                                                false,
                                                false,
                                                true,
                                                true,
                                                false
                                        )
                                )
                );

        assertEquals(
                "Admission application not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                repository
        );
    }

    @Test
    void rejectsGuardianAfterApplicationLeavesDraft() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.of(application)
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
                                service.create(
                                        tenantId,
                                        applicationId,
                                        command(
                                                false,
                                                false,
                                                true,
                                                true,
                                                false
                                        )
                                )
                );

        assertEquals(
                "Guardian cannot be added after admission application leaves DRAFT",
                exception.getMessage()
        );

        verifyNoInteractions(
                repository
        );
    }

    @Test
    void rejectsInvalidRelationshipType() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        prepareDraftApplication(
                tenantId,
                applicationId
        );

        CreateAdmissionGuardianCommand command =
                new CreateAdmissionGuardianCommand(
                        "FRIEND",
                        "Jane",
                        null,
                        "Guardian",
                        "+256700000001",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false,
                        false,
                        true,
                        true,
                        false
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.create(
                                        tenantId,
                                        applicationId,
                                        command
                                )
                );

        assertEquals(
                "Invalid relationship type: FRIEND",
                exception.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void rejectsMissingTenantBeforeRepositoryAccess() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                null,
                                UUID.randomUUID(),
                                command(
                                        false,
                                        false,
                                        true,
                                        true,
                                        false
                                )
                        )
        );

        verifyNoInteractions(
                applications,
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
                Optional.of(application)
        );

        when(
                application.getAdmissionStatus()
        ).thenReturn(
                "DRAFT"
        );
    }

    private CreateAdmissionGuardianCommand command(
            Boolean primaryGuardian,
            Boolean emergencyContact,
            Boolean authorizedToCollect,
            Boolean receivesCommunications,
            Boolean financialResponsibility
    ) {

        return new CreateAdmissionGuardianCommand(
                "mother",
                " Jane ",
                null,
                " Guardian ",
                " +256700000001 ",
                null,
                "jane@example.com",
                null,
                null,
                null,
                null,
                null,
                primaryGuardian,
                emergencyContact,
                authorizedToCollect,
                receivesCommunications,
                financialResponsibility
        );
    }
}
