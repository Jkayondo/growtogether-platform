package africa.growtogether.platform.eiam.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContext;
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
import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class FirstAdminBootstrapServiceTest {

    private static final String RAW_BOOTSTRAP_TOKEN =
            "controlled-bootstrap-secret-2026";

    private static final String ADMIN_PASSWORD =
            "Strong-Administrator-Password-2026";

    private final TenantRepository tenants =
            mock(TenantRepository.class);

    private final UserAccountRepository users =
            mock(UserAccountRepository.class);

    private final RoleRepository roles =
            mock(RoleRepository.class);

    private final UserRoleRepository userRoles =
            mock(UserRoleRepository.class);

    private final AuditEventService audit =
            mock(AuditEventService.class);

    private final PasswordService passwords =
            new PasswordService(
                    new BCryptPasswordEncoder()
            );

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void validControlledBootstrapCreatesExactlyOneActiveTenantAdministrator()
            throws Exception {

        Tenant tenant =
                activeTenant();

        UUID tenantId =
                tenant.getId();

        UUID roleId =
                UUID.randomUUID();

        UUID administratorId =
                UUID.randomUUID();

        Role tenantAdmin =
                mock(Role.class);

        when(tenantAdmin.getId())
                .thenReturn(roleId);

        when(tenantAdmin.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        when(tenants.findByIdForUpdate(tenantId))
                .thenReturn(Optional.of(tenant));

        when(users.countByTenantId(tenantId))
                .thenReturn(0L);

        when(roles.findByTenantIdAndCodeIgnoreCase(
                tenantId,
                "TENANT_ADMIN"
        )).thenReturn(Optional.of(tenantAdmin));

        when(users.saveAndFlush(any(UserAccount.class)))
                .thenAnswer(invocation -> {
                    UserAccount user =
                            invocation.getArgument(0);

                    setEntityId(
                            user,
                            administratorId
                    );

                    return user;
                });

        when(userRoles.saveAndFlush(any(UserRole.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        FirstAdminBootstrapService service =
                service(
                        tenantId,
                        true,
                        RAW_BOOTSTRAP_TOKEN
                );

        useTenant(tenantId);

        FirstAdminBootstrapView result =
                service.bootstrap(
                        RAW_BOOTSTRAP_TOKEN,
                        command()
                );

        assertThat(result.tenantId())
                .isEqualTo(tenantId);

        assertThat(result.administratorUserId())
                .isEqualTo(administratorId);

        assertThat(result.tenantAdminRoleId())
                .isEqualTo(roleId);

        ArgumentCaptor<UserAccount> userCaptor =
                ArgumentCaptor.forClass(
                        UserAccount.class
                );

        verify(users)
                .saveAndFlush(
                        userCaptor.capture()
                );

        UserAccount created =
                userCaptor.getValue();

        assertThat(created.getTenantId())
                .isEqualTo(tenantId);

        assertThat(created.canAuthenticateAt(
                java.time.Instant.now()
        )).isTrue();

        assertThat(
                passwords.matches(
                        ADMIN_PASSWORD,
                        passwordHash(created)
                )
        ).isTrue();

        ArgumentCaptor<UserRole> roleCaptor =
                ArgumentCaptor.forClass(
                        UserRole.class
                );

        verify(userRoles)
                .saveAndFlush(
                        roleCaptor.capture()
                );

        assertThat(roleCaptor.getValue().getTenantId())
                .isEqualTo(tenantId);

        assertThat(roleCaptor.getValue().getUserId())
                .isEqualTo(administratorId);

        assertThat(roleCaptor.getValue().getRoleId())
                .isEqualTo(roleId);

        ArgumentCaptor<RecordAuditEventCommand> auditCaptor =
                ArgumentCaptor.forClass(
                        RecordAuditEventCommand.class
                );

        verify(audit)
                .record(
                        auditCaptor.capture()
                );

        RecordAuditEventCommand recorded =
                auditCaptor.getValue();

        assertThat(recorded.eventType())
                .isEqualTo("FIRST_ADMIN_BOOTSTRAP");

        assertThat(recorded.category())
                .isEqualTo(
                        AuditEventCategory.TENANT_ADMINISTRATION
                );

        assertThat(recorded.outcome())
                .isEqualTo(
                        AuditOutcome.SUCCESS
                );

        assertThat(recorded.severity())
                .isEqualTo(
                        SecuritySeverity.HIGH
                );
    }

    @Test
    void bootstrapIsRejectedWhenCapabilityIsDisabled() {

        Tenant tenant =
                activeTenant();

        UUID tenantId =
                tenant.getId();

        useTenant(tenantId);

        FirstAdminBootstrapService service =
                service(
                        tenantId,
                        false,
                        RAW_BOOTSTRAP_TOKEN
                );

        assertUnavailable(
                () -> service.bootstrap(
                        RAW_BOOTSTRAP_TOKEN,
                        command()
                )
        );

        verifyNoInteractions(
                tenants,
                users,
                roles,
                userRoles,
                audit
        );
    }

    @Test
    void bootstrapIsRejectedForWrongTokenBeforeTenantMutation() {

        Tenant tenant =
                activeTenant();

        UUID tenantId =
                tenant.getId();

        useTenant(tenantId);

        FirstAdminBootstrapService service =
                service(
                        tenantId,
                        true,
                        RAW_BOOTSTRAP_TOKEN
                );

        assertUnavailable(
                () -> service.bootstrap(
                        "incorrect-bootstrap-secret",
                        command()
                )
        );

        verifyNoInteractions(
                tenants,
                users,
                roles,
                userRoles,
                audit
        );
    }

    @Test
    void bootstrapIsRejectedForDifferentConfiguredTenant() {

        Tenant tenant =
                activeTenant();

        UUID requestTenant =
                tenant.getId();

        UUID configuredTenant =
                UUID.randomUUID();

        useTenant(requestTenant);

        FirstAdminBootstrapService service =
                service(
                        configuredTenant,
                        true,
                        RAW_BOOTSTRAP_TOKEN
                );

        assertUnavailable(
                () -> service.bootstrap(
                        RAW_BOOTSTRAP_TOKEN,
                        command()
                )
        );

        verifyNoInteractions(
                tenants,
                users,
                roles,
                userRoles,
                audit
        );
    }

    @Test
    void bootstrapBecomesUnavailableOnceTenantHasAnyUser() {

        Tenant tenant =
                activeTenant();

        UUID tenantId =
                tenant.getId();

        when(tenants.findByIdForUpdate(tenantId))
                .thenReturn(Optional.of(tenant));

        when(users.countByTenantId(tenantId))
                .thenReturn(1L);

        useTenant(tenantId);

        FirstAdminBootstrapService service =
                service(
                        tenantId,
                        true,
                        RAW_BOOTSTRAP_TOKEN
                );

        assertUnavailable(
                () -> service.bootstrap(
                        RAW_BOOTSTRAP_TOKEN,
                        command()
                )
        );

        verify(tenants)
                .findByIdForUpdate(tenantId);

        verify(users)
                .countByTenantId(tenantId);

        verifyNoInteractions(
                roles,
                userRoles,
                audit
        );

        verify(users, never())
                .saveAndFlush(
                        any(UserAccount.class)
                );
    }

    private FirstAdminBootstrapService service(
            UUID tenantId,
            boolean enabled,
            String rawBootstrapToken
    ) {

        FirstAdminBootstrapProperties properties =
                new FirstAdminBootstrapProperties(
                        enabled,
                        tenantId.toString(),
                        passwords.hash(
                                rawBootstrapToken
                        )
                );

        return new FirstAdminBootstrapService(
                properties,
                tenants,
                users,
                roles,
                userRoles,
                passwords,
                audit
        );
    }

    private static Tenant activeTenant() {

        Tenant tenant =
                new Tenant(
                        UUID.randomUUID(),
                        "GT-SCHOOL",
                        "GT School"
                );

        tenant.activate();

        return tenant;
    }

    private static FirstAdminBootstrapCommand command() {
        return new FirstAdminBootstrapCommand(
                "pilot-admin@growtogether.africa",
                "pilot-admin",
                ADMIN_PASSWORD,
                "Pilot Tenant Administrator"
        );
    }

    private static void useTenant(
            UUID tenantId
    ) {
        RequestContextHolder.set(
                new RequestContext(
                        "first-admin-bootstrap-test",
                        tenantId.toString()
                )
        );
    }

    private static void assertUnavailable(
            Runnable action
    ) {
        assertThatThrownBy(
                action::run
        )
                .isInstanceOf(
                        AccessDeniedException.class
                )
                .hasMessage(
                        "First administrator bootstrap is unavailable."
                );
    }

    private static void setEntityId(
            AuditedTenantEntity entity,
            UUID id
    ) throws Exception {

        Field field =
                AuditedTenantEntity.class
                        .getDeclaredField(
                                "id"
                        );

        field.setAccessible(true);
        field.set(
                entity,
                id
        );
    }

    private static String passwordHash(
            UserAccount user
    ) throws Exception {

        Field field =
                UserAccount.class
                        .getDeclaredField(
                                "passwordHash"
                        );

        field.setAccessible(true);

        return (String) field.get(user);
    }
}
