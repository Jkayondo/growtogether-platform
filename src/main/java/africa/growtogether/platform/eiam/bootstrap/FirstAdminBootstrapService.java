package africa.growtogether.platform.eiam.bootstrap;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.audit.AuditEventCategory;
import africa.growtogether.platform.eiam.audit.AuditEventService;
import africa.growtogether.platform.eiam.audit.AuditOutcome;
import africa.growtogether.platform.eiam.audit.RecordAuditEventCommand;
import africa.growtogether.platform.eiam.audit.SecuritySeverity;
import africa.growtogether.platform.eiam.role.Role;
import africa.growtogether.platform.eiam.role.RoleRepository;
import africa.growtogether.platform.eiam.role.UserRole;
import africa.growtogether.platform.eiam.role.UserRoleRepository;
import africa.growtogether.platform.eiam.tenant.Tenant;
import africa.growtogether.platform.eiam.tenant.TenantRepository;
import africa.growtogether.platform.eiam.tenant.TenantStatus;
import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FirstAdminBootstrapService {

    private static final String TENANT_ADMIN = "TENANT_ADMIN";
    private static final String UNAVAILABLE =
            "First administrator bootstrap is unavailable.";

    private final FirstAdminBootstrapProperties properties;
    private final TenantRepository tenants;
    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PasswordService passwords;
    private final AuditEventService audit;

    public FirstAdminBootstrapService(
            FirstAdminBootstrapProperties properties,
            TenantRepository tenants,
            UserAccountRepository users,
            RoleRepository roles,
            UserRoleRepository userRoles,
            PasswordService passwords,
            AuditEventService audit
    ) {
        this.properties = properties;
        this.tenants = tenants;
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.passwords = passwords;
        this.audit = audit;
    }

    @Transactional
    public FirstAdminBootstrapView bootstrap(
            String rawBootstrapToken,
            FirstAdminBootstrapCommand command
    ) {
        UUID tenantId = activeTenant();

        requireEnabledFor(tenantId);
        requireValidBootstrapToken(rawBootstrapToken);

        Tenant tenant = tenants.findByIdForUpdate(tenantId)
                .orElseThrow(FirstAdminBootstrapService::unavailable);

        if (TenantStatus.ACTIVE.equals(tenant.getStatus()) == false) {
            throw unavailable();
        }

        if (users.countByTenantId(tenantId) != 0L) {
            throw unavailable();
        }

        Role tenantAdmin = roles
                .findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        TENANT_ADMIN
                )
                .orElseThrow(FirstAdminBootstrapService::unavailable);

        if (EntityStatus.ACTIVE.equals(tenantAdmin.getStatus()) == false) {
            throw unavailable();
        }

        UserAccount administrator =
                new UserAccount(
                        command.administratorUsername(),
                        command.administratorEmail(),
                        command.administratorDisplayName(),
                        passwords.hash(
                                command.administratorPassword()
                        )
                );

        administrator.setTenantId(tenantId);
        administrator.activate();
        users.saveAndFlush(administrator);

        UserRole assignment =
                new UserRole(
                        administrator.getId(),
                        tenantAdmin.getId()
                );

        assignment.setTenantId(tenantId);
        userRoles.saveAndFlush(assignment);

        audit.record(
                new RecordAuditEventCommand(
                        "FIRST_ADMIN_BOOTSTRAP",
                        AuditEventCategory.TENANT_ADMINISTRATION,
                        AuditOutcome.SUCCESS,
                        SecuritySeverity.HIGH,
                        "UserAccount",
                        administrator.getId().toString(),
                        "Initial tenant administrator created through controlled one-time bootstrap.",
                        Map.of(
                                "role",
                                TENANT_ADMIN,
                                "tenantCode",
                                tenant.getCode()
                        )
                )
        );

        return new FirstAdminBootstrapView(
                tenantId,
                administrator.getId(),
                tenantAdmin.getId()
        );
    }

    private void requireEnabledFor(UUID requestTenantId) {
        if (properties.enabled() == false) {
            throw unavailable();
        }

        String configured = properties.tenantId();

        if (configured == null || configured.isBlank()) {
            throw unavailable();
        }

        UUID configuredTenant;

        try {
            configuredTenant =
                    UUID.fromString(
                            configured.trim()
                    );
        } catch (IllegalArgumentException exception) {
            throw unavailable();
        }

        if (configuredTenant.equals(requestTenantId) == false) {
            throw unavailable();
        }
    }

    private void requireValidBootstrapToken(String rawBootstrapToken) {
        String encoded = properties.tokenHash();

        if (rawBootstrapToken == null
                || rawBootstrapToken.isBlank()
                || encoded == null
                || encoded.isBlank()) {
            throw unavailable();
        }

        if (passwords.matches(rawBootstrapToken, encoded) == false) {
            throw unavailable();
        }
    }

    private static UUID activeTenant() {
        String tenantId =
                RequestContextHolder.current()
                        .map(context -> context.tenantId())
                        .filter(value ->
                                value != null
                                        && value.isBlank() == false
                        )
                        .orElseThrow(
                                FirstAdminBootstrapService::unavailable
                        );

        try {
            return UUID.fromString(tenantId);
        } catch (IllegalArgumentException exception) {
            throw unavailable();
        }
    }

    private static AccessDeniedException unavailable() {
        return new AccessDeniedException(UNAVAILABLE);
    }
}
