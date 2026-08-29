package africa.growtogether.platform.eiam.membership;

import africa.growtogether.platform.common.events.EventPublisher;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.audit.AuditEventService;
import africa.growtogether.platform.eiam.role.Role;
import africa.growtogether.platform.eiam.role.RoleNotFoundException;
import africa.growtogether.platform.eiam.role.RoleRepository;
import africa.growtogether.platform.eiam.role.UserRoleRepository;
import africa.growtogether.platform.eiam.user.UserAccountRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipRoleCodeInvitationTest {

    @Mock
    private OrganizationInvitationRepository invitations;

    @Mock
    private InvitationRoleRepository invitationRoles;

    @Mock
    private TenantMembershipRepository memberships;

    @Mock
    private UserAccountRepository users;

    @Mock
    private RoleRepository roles;

    @Mock
    private UserRoleRepository userRoles;

    @Mock
    private PasswordService passwords;

    @Mock
    private EnterpriseIdentityContext identity;

    @Mock
    private AuditEventService audit;

    @Mock
    private EventPublisher eventPublisher;

    private MembershipService service;

    @BeforeEach
    void setUp() {

        service =
                spy(
                        new MembershipService(
                                invitations,
                                invitationRoles,
                                memberships,
                                users,
                                roles,
                                userRoles,
                                passwords,
                                identity,
                                audit,

                        eventPublisher
                        )
                );
    }

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void resolvesParentRoleWithinActiveTenantAndDelegatesToExistingInvitationFlow() {

        UUID tenantId =
                UUID.randomUUID();

        UUID parentRoleId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        Role parentRole =
                mock(
                        Role.class
                );

        when(
                parentRole.getId()
        ).thenReturn(
                parentRoleId
        );

        when(
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        "PARENT"
                )
        ).thenReturn(
                Optional.of(
                        parentRole
                )
        );

        InvitationView expected =
                mock(
                        InvitationView.class
                );

        doReturn(
                expected
        ).when(
                service
        ).createInvitation(
                any(
                        CreateInvitationCommand.class
                )
        );

        Instant expiry =
                Instant.now().plusSeconds(
                        3600
                );

        InvitationView result =
                service.createInvitationByRoleCodes(
                        new CreateRoleCodeInvitationCommand(
                                null,
                                "+256701234567",
                                Set.of(
                                        "PARENT"
                                ),
                                expiry
                        )
                );

        assertSame(
                expected,
                result
        );

        verify(
                roles
        ).findByTenantIdAndCodeIgnoreCase(
                tenantId,
                "PARENT"
        );

        ArgumentCaptor<CreateInvitationCommand> commandCaptor =
                ArgumentCaptor.forClass(
                        CreateInvitationCommand.class
                );

        verify(
                service
        ).createInvitation(
                commandCaptor.capture()
        );

        CreateInvitationCommand delegated =
                commandCaptor.getValue();

        assertEquals(
                null,
                delegated.email()
        );

        assertEquals(
                "+256701234567",
                delegated.phoneNumber()
        );

        assertEquals(
                Set.of(
                        parentRoleId
                ),
                delegated.roleIds()
        );

        assertEquals(
                expiry,
                delegated.expiresAt()
        );
    }

    @Test
    void missingParentRoleFailsBeforeInvitationCreation() {

        UUID tenantId =
                UUID.randomUUID();

        setTenant(
                tenantId
        );

        when(
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        "PARENT"
                )
        ).thenReturn(
                Optional.empty()
        );

        RoleNotFoundException error =
                assertThrows(
                        RoleNotFoundException.class,
                        () ->
                                service.createInvitationByRoleCodes(
                                        new CreateRoleCodeInvitationCommand(
                                                "parent@example.com",
                                                null,
                                                Set.of(
                                                        "PARENT"
                                                ),
                                                null
                                        )
                                )
                );

        assertEquals(
                "Role was not found.",
                error.getMessage()
        );

        verify(
                service,
                never()
        ).createInvitation(
                any(
                        CreateInvitationCommand.class
                )
        );
    }

    private static void setTenant(
            UUID tenantId
    ) {

        RequestContextHolder.set(
                new RequestContext(
                        "test",
                        tenantId.toString()
                )
        );
    }
}
