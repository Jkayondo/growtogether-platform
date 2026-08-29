package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.eiam.membership.CreateRoleCodeInvitationCommand;
import africa.growtogether.platform.eiam.membership.InvitationStatus;
import africa.growtogether.platform.eiam.membership.InvitationView;
import africa.growtogether.platform.eiam.membership.MembershipService;

import africa.growtogether.platform.school.admission.payment.AdmissionOnboardingGateDecision;
import africa.growtogether.platform.school.admission.payment.AdmissionOnboardingGateService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmissionGuardianAccountProvisioningServiceTest {

    @Mock
    private AdmissionGuardianRepository guardians;

    @Mock
    private AdmissionGuardianAccountProvisioningRepository provisionings;

    @Mock
    private AdmissionOnboardingGateService onboardingGate;

    @Mock
    private MembershipService memberships;

    private AdmissionGuardianAccountProvisioningService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionGuardianAccountProvisioningService(
                        guardians,
                        provisionings,
                        onboardingGate,
                        memberships
                );
    }

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void canonicalPhoneIsPreferredAndProvisioningEvidenceIsSaved() {

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        UUID invitationId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        AdmissionGuardian guardian =
                guardian(
                        applicationId,
                        "+256701234567",
                        "parent@example.com"
                );

        when(
                guardians.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                java.util.Optional.of(
                        guardian
                )
        );

        when(
                provisionings.existsByTenantIdAndAdmissionGuardianId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                false
        );

        when(
                onboardingGate.evaluate(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                unlockedGate(
                        applicationId
                )
        );

        InvitationView invitation =
                new InvitationView(
                        invitationId,
                        null,
                        "+256701234567",
                        InvitationStatus.PENDING,
                        Instant.parse(
                                "2026-08-30T12:00:00Z"
                        ),
                        Set.of(
                                UUID.randomUUID()
                        ),
                        "one-time-secret-token"
                );

        when(
                memberships.createInvitationByRoleCodes(
                        any(
                                CreateRoleCodeInvitationCommand.class
                        )
                )
        ).thenReturn(
                invitation
        );

        when(
                provisionings.saveAndFlush(
                        any(
                                AdmissionGuardianAccountProvisioning.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        AdmissionGuardianAccountProvisioningResult result =
                service.provisionParentAccount(
                        guardianId
                );

        ArgumentCaptor<CreateRoleCodeInvitationCommand> commandCaptor =
                ArgumentCaptor.forClass(
                        CreateRoleCodeInvitationCommand.class
                );

        verify(
                memberships
        ).createInvitationByRoleCodes(
                commandCaptor.capture()
        );

        CreateRoleCodeInvitationCommand command =
                commandCaptor.getValue();

        assertEquals(
                null,
                command.email()
        );

        assertEquals(
                "+256701234567",
                command.phoneNumber()
        );

        assertEquals(
                Set.of(
                        "PARENT"
                ),
                command.roleCodes()
        );

        ArgumentCaptor<AdmissionGuardianAccountProvisioning>
                provisioningCaptor =
                ArgumentCaptor.forClass(
                        AdmissionGuardianAccountProvisioning.class
                );

        verify(
                provisionings
        ).saveAndFlush(
                provisioningCaptor.capture()
        );

        AdmissionGuardianAccountProvisioning saved =
                provisioningCaptor.getValue();

        assertEquals(
                tenantId,
                saved.getTenantId()
        );

        assertEquals(
                guardianId,
                saved.getAdmissionGuardianId()
        );

        assertEquals(
                invitationId,
                saved.getInvitationId()
        );

        assertEquals(
                AdmissionGuardianContactIdentityType.PHONE,
                saved.getContactIdentityType()
        );

        assertEquals(
                "+256701234567",
                saved.getContactIdentity()
        );

        assertTrue(
                saved.isPendingActivation()
        );

        assertEquals(
                "one-time-secret-token",
                result.acceptanceToken()
        );

        assertFalse(
                result.toString()
                        .contains(
                                "one-time-secret-token"
                        )
        );

        assertTrue(
                result.toString()
                        .contains(
                                "acceptanceToken=<redacted>"
                        )
        );

        InOrder order =
                inOrder(
                        onboardingGate,
                        memberships,
                        provisionings
                );

        order.verify(
                onboardingGate
        ).evaluate(
                tenantId,
                applicationId
        );

        order.verify(
                memberships
        ).createInvitationByRoleCodes(
                any(
                        CreateRoleCodeInvitationCommand.class
                )
        );

        order.verify(
                provisionings
        ).saveAndFlush(
                any(
                        AdmissionGuardianAccountProvisioning.class
                )
        );
    }

    @Test
    void nonCanonicalPhoneFallsBackToNormalizedEmail() {

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        UUID invitationId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        AdmissionGuardian guardian =
                guardian(
                        applicationId,
                        "0701234567",
                        " Parent@Example.COM "
                );

        prepareProvisionableGuardian(
                tenantId,
                guardianId,
                applicationId,
                guardian
        );

        when(
                memberships.createInvitationByRoleCodes(
                        any(
                                CreateRoleCodeInvitationCommand.class
                        )
                )
        ).thenReturn(
                new InvitationView(
                        invitationId,
                        "parent@example.com",
                        null,
                        InvitationStatus.PENDING,
                        Instant.parse(
                                "2026-08-30T12:00:00Z"
                        ),
                        Set.of(
                                UUID.randomUUID()
                        ),
                        "email-token"
                )
        );

        when(
                provisionings.saveAndFlush(
                        any(
                                AdmissionGuardianAccountProvisioning.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        service.provisionParentAccount(
                guardianId
        );

        ArgumentCaptor<CreateRoleCodeInvitationCommand> commandCaptor =
                ArgumentCaptor.forClass(
                        CreateRoleCodeInvitationCommand.class
                );

        verify(
                memberships
        ).createInvitationByRoleCodes(
                commandCaptor.capture()
        );

        CreateRoleCodeInvitationCommand command =
                commandCaptor.getValue();

        assertEquals(
                "parent@example.com",
                command.email()
        );

        assertEquals(
                null,
                command.phoneNumber()
        );

        assertEquals(
                Set.of(
                        "PARENT"
                ),
                command.roleCodes()
        );
    }

    @Test
    void paymentGateBlocksBeforeEiamInvitation() {

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        AdmissionGuardian guardian =
                guardian(
                        applicationId,
                        "+256701234567",
                        null
                );

        when(
                guardians.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                java.util.Optional.of(
                        guardian
                )
        );

        when(
                provisionings.existsByTenantIdAndAdmissionGuardianId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                false
        );

        UUID blockingObligationId =
                UUID.randomUUID();

        when(
                onboardingGate.evaluate(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                new AdmissionOnboardingGateDecision(
                        applicationId,
                        false,
                        2,
                        1,
                        List.of(
                                blockingObligationId
                        )
                )
        );

        AdmissionGuardianAccountProvisioningException error =
                assertThrows(
                        AdmissionGuardianAccountProvisioningException.class,
                        () ->
                                service.provisionParentAccount(
                                        guardianId
                                )
                );

        assertEquals(
                "Parent account provisioning is blocked by outstanding admission onboarding obligations",
                error.getMessage()
        );

        verify(
                memberships,
                never()
        ).createInvitationByRoleCodes(
                any(
                        CreateRoleCodeInvitationCommand.class
                )
        );

        verify(
                provisionings,
                never()
        ).saveAndFlush(
                any(
                        AdmissionGuardianAccountProvisioning.class
                )
        );
    }

    @Test
    void existingProvisioningStopsBeforePaymentGateAndEiam() {

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        AdmissionGuardian guardian =
                guardian(
                        applicationId,
                        "+256701234567",
                        null
                );

        when(
                guardians.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                java.util.Optional.of(
                        guardian
                )
        );

        when(
                provisionings.existsByTenantIdAndAdmissionGuardianId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                true
        );

        AdmissionGuardianAccountProvisioningException error =
                assertThrows(
                        AdmissionGuardianAccountProvisioningException.class,
                        () ->
                                service.provisionParentAccount(
                                        guardianId
                                )
                );

        assertEquals(
                "Parent account provisioning already exists for this admission guardian",
                error.getMessage()
        );

        verify(
                onboardingGate,
                never()
        ).evaluate(
                any(),
                any()
        );

        verify(
                memberships,
                never()
        ).createInvitationByRoleCodes(
                any(
                        CreateRoleCodeInvitationCommand.class
                )
        );
    }

    @Test
    void localPhoneWithoutValidEmailFailsSafelyWithoutCountryGuessing() {

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        AdmissionGuardian guardian =
                guardian(
                        applicationId,
                        "0701234567",
                        null
                );

        prepareProvisionableGuardian(
                tenantId,
                guardianId,
                applicationId,
                guardian
        );

        AdmissionGuardianAccountProvisioningException error =
                assertThrows(
                        AdmissionGuardianAccountProvisioningException.class,
                        () ->
                                service.provisionParentAccount(
                                        guardianId
                                )
                );

        assertEquals(
                "Admission guardian requires a canonical international phone number or valid email before parent account provisioning",
                error.getMessage()
        );

        verify(
                memberships,
                never()
        ).createInvitationByRoleCodes(
                any(
                        CreateRoleCodeInvitationCommand.class
                )
        );

        verify(
                provisionings,
                never()
        ).saveAndFlush(
                any(
                        AdmissionGuardianAccountProvisioning.class
                )
        );
    }

    @Test
    void mismatchedEiamInvitationIdentityIsRejectedBeforeEvidencePersistence() {

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        AdmissionGuardian guardian =
                guardian(
                        applicationId,
                        "+256701234567",
                        null
                );

        prepareProvisionableGuardian(
                tenantId,
                guardianId,
                applicationId,
                guardian
        );

        when(
                memberships.createInvitationByRoleCodes(
                        any(
                                CreateRoleCodeInvitationCommand.class
                        )
                )
        ).thenReturn(
                new InvitationView(
                        UUID.randomUUID(),
                        null,
                        "+256799999999",
                        InvitationStatus.PENDING,
                        Instant.parse(
                                "2026-08-30T12:00:00Z"
                        ),
                        Set.of(
                                UUID.randomUUID()
                        ),
                        "token"
                )
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.provisionParentAccount(
                                        guardianId
                                )
                );

        assertEquals(
                "EIAM invitation identity does not match the selected admission guardian identity",
                error.getMessage()
        );

        verify(
                provisionings,
                never()
        ).saveAndFlush(
                any(
                        AdmissionGuardianAccountProvisioning.class
                )
        );
    }

    private void prepareProvisionableGuardian(
            UUID tenantId,
            UUID guardianId,
            UUID applicationId,
            AdmissionGuardian guardian
    ) {

        when(
                guardians.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                java.util.Optional.of(
                        guardian
                )
        );

        when(
                provisionings.existsByTenantIdAndAdmissionGuardianId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                false
        );

        when(
                onboardingGate.evaluate(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                unlockedGate(
                        applicationId
                )
        );
    }

    private static AdmissionOnboardingGateDecision unlockedGate(
            UUID applicationId
    ) {

        return new AdmissionOnboardingGateDecision(
                applicationId,
                true,
                1,
                1,
                List.of()
        );
    }

    private static AdmissionGuardian guardian(
            UUID applicationId,
            String phone,
            String email
    ) {

        AdmissionGuardian guardian =
                mock(
                        AdmissionGuardian.class
                );

        lenient().when(
                guardian.getAdmissionApplicationId()
        ).thenReturn(
                applicationId
        );

        lenient().when(
                guardian.getPhoneNumber()
        ).thenReturn(
                phone
        );

        lenient().when(
                guardian.getEmail()
        ).thenReturn(
                email
        );

        when(
                guardian.getStatus()
        ).thenReturn(
                EntityStatus.ACTIVE
        );

        return guardian;
    }

    private static void setTenant(
            UUID tenantId
    ) {

        RequestContextHolder.set(
                new RequestContext(
                        "test",
                        tenantId.toString()
                )
        );
    }
}
