package africa.growtogether.platform.school.admission;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/*
 * Non-secret evidence returned after successful admission-stage
 * parent account activation.
 *
 * No invitation token or password is retained here.
 */
public record AdmissionGuardianAccountActivationResult(
        UUID provisioningId,
        UUID admissionGuardianId,
        UUID invitationId,
        UUID membershipId,
        UUID eiamUserId,
        AdmissionGuardianProvisioningStatus provisioningStatus,
        Instant activatedAt
) {

    public AdmissionGuardianAccountActivationResult {

        Objects.requireNonNull(
                provisioningId,
                "provisioningId must not be null"
        );

        Objects.requireNonNull(
                admissionGuardianId,
                "admissionGuardianId must not be null"
        );

        Objects.requireNonNull(
                invitationId,
                "invitationId must not be null"
        );

        Objects.requireNonNull(
                membershipId,
                "membershipId must not be null"
        );

        Objects.requireNonNull(
                eiamUserId,
                "eiamUserId must not be null"
        );

        Objects.requireNonNull(
                provisioningStatus,
                "provisioningStatus must not be null"
        );

        Objects.requireNonNull(
                activatedAt,
                "activatedAt must not be null"
        );
    }
}
