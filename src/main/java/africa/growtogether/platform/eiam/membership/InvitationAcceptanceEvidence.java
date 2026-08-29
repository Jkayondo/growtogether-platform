package africa.growtogether.platform.eiam.membership;

import java.util.Objects;
import java.util.UUID;

/*
 * Internal evidence returned by EIAM after successful invitation
 * acceptance.
 *
 * The invitation token remains secret and is not returned here.
 * Consumers receive only the durable invitation identifier and
 * resulting tenant membership.
 */
public record InvitationAcceptanceEvidence(
        UUID invitationId,
        MembershipView membership
) {

    public InvitationAcceptanceEvidence {

        Objects.requireNonNull(
                invitationId,
                "invitationId must not be null"
        );

        Objects.requireNonNull(
                membership,
                "membership must not be null"
        );
    }
}
