package africa.growtogether.platform.eiam.permission;

import africa.growtogether.platform.eiam.role.Role;
import africa.growtogether.platform.eiam.role.RoleRepository;
import africa.growtogether.platform.eiam.role.UserRole;
import africa.growtogether.platform.eiam.role.UserRoleRepository;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EffectiveAuthorityService {
    private final UserRoleRepository userRoles; private final RoleRepository roles;
    private final RolePermissionRepository rolePermissions; private final PermissionRepository permissions;
    public EffectiveAuthorityService(UserRoleRepository userRoles, RoleRepository roles, RolePermissionRepository rolePermissions, PermissionRepository permissions) {
        this.userRoles = userRoles; this.roles = roles; this.rolePermissions = rolePermissions; this.permissions = permissions;
    }
    @Transactional(readOnly = true)
    public EffectiveAuthorities resolve(UUID tenantId, UUID userId) {
        Set<UUID> roleIds = userRoles.findAllByTenantIdAndUserId(tenantId, userId).stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        Set<String> roleCodes = roles.findAllById(roleIds).stream().filter(r -> tenantId.equals(r.getTenantId())).map(Role::getCode).collect(Collectors.toUnmodifiableSet());
        Set<UUID> permissionIds = rolePermissions.findAllByTenantIdAndRoleIdIn(tenantId, roleIds).stream().map(RolePermission::getPermissionId).collect(Collectors.toSet());
        Set<String> permissionCodes = permissions.findAllByTenantIdAndIdIn(tenantId, permissionIds).stream().filter(Permission::isAssignable).map(Permission::getCode).collect(Collectors.toUnmodifiableSet());
        return new EffectiveAuthorities(roleCodes, permissionCodes);
    }
    public record EffectiveAuthorities(Set<String> roles, Set<String> permissions) {}
}
