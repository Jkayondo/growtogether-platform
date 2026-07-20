package africa.growtogether.platform.eiam.membership;

import java.time.Instant;
import java.util.UUID;

public record MembershipView(UUID id, UUID userId, MembershipStatus status, Instant joinedAt, Instant endedAt) {
    static MembershipView from(TenantMembership membership) {
        return new MembershipView(membership.getId(), membership.getUserId(), membership.getMembershipStatus(), membership.getJoinedAt(), membership.getEndedAt());
    }
}
