package africa.growtogether.platform.eiam.permission;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    List<RolePermission> findAllByTenantIdAndRoleId(UUID tenantId, UUID roleId);
    List<RolePermission> findAllByTenantIdAndRoleIdIn(UUID tenantId, Collection<UUID> roleIds);
    long countByTenantIdAndPermissionId(UUID tenantId, UUID permissionId);
    void deleteAllByTenantIdAndRoleId(UUID tenantId, UUID roleId);
    void deleteByTenantIdAndRoleIdAndPermissionId(UUID tenantId, UUID roleId, UUID permissionId);
}
