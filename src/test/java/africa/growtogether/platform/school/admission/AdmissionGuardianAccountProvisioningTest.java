package africa.growtogether.platform.school.admission;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdmissionGuardianAccountProvisioningTest {

    @Test
    void secureInvitationStartsPendingAndNormalizesEmail() {

        UUID guardianId =
                UUID.randomUUID();

        UUID invitationId =
                UUID.randomUUID();

        AdmissionGuardianAccountProvisioning provisioning =
                AdmissionGuardianAccountProvisioning.secureInvitation(
                        guardianId,
                        AdmissionGuardianContactIdentityType.EMAIL,
                        " Parent@Example.COM ",
                        invitationId
                );

        assertEquals(
                guardianId,
                provisioning.getAdmissionGuardianId()
        );

        assertEquals(
                AdmissionGuardianProvisioningMethod.SECURE_INVITATION,
                provisioning.getProvisioningMethod()
        );

        assertEquals(
                AdmissionGuardianProvisioningStatus.PENDING_ACTIVATION,
                provisioning.getProvisioningStatus()
        );

        assertEquals(
                AdmissionGuardianContactIdentityType.EMAIL,
                provisioning.getContactIdentityType()
        );

        assertEquals(
                "parent@example.com",
                provisioning.getContactIdentity()
        );

        assertEquals(
                invitationId,
                provisioning.getInvitationId()
        );

        assertNull(
                provisioning.getEiamUserId()
        );

        assertNull(
                provisioning.getActivatedAt()
        );

        assertNull(
                provisioning.getCancelledAt()
        );

        assertTrue(
                provisioning.isPendingActivation()
        );

        assertFalse(
                provisioning.isActivated()
        );

        assertFalse(
                provisioning.isCancelled()
        );
    }

    @Test
    void secureInvitationRejectsNonCanonicalPhoneIdentity() {

        UUID guardianId =
                UUID.randomUUID();

        UUID invitationId =
                UUID.randomUUID();

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                AdmissionGuardianAccountProvisioning
                                        .secureInvitation(
                                                guardianId,
                                                AdmissionGuardianContactIdentityType.PHONE,
                                                "0701234567",
                                                invitationId
                                        )
                );

        assertEquals(
                "PHONE contact identity must use canonical international format",
                error.getMessage()
        );
    }

    @Test
    void existingIdentityReuseStartsActivatedWithEvidence() {

        UUID guardianId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        Instant activatedAt =
                Instant.parse(
                        "2026-08-23T12:00:00Z"
                );

        AdmissionGuardianAccountProvisioning provisioning =
                AdmissionGuardianAccountProvisioning.existingIdentityReuse(
                        guardianId,
                        userId,
                        activatedAt
                );

        assertEquals(
                AdmissionGuardianProvisioningMethod.EXISTING_IDENTITY_REUSE,
                provisioning.getProvisioningMethod()
        );

        assertEquals(
                AdmissionGuardianProvisioningStatus.ACTIVATED,
                provisioning.getProvisioningStatus()
        );

        assertEquals(
                userId,
                provisioning.getEiamUserId()
        );

        assertEquals(
                activatedAt,
                provisioning.getActivatedAt()
        );

        assertNull(
                provisioning.getInvitationId()
        );

        assertNull(
                provisioning.getContactIdentityType()
        );

        assertNull(
                provisioning.getContactIdentity()
        );

        assertTrue(
                provisioning.isActivated()
        );
    }

    @Test
    void pendingSecureInvitationActivatesAndSameUserRetryIsIdempotent() {

        AdmissionGuardianAccountProvisioning provisioning =
                secureInvitation();

        UUID userId =
                UUID.randomUUID();

        Instant firstActivation =
                Instant.parse(
                        "2026-08-23T12:10:00Z"
                );

        provisioning.activate(
                userId,
                firstActivation
        );

        assertEquals(
                AdmissionGuardianProvisioningStatus.ACTIVATED,
                provisioning.getProvisioningStatus()
        );

        assertEquals(
                userId,
                provisioning.getEiamUserId()
        );

        assertEquals(
                firstActivation,
                provisioning.getActivatedAt()
        );

        assertTrue(
                provisioning.isActivated()
        );

        provisioning.activate(
                userId,
                firstActivation.plusSeconds(
                        600
                )
        );

        assertEquals(
                firstActivation,
                provisioning.getActivatedAt()
        );
    }

    @Test
    void activatedProvisioningCannotBeReassignedOrCancelled() {

        AdmissionGuardianAccountProvisioning provisioning =
                secureInvitation();

        UUID firstUserId =
                UUID.randomUUID();

        provisioning.activate(
                firstUserId,
                Instant.parse(
                        "2026-08-23T12:20:00Z"
                )
        );

        IllegalStateException reassignmentError =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                provisioning.activate(
                                        UUID.randomUUID(),
                                        Instant.parse(
                                                "2026-08-23T12:21:00Z"
                                        )
                                )
                );

        assertEquals(
                "Guardian account provisioning is already activated for another EIAM user",
                reassignmentError.getMessage()
        );

        IllegalStateException cancellationError =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                provisioning.cancel(
                                        Instant.parse(
                                                "2026-08-23T12:22:00Z"
                                        )
                                )
                );

        assertEquals(
                "Activated guardian account provisioning cannot be cancelled",
                cancellationError.getMessage()
        );
    }

    @Test
    void pendingProvisioningCanBeCancelledAndCannotLaterActivate() {

        AdmissionGuardianAccountProvisioning provisioning =
                secureInvitation();

        Instant cancelledAt =
                Instant.parse(
                        "2026-08-23T12:30:00Z"
                );

        provisioning.cancel(
                cancelledAt
        );

        assertEquals(
                AdmissionGuardianProvisioningStatus.CANCELLED,
                provisioning.getProvisioningStatus()
        );

        assertEquals(
                cancelledAt,
                provisioning.getCancelledAt()
        );

        assertTrue(
                provisioning.isCancelled()
        );

        provisioning.cancel(
                cancelledAt.plusSeconds(
                        600
                )
        );

        assertEquals(
                cancelledAt,
                provisioning.getCancelledAt()
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                provisioning.activate(
                                        UUID.randomUUID(),
                                        Instant.parse(
                                                "2026-08-23T12:31:00Z"
                                        )
                                )
                );

        assertEquals(
                "Cancelled guardian account provisioning cannot be activated",
                error.getMessage()
        );
    }

    private static AdmissionGuardianAccountProvisioning secureInvitation() {

        return AdmissionGuardianAccountProvisioning.secureInvitation(
                UUID.randomUUID(),
                AdmissionGuardianContactIdentityType.PHONE,
                "+256701234567",
                UUID.randomUUID()
        );
    }
}
