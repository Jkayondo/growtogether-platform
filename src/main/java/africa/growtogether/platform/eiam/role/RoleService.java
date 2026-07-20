package africa.growtogether.platform.eiam.role;

import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import africa.growtogether.platform.eiam.user.UserNotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final UserAccountRepository users;

    public RoleService(RoleRepository roles, UserRoleRepository userRoles, UserAccountRepository users) {
        this.roles = roles; this.userRoles = userRoles; this.users = users;
    }

    @Transactional
    public RoleView create(CreateRoleCommand command) {
        UUID tenant = activeTenant();
        assertUnique(tenant, null, command.code(), command.name());
        return RoleView.from(roles.saveAndFlush(new Role(command.code(), command.name(), command.description(), command.systemRole())));
    }

    @Transactional(readOnly = true)
    public List<RoleView> list() { return roles.findAllByTenantIdOrderByNameAsc(activeTenant()).stream().map(RoleView::from).toList(); }

    @Transactional(readOnly = true)
    public RoleView get(UUID id) { return RoleView.from(requiredRole(id, activeTenant())); }

    @Transactional
    public RoleView update(UUID id, UpdateRoleCommand command) {
        UUID tenant = activeTenant(); Role role = requiredRole(id, tenant);
        assertUnique(tenant, id, command.code(), command.name());
        role.update(command.code(), command.name(), command.description());
        return RoleView.from(roles.saveAndFlush(role));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenant = activeTenant(); Role role = requiredRole(id, tenant);
        if (role.isSystemRole()) throw new RoleLifecycleException("System roles cannot be deleted.");
        if (userRoles.countByTenantIdAndRoleId(tenant, id) > 0) throw new RoleLifecycleException("Assigned roles must be unassigned before deletion.");
        roles.delete(role);
    }

    @Transactional
    public List<RoleView> replaceUserRoles(UUID userId, ReplaceUserRolesCommand command) {
        UUID tenant = activeTenant(); requiredUser(userId, tenant);
        Set<UUID> requested = new LinkedHashSet<>(command.roleIds());
        List<Role> selected = requested.stream().map(id -> requiredRole(id, tenant)).toList();
        userRoles.deleteAllByTenantIdAndUserId(tenant, userId);
        selected.forEach(role -> userRoles.save(new UserRole(userId, role.getId())));
        userRoles.flush();
        return selected.stream().map(RoleView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RoleView> userRoles(UUID userId) {
        UUID tenant = activeTenant(); requiredUser(userId, tenant);
        Set<UUID> ids = userRoles.findAllByTenantIdAndUserId(tenant, userId).stream().map(UserRole::getRoleId).collect(java.util.stream.Collectors.toSet());
        return roles.findAllById(ids).stream().filter(role -> tenant.equals(role.getTenantId())).map(RoleView::from).toList();
    }

    @Transactional
    public void removeUserRole(UUID userId, UUID roleId) {
        UUID tenant = activeTenant(); requiredUser(userId, tenant); requiredRole(roleId, tenant);
        userRoles.deleteByTenantIdAndUserIdAndRoleId(tenant, userId, roleId);
    }

    private void assertUnique(UUID tenant, UUID currentId, String code, String name) {
        roles.findByTenantIdAndCodeIgnoreCase(tenant, code.trim()).filter(r -> !r.getId().equals(currentId))
            .ifPresent(r -> { throw new DuplicateRoleException("code", "Role code is already in use for this tenant."); });
        roles.findByTenantIdAndNameIgnoreCase(tenant, name.trim()).filter(r -> !r.getId().equals(currentId))
            .ifPresent(r -> { throw new DuplicateRoleException("name", "Role name is already in use for this tenant."); });
    }
    private Role requiredRole(UUID id, UUID tenant) { return roles.findByIdAndTenantId(id, tenant).orElseThrow(RoleNotFoundException::new); }
    private UserAccount requiredUser(UUID id, UUID tenant) { return users.findByIdAndTenantId(id, tenant).orElseThrow(UserNotFoundException::new); }
    private static UUID activeTenant() {
        return RequestContextHolder.current().map(c -> c.tenantId()).filter(v -> v != null && !v.isBlank()).map(UUID::fromString)
            .orElseThrow(() -> new IllegalStateException("An active tenant is required."));
    }
}
