package africa.growtogether.platform.eiam.role;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "eiam_user_role")
public class UserRole extends AuditedTenantEntity {
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    protected UserRole() {}
    public UserRole(UUID userId, UUID roleId) { this.userId = userId; this.roleId = roleId; }
    public UUID getUserId() { return userId; }
    public UUID getRoleId() { return roleId; }
}
