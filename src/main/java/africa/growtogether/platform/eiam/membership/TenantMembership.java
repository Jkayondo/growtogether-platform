package africa.growtogether.platform.eiam.membership;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eiam_tenant_membership")
public class TenantMembership extends AuditedTenantEntity {
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false, length = 20)
    private MembershipStatus membershipStatus = MembershipStatus.ACTIVE;
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;
    @Column(name = "ended_at")
    private Instant endedAt;

    protected TenantMembership() {}
    public TenantMembership(UUID userId, Instant joinedAt) {
        this.userId = userId;
        this.joinedAt = joinedAt;
    }
    public void changeStatus(MembershipStatus target, Instant now) {
        if (membershipStatus == MembershipStatus.REMOVED && target != MembershipStatus.REMOVED) {
            throw new MembershipException("A removed membership cannot be reactivated.");
        }
        membershipStatus = target;
        endedAt = target == MembershipStatus.REMOVED ? now : null;
    }
    public UUID getUserId() { return userId; }
    public MembershipStatus getMembershipStatus() { return membershipStatus; }
    public Instant getJoinedAt() { return joinedAt; }
    public Instant getEndedAt() { return endedAt; }
}
