package africa.growtogether.platform.eiam.permission;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "eiam_role_permission")
public class RolePermission extends AuditedTenantEntity {
    @Column(name = "role_id", nullable = false)
    private UUID roleId;
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    protected RolePermission() {}
    public RolePermission(UUID roleId, UUID permissionId) { this.roleId = roleId; this.permissionId = permissionId; }
    public UUID getRoleId() { return roleId; }
    public UUID getPermissionId() { return permissionId; }
}
