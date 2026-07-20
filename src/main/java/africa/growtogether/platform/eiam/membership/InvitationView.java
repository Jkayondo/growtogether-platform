package africa.growtogether.platform.eiam.membership;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record InvitationView(UUID id, String email, InvitationStatus status, Instant expiresAt, Set<UUID> roleIds, String acceptanceToken) {
    static InvitationView from(OrganizationInvitation invitation, Set<UUID> roleIds, String token) {
        return new InvitationView(invitation.getId(), invitation.getEmail(), invitation.getInvitationStatus(), invitation.getExpiresAt(), Set.copyOf(roleIds), token);
    }
}
