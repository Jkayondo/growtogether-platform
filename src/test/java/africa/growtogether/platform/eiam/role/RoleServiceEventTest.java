package africa.growtogether.platform.eiam.role;

import africa.growtogether.platform.common.events.EventPublisher;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.eiam.role.events.UserRolesChangedEvent;
import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceEventTest {

    @Mock
    private RoleRepository roles;

    @Mock
    private UserRoleRepository userRoles;

    @Mock
    private UserAccountRepository users;

    @Mock
    private EventPublisher eventPublisher;

    private RoleService service;

    private UUID tenantId;
    private UUID userId;


    @BeforeEach
    void setUp() {

        service =
                new RoleService(
                        roles,
                        userRoles,
                        users,
                        eventPublisher
                );

        tenantId =
                UUID.randomUUID();

        userId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "role-service-event-test",
                        tenantId.toString()
                )
        );
    }


    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }


    @Test
    void replaceUserRolesPublishesTenantSafeRoleChangeEvent() {

        UUID roleId =
                UUID.randomUUID();

        UserAccount user =
                mock(UserAccount.class);

        Role role =
                mock(Role.class);

        when(
                users.findByIdAndTenantId(
                        userId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                roles.findByIdAndTenantId(
                        roleId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(role)
        );

        when(
                role.getId()
        ).thenReturn(
                roleId
        );


        service.replaceUserRoles(
                userId,
                new ReplaceUserRolesCommand(
                        Set.of(roleId)
                )
        );


        verify(
                userRoles
        ).deleteAllByTenantIdAndUserId(
                tenantId,
                userId
        );

        verify(
                userRoles
        ).save(
                any(UserRole.class)
        );

        verify(
                userRoles
        ).flush();


        ArgumentCaptor<UserRolesChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        UserRolesChangedEvent.class
                );

        verify(
                eventPublisher
        ).publish(
                eventCaptor.capture()
        );

        UserRolesChangedEvent event =
                eventCaptor.getValue();

        assertEquals(
                tenantId,
                event.tenantId()
        );

        assertEquals(
                userId,
                event.userId()
        );

        assertNotNull(
                event.eventId()
        );

        assertNotNull(
                event.occurredAt()
        );
    }


    @Test
    void removeUserRolePublishesTenantSafeRoleChangeEvent() {

        UUID roleId =
                UUID.randomUUID();

        UserAccount user =
                mock(UserAccount.class);

        Role role =
                mock(Role.class);

        when(
                users.findByIdAndTenantId(
                        userId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                roles.findByIdAndTenantId(
                        roleId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(role)
        );


        service.removeUserRole(
                userId,
                roleId
        );


        verify(
                userRoles
        ).deleteByTenantIdAndUserIdAndRoleId(
                tenantId,
                userId,
                roleId
        );

        verify(
                userRoles
        ).flush();


        ArgumentCaptor<UserRolesChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        UserRolesChangedEvent.class
                );

        verify(
                eventPublisher
        ).publish(
                eventCaptor.capture()
        );

        UserRolesChangedEvent event =
                eventCaptor.getValue();

        assertEquals(
                tenantId,
                event.tenantId()
        );

        assertEquals(
                userId,
                event.userId()
        );

        assertNotNull(
                event.eventId()
        );

        assertNotNull(
                event.occurredAt()
        );
    }
}
