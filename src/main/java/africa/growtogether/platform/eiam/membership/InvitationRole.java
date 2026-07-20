package africa.growtogether.platform.eiam.membership;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "eiam_invitation_role")
public class InvitationRole extends AuditedTenantEntity {
    @Column(name = "invitation_id", nullable = false, updatable = false)
    private UUID invitationId;
    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    protected InvitationRole() {}
    public InvitationRole(UUID invitationId, UUID roleId) {
        this.invitationId = invitationId;
        this.roleId = roleId;
    }
    public UUID getInvitationId() { return invitationId; }
    public UUID getRoleId() { return roleId; }
}
