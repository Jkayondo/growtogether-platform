package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "gt_connect_space_members",
        indexes = {
                @Index(
                        name = "ix_gt_connect_members_tenant_space",
                        columnList = "tenant_id,space_id"
                ),
                @Index(
                        name = "ix_gt_connect_members_tenant_user",
                        columnList = "tenant_id,user_id"
                )
        }
)
public class ConnectSpaceMember extends AuditedTenantEntity {

    @Column(
            name = "space_id",
            nullable = false
    )
    private UUID spaceId;

    @Column(
            name = "user_id",
            nullable = false
    )
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "member_role",
            nullable = false,
            length = 30
    )
    private ConnectMemberRole memberRole;

    @Column(
            name = "role_authority_source",
            length = 100
    )
    private String roleAuthoritySource;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "previous_member_role",
            length = 30
    )
    private ConnectMemberRole previousMemberRole;

    @Column(
            name = "joined_at",
            nullable = false
    )
    private Instant joinedAt;

    @Column(
            name = "left_at"
    )
    private Instant leftAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "membership_status",
            nullable = false,
            length = 20
    )
    private ConnectMembershipStatus membershipStatus;

    protected ConnectSpaceMember() {
    }

    public ConnectSpaceMember(
            UUID tenantId,
            UUID spaceId,
            UUID userId,
            ConnectMemberRole memberRole
    ) {
        setTenantId(required(tenantId, "tenantId"));

        this.spaceId = required(spaceId, "spaceId");
        this.userId = required(userId, "userId");

        this.memberRole =
                memberRole == null
                        ? ConnectMemberRole.MEMBER
                        : memberRole;

        this.joinedAt = Instant.now();
        this.membershipStatus = ConnectMembershipStatus.ACTIVE;
    }

    /**
     * Changes the role of an ACTIVE membership while preserving
     * membership identity and lifecycle history.
     *
     * Authorization for a particular role transition belongs to the
     * calling service. This entity only protects membership integrity.
     */
    public void changeRole(
            ConnectMemberRole targetRole
    ) {
        requireActive();

        if (targetRole == null) {
            throw new IllegalArgumentException(
                    "targetRole is required"
            );
        }

        if (roleAuthoritySource != null) {
            throw new IllegalStateException(
                    "Authoritative membership roles must be changed "
                            + "through their authority lifecycle"
            );
        }

        this.memberRole =
                targetRole;
    }

    /**
     * Applies a role controlled by an authoritative GT source.
     *
     * An existing ordinary role is preserved so it can be restored
     * when the external authority is withdrawn.
     */
    public void applyAuthoritativeRole(
            ConnectMemberRole targetRole,
            String authoritySource
    ) {
        requireActive();

        if (targetRole == null) {
            throw new IllegalArgumentException(
                    "targetRole is required"
            );
        }

        String normalizedSource =
                requiredText(
                        authoritySource,
                        "authoritySource"
                );

        if (
                roleAuthoritySource != null
                        && !roleAuthoritySource.equals(
                                normalizedSource
                        )
        ) {
            throw new IllegalStateException(
                    "Membership role is already controlled "
                            + "by another authority"
            );
        }

        if (roleAuthoritySource == null) {

            previousMemberRole =
                    memberRole == targetRole
                            ? null
                            : memberRole;

            roleAuthoritySource =
                    normalizedSource;
        }

        memberRole =
                targetRole;
    }

    /**
     * Releases a role controlled by an authoritative GT source.
     *
     * If an ordinary role existed before the authority was applied,
     * that role is restored.
     *
     * If no previous ordinary role existed, the active membership was
     * authority-derived and is therefore removed. Its authority source
     * remains on the historical row for provenance.
     */
    public void releaseAuthoritativeRole(
            String authoritySource
    ) {
        requireActive();

        String normalizedSource =
                requiredText(
                        authoritySource,
                        "authoritySource"
                );

        if (roleAuthoritySource == null) {
            return;
        }

        if (
                !roleAuthoritySource.equals(
                        normalizedSource
                )
        ) {
            throw new IllegalStateException(
                    "Membership role is controlled "
                            + "by another authority"
            );
        }

        if (previousMemberRole != null) {

            memberRole =
                    previousMemberRole;

            previousMemberRole =
                    null;

            roleAuthoritySource =
                    null;

            return;
        }

        remove();
    }

    public void leave() {
        requireActive();

        this.membershipStatus = ConnectMembershipStatus.LEFT;
        this.leftAt = Instant.now();
    }

    public void remove() {
        requireActive();

        this.membershipStatus = ConnectMembershipStatus.REMOVED;
        this.leftAt = Instant.now();
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public ConnectMemberRole getMemberRole() {
        return memberRole;
    }

    public String getRoleAuthoritySource() {
        return roleAuthoritySource;
    }

    public ConnectMemberRole getPreviousMemberRole() {
        return previousMemberRole;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public ConnectMembershipStatus getMembershipStatus() {
        return membershipStatus;
    }

    private void requireActive() {
        if (membershipStatus != ConnectMembershipStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Membership is not active"
            );
        }
    }

    private static String requiredText(
            String value,
            String name
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    name + " is required"
            );
        }

        return value.trim();
    }

    private static UUID required(UUID value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
