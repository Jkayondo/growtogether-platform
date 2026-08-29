package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.eiam.membership.AcceptInvitationCommand;
import africa.growtogether.platform.eiam.membership.InvitationAcceptanceEvidence;
import africa.growtogether.platform.eiam.membership.MembershipService;
import africa.growtogether.platform.eiam.membership.MembershipStatus;
import africa.growtogether.platform.eiam.membership.MembershipView;

import africa.growtogether.platform.school.admission.payment.AdmissionOnboardingGateService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmissionGuardianAccountActivationServiceTest {

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
    void matchingInvitationActivatesProvisioningAndReturnsDurableEvidence() {

        UUID tenantId =
                UUID.randomUUID();

        UUID provisioningId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID invitationId =
                UUID.randomUUID();

        UUID membershipId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        Instant joinedAt =
                Instant.parse(
                        "2026-08-24T09:00:00Z"
                );

        Instant activatedAt =
                Instant.parse(
                        "2026-08-24T09:05:00Z"
                );

        setTenant(
                tenantId
        );

        AdmissionGuardianAccountProvisioning provisioning =
                mock(
                        AdmissionGuardianAccountProvisioning.class
                );

        preparePendingSecureProvisioning(
                tenantId,
                provisioningId,
                invitationId,
                provisioning
        );

        AcceptInvitationCommand command =
                command();

        MembershipView membership =
                new MembershipView(
                        membershipId,
                        userId,
                        MembershipStatus.ACTIVE,
                        joinedAt,
                        null
                );

        when(
                memberships.acceptWithEvidence(
                        command
                )
        ).thenReturn(
                new InvitationAcceptanceEvidence(
                        invitationId,
                        membership
                )
        );

        when(
                provisionings.saveAndFlush(
                        provisioning
                )
        ).thenReturn(
                provisioning
        );

        when(
                provisioning.getId()
        ).thenReturn(
                provisioningId
        );

        when(
                provisioning.getAdmissionGuardianId()
        ).thenReturn(
                guardianId
        );

        when(
                provisioning.getEiamUserId()
        ).thenReturn(
                userId
        );

        when(
                provisioning.getProvisioningStatus()
        ).thenReturn(
                AdmissionGuardianProvisioningStatus.ACTIVATED
        );

        when(
                provisioning.getActivatedAt()
        ).thenReturn(
                activatedAt
        );

        AdmissionGuardianAccountActivationResult result =
                service.completeParentAccountActivation(
                        provisioningId,
                        command
                );

        verify(
                provisioning
        ).activate(
                org.mockito.ArgumentMatchers.eq(
                        userId
                ),
                any(
                        Instant.class
                )
        );

        assertEquals(
                provisioningId,
                result.provisioningId()
        );

        assertEquals(
                guardianId,
                result.admissionGuardianId()
        );

        assertEquals(
                invitationId,
                result.invitationId()
        );

        assertEquals(
                membershipId,
                result.membershipId()
        );

        assertEquals(
                userId,
                result.eiamUserId()
        );

        assertEquals(
                AdmissionGuardianProvisioningStatus.ACTIVATED,
                result.provisioningStatus()
        );

        assertEquals(
                activatedAt,
                result.activatedAt()
        );

        assertNotNull(
                result.activatedAt()
        );

        InOrder order =
                inOrder(
                        memberships,
                        provisioning,
                        provisionings
                );

        order.verify(
                memberships
        ).acceptWithEvidence(
                command
        );

        order.verify(
                provisioning
        ).activate(
                org.mockito.ArgumentMatchers.eq(
                        userId
                ),
                any(
                        Instant.class
                )
        );

        order.verify(
                provisionings
        ).saveAndFlush(
                provisioning
        );
    }

    @Test
    void differentAcceptedInvitationIsRejectedBeforeV156Activation() {

        UUID tenantId =
                UUID.randomUUID();

        UUID provisioningId =
                UUID.randomUUID();

        UUID expectedInvitationId =
                UUID.randomUUID();

        UUID differentInvitationId =
                UUID.randomUUID();

        UUID membershipId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        AdmissionGuardianAccountProvisioning provisioning =
                mock(
                        AdmissionGuardianAccountProvisioning.class
                );

        preparePendingSecureProvisioning(
                tenantId,
                provisioningId,
                expectedInvitationId,
                provisioning
        );

        AcceptInvitationCommand command =
                command();

        when(
                memberships.acceptWithEvidence(
                        command
                )
        ).thenReturn(
                new InvitationAcceptanceEvidence(
                        differentInvitationId,
                        new MembershipView(
                                membershipId,
                                userId,
                                MembershipStatus.ACTIVE,
                                Instant.now(),
                                null
                        )
                )
        );

        AdmissionGuardianAccountProvisioningException error =
                assertThrows(
                        AdmissionGuardianAccountProvisioningException.class,
                        () ->
                                service.completeParentAccountActivation(
                                        provisioningId,
                                        command
                                )
                );

        assertEquals(
                "Accepted EIAM invitation does not belong to this guardian account provisioning",
                error.getMessage()
        );

        verify(
                provisioning,
                never()
        ).activate(
                any(),
                any()
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
    void cancelledProvisioningIsRejectedBeforeEiamAcceptance() {

        UUID tenantId =
                UUID.randomUUID();

        UUID provisioningId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        AdmissionGuardianAccountProvisioning provisioning =
                mock(
                        AdmissionGuardianAccountProvisioning.class
                );

        when(
                provisionings.findByTenantIdAndId(
                        tenantId,
                        provisioningId
                )
        ).thenReturn(
                Optional.of(
                        provisioning
                )
        );

        when(
                provisioning.isCancelled()
        ).thenReturn(
                true
        );

        AdmissionGuardianAccountProvisioningException error =
                assertThrows(
                        AdmissionGuardianAccountProvisioningException.class,
                        () ->
                                service.completeParentAccountActivation(
                                        provisioningId,
                                        command()
                                )
                );

        assertEquals(
                "Cancelled guardian account provisioning cannot be completed",
                error.getMessage()
        );

        verify(
                memberships,
                never()
        ).acceptWithEvidence(
                any(
                        AcceptInvitationCommand.class
                )
        );
    }

    @Test
    void alreadyActivatedProvisioningIsRejectedBeforeEiamAcceptance() {

        UUID tenantId =
                UUID.randomUUID();

        UUID provisioningId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        AdmissionGuardianAccountProvisioning provisioning =
                mock(
                        AdmissionGuardianAccountProvisioning.class
                );

        when(
                provisionings.findByTenantIdAndId(
                        tenantId,
                        provisioningId
                )
        ).thenReturn(
                Optional.of(
                        provisioning
                )
        );

        when(
                provisioning.isCancelled()
        ).thenReturn(
                false
        );

        when(
                provisioning.isActivated()
        ).thenReturn(
                true
        );

        AdmissionGuardianAccountProvisioningException error =
                assertThrows(
                        AdmissionGuardianAccountProvisioningException.class,
                        () ->
                                service.completeParentAccountActivation(
                                        provisioningId,
                                        command()
                                )
                );

        assertEquals(
                "Guardian account provisioning is already activated",
                error.getMessage()
        );

        verify(
                memberships,
                never()
        ).acceptWithEvidence(
                any(
                        AcceptInvitationCommand.class
                )
        );
    }

    @Test
    void provisioningLookupIsRestrictedToActiveTenant() {

        UUID tenantId =
                UUID.randomUUID();

        UUID provisioningId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        when(
                provisionings.findByTenantIdAndId(
                        tenantId,
                        provisioningId
                )
        ).thenReturn(
                Optional.empty()
        );

        AdmissionGuardianAccountProvisioningException error =
                assertThrows(
                        AdmissionGuardianAccountProvisioningException.class,
                        () ->
                                service.completeParentAccountActivation(
                                        provisioningId,
                                        command()
                                )
                );

        assertEquals(
                "Guardian account provisioning was not found for the active tenant",
                error.getMessage()
        );

        verify(
                provisionings
        ).findByTenantIdAndId(
                tenantId,
                provisioningId
        );

        verify(
                memberships,
                never()
        ).acceptWithEvidence(
                any(
                        AcceptInvitationCommand.class
                )
        );
    }

    private void preparePendingSecureProvisioning(
            UUID tenantId,
            UUID provisioningId,
            UUID invitationId,
            AdmissionGuardianAccountProvisioning provisioning
    ) {

        when(
                provisionings.findByTenantIdAndId(
                        tenantId,
                        provisioningId
                )
        ).thenReturn(
                Optional.of(
                        provisioning
                )
        );

        when(
                provisioning.isCancelled()
        ).thenReturn(
                false
        );

        when(
                provisioning.isActivated()
        ).thenReturn(
                false
        );

        when(
                provisioning.isPendingActivation()
        ).thenReturn(
                true
        );

        when(
                provisioning.getProvisioningMethod()
        ).thenReturn(
                AdmissionGuardianProvisioningMethod.SECURE_INVITATION
        );

        when(
                provisioning.getInvitationId()
        ).thenReturn(
                invitationId
        );
    }

    private static AcceptInvitationCommand command() {

        return new AcceptInvitationCommand(
                "one-time-activation-token",
                "parent.account",
                "Parent Account",
                "strong-password-123"
        );
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
