package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.ens.NotificationChannel;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/*
 * Non-secret evidence returned after secure parent activation
 * notification creation.
 *
 * The EIAM acceptance token is deliberately excluded.
 */
public record AdmissionGuardianActivationNotificationResult(
        UUID provisioningId,
        UUID admissionGuardianId,
        UUID invitationId,
        UUID notificationRequestId,
        NotificationChannel channel,
        Instant expiresAt
) {

    public AdmissionGuardianActivationNotificationResult {

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
                notificationRequestId,
                "notificationRequestId must not be null"
        );

        Objects.requireNonNull(
                channel,
                "channel must not be null"
        );

        Objects.requireNonNull(
                expiresAt,
                "expiresAt must not be null"
        );
    }
}
