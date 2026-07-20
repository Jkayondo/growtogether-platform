package africa.growtogether.platform.eiam.permission;

import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.role.Role;
import africa.growtogether.platform.eiam.role.RoleNotFoundException;
import africa.growtogether.platform.eiam.role.RoleRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {
    private final PermissionRepository permissions;
    private final RolePermissionRepository rolePermissions;
    private final RoleRepository roles;

    public PermissionService(PermissionRepository permissions, RolePermissionRepository rolePermissions, RoleRepository roles) {
        this.permissions = permissions; this.rolePermissions = rolePermissions; this.roles = roles;
    }

    @Transactional
    public PermissionView create(CreatePermissionCommand command) {
        UUID tenant = activeTenant(); assertUnique(tenant, null, command.code());
        return PermissionView.from(permissions.saveAndFlush(new Permission(command.code(), command.name(), command.module(), command.description(), command.systemPermission())));
    }

    @Transactional(readOnly = true)
    public List<PermissionView> list() { return permissions.findAllByTenantIdOrderByModuleAscCodeAsc(activeTenant()).stream().map(PermissionView::from).toList(); }

    @Transactional(readOnly = true)
    public PermissionView get(UUID id) { return PermissionView.from(requiredPermission(id, activeTenant())); }

    @Transactional
    public PermissionView update(UUID id, UpdatePermissionCommand command) {
        UUID tenant = activeTenant(); Permission permission = requiredPermission(id, tenant);
        assertUnique(tenant, id, command.code());
        permission.update(command.code(), command.name(), command.module(), command.description());
        return PermissionView.from(permissions.saveAndFlush(permission));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenant = activeTenant(); Permission permission = requiredPermission(id, tenant);
        if (permission.isSystemPermission()) throw new PermissionLifecycleException("System permissions cannot be deleted.");
        if (rolePermissions.countByTenantIdAndPermissionId(tenant, id) > 0) throw new PermissionLifecycleException("Assigned permissions must be unassigned before deletion.");
        permissions.delete(permission);
    }

    @Transactional
    public List<PermissionView> replaceRolePermissions(UUID roleId, ReplaceRolePermissionsCommand command) {
        UUID tenant = activeTenant(); requiredRole(roleId, tenant);
        Set<UUID> requested = new LinkedHashSet<>(command.permissionIds());
        List<Permission> selected = requested.stream().map(id -> requiredPermission(id, tenant)).toList();
        selected.stream().filter(p -> !p.isAssignable()).findFirst().ifPresent(p -> { throw new PermissionLifecycleException("Inactive permissions cannot be assigned."); });
        rolePermissions.deleteAllByTenantIdAndRoleId(tenant, roleId);
        selected.forEach(permission -> rolePermissions.save(new RolePermission(roleId, permission.getId())));
        rolePermissions.flush();
        return selected.stream().map(PermissionView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionView> rolePermissions(UUID roleId) {
        UUID tenant = activeTenant(); requiredRole(roleId, tenant);
        Set<UUID> ids = rolePermissions.findAllByTenantIdAndRoleId(tenant, roleId).stream().map(RolePermission::getPermissionId).collect(java.util.stream.Collectors.toSet());
        return permissions.findAllByTenantIdAndIdIn(tenant, ids).stream().map(PermissionView::from).toList();
    }

    @Transactional
    public void removeRolePermission(UUID roleId, UUID permissionId) {
        UUID tenant = activeTenant(); requiredRole(roleId, tenant); requiredPermission(permissionId, tenant);
        rolePermissions.deleteByTenantIdAndRoleIdAndPermissionId(tenant, roleId, permissionId);
    }

    private void assertUnique(UUID tenant, UUID currentId, String code) {
        permissions.findByTenantIdAndCodeIgnoreCase(tenant, code.trim()).filter(p -> !p.getId().equals(currentId))
            .ifPresent(p -> { throw new DuplicatePermissionException("Permission code is already in use for this tenant."); });
    }
    private Permission requiredPermission(UUID id, UUID tenant) { return permissions.findByIdAndTenantId(id, tenant).orElseThrow(PermissionNotFoundException::new); }
    private Role requiredRole(UUID id, UUID tenant) { return roles.findByIdAndTenantId(id, tenant).orElseThrow(RoleNotFoundException::new); }
    private static UUID activeTenant() {
        return RequestContextHolder.current().map(c -> c.tenantId()).filter(v -> v != null && !v.isBlank()).map(UUID::fromString)
            .orElseThrow(() -> new IllegalStateException("An active tenant is required."));
    }
}
