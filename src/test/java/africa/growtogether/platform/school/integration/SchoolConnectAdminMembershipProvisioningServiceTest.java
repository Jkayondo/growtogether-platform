package africa.growtogether.platform.school.integration;

import africa.growtogether.platform.common.persistence.TenantScopeViolationException;

import africa.growtogether.platform.connect.ConnectMemberRole;
import africa.growtogether.platform.connect.ConnectMembershipStatus;
import africa.growtogether.platform.connect.ConnectSpace;
import africa.growtogether.platform.connect.ConnectSpaceMember;
import africa.growtogether.platform.connect.ConnectSpaceMemberRepository;

import africa.growtogether.platform.eiam.role.Role;
import africa.growtogether.platform.eiam.role.RoleRepository;
import africa.growtogether.platform.eiam.role.UserRole;
import africa.growtogether.platform.eiam.role.UserRoleRepository;

import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import africa.growtogether.platform.eiam.user.UserAccountStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolConnectAdminMembershipProvisioningServiceTest {

    @Mock
    private RoleRepository roles;

    @Mock
    private UserRoleRepository userRoles;

    @Mock
    private UserAccountRepository users;

    @Mock
    private ConnectSpaceMemberRepository members;

    private SchoolConnectAdminMembershipProvisioningService service;


    @BeforeEach
    void setUp() {

        service =
                new SchoolConnectAdminMembershipProvisioningService(
                        roles,
                        userRoles,
                        users,
                        members
                );
    }


    @Test
    void activeSchoolAdministratorBecomesConnectAdminMember() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID roleId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        ConnectSpace institutionSpace =
                mock(ConnectSpace.class);

        Role schoolAdminRole =
                mock(Role.class);

        UserRole assignment =
                mock(UserRole.class);

        UserAccount user =
                mock(UserAccount.class);


        when(
                institutionSpace.getTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                institutionSpace.getId()
        ).thenReturn(
                spaceId
        );

        when(
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        "SCHOOL_ADMIN"
                )
        ).thenReturn(
                Optional.of(
                        schoolAdminRole
                )
        );

        when(
                schoolAdminRole.getId()
        ).thenReturn(
                roleId
        );

        when(
                userRoles.findAllByTenantIdAndRoleId(
                        tenantId,
                        roleId
                )
        ).thenReturn(
                List.of(
                        assignment
                )
        );

        when(
                assignment.getUserId()
        ).thenReturn(
                userId
        );

        when(
                users.findByIdAndTenantId(
                        userId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.getAccountStatus()
        ).thenReturn(
                UserAccountStatus.ACTIVE
        );

        when(
                user.getId()
        ).thenReturn(
                userId
        );

        when(
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                userId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.empty()
        );


        int created =
                service.ensureSchoolAdministrators(
                        tenantId,
                        institutionSpace
                );


        assertEquals(
                1,
                created
        );

        ArgumentCaptor<ConnectSpaceMember> captured =
                ArgumentCaptor.forClass(
                        ConnectSpaceMember.class
                );

        verify(
                members
        ).save(
                captured.capture()
        );

        ConnectSpaceMember membership =
                captured.getValue();

        assertEquals(
                tenantId,
                membership.getTenantId()
        );

        assertEquals(
                spaceId,
                membership.getSpaceId()
        );

        assertEquals(
                userId,
                membership.getUserId()
        );

        assertEquals(
                ConnectMemberRole.ADMIN,
                membership.getMemberRole()
        );

        assertEquals(
                ConnectMembershipStatus.ACTIVE,
                membership.getMembershipStatus()
        );
    }


    @Test
    void existingMemberIsPromotedWithoutCreatingDuplicateMembership() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID roleId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        ConnectSpace institutionSpace =
                mock(ConnectSpace.class);

        Role schoolAdminRole =
                mock(Role.class);

        UserRole assignment =
                mock(UserRole.class);

        UserAccount user =
                mock(UserAccount.class);

        ConnectSpaceMember existingMembership =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        userId,
                        ConnectMemberRole.MEMBER
                );


        when(
                institutionSpace.getTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                institutionSpace.getId()
        ).thenReturn(
                spaceId
        );

        when(
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        "SCHOOL_ADMIN"
                )
        ).thenReturn(
                Optional.of(
                        schoolAdminRole
                )
        );

        when(
                schoolAdminRole.getId()
        ).thenReturn(
                roleId
        );

        when(
                userRoles.findAllByTenantIdAndRoleId(
                        tenantId,
                        roleId
                )
        ).thenReturn(
                List.of(
                        assignment
                )
        );

        when(
                assignment.getUserId()
        ).thenReturn(
                userId
        );

        when(
                users.findByIdAndTenantId(
                        userId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.getAccountStatus()
        ).thenReturn(
                UserAccountStatus.ACTIVE
        );

        when(
                user.getId()
        ).thenReturn(
                userId
        );

        when(
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                userId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(
                        existingMembership
                )
        );


        int created =
                service.ensureSchoolAdministrators(
                        tenantId,
                        institutionSpace
                );


        assertEquals(
                0,
                created
        );

        assertEquals(
                ConnectMemberRole.ADMIN,
                existingMembership.getMemberRole()
        );

        assertEquals(
                ConnectMemberRole.MEMBER,
                existingMembership.getPreviousMemberRole()
        );

        assertEquals(
                "EIAM_SCHOOL_ADMIN",
                existingMembership.getRoleAuthoritySource()
        );

        assertEquals(
                ConnectMembershipStatus.ACTIVE,
                existingMembership.getMembershipStatus()
        );

        verify(
                members
        ).save(
                existingMembership
        );
    }


    @Test
    void inactiveSchoolAdministratorIsNotProvisioned() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID roleId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        ConnectSpace institutionSpace =
                mock(ConnectSpace.class);

        Role schoolAdminRole =
                mock(Role.class);

        UserRole assignment =
                mock(UserRole.class);

        UserAccount user =
                mock(UserAccount.class);


        when(
                institutionSpace.getTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                institutionSpace.getId()
        ).thenReturn(
                spaceId
        );

        when(
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        "SCHOOL_ADMIN"
                )
        ).thenReturn(
                Optional.of(
                        schoolAdminRole
                )
        );

        when(
                schoolAdminRole.getId()
        ).thenReturn(
                roleId
        );

        when(
                userRoles.findAllByTenantIdAndRoleId(
                        tenantId,
                        roleId
                )
        ).thenReturn(
                List.of(
                        assignment
                )
        );

        when(
                assignment.getUserId()
        ).thenReturn(
                userId
        );

        when(
                users.findByIdAndTenantId(
                        userId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.getAccountStatus()
        ).thenReturn(
                UserAccountStatus.SUSPENDED
        );


        int created =
                service.ensureSchoolAdministrators(
                        tenantId,
                        institutionSpace
                );


        assertEquals(
                0,
                created
        );

        verifyNoInteractions(
                members
        );
    }


    @Test
    void missingSchoolAdminRoleFailsClearly() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        ConnectSpace institutionSpace =
                mock(ConnectSpace.class);


        when(
                institutionSpace.getTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                institutionSpace.getId()
        ).thenReturn(
                spaceId
        );

        when(
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        "SCHOOL_ADMIN"
                )
        ).thenReturn(
                Optional.empty()
        );


        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.ensureSchoolAdministrators(
                                        tenantId,
                                        institutionSpace
                                )
                );


        assertTrue(
                error.getMessage()
                        .contains(
                                "SCHOOL_ADMIN"
                        )
        );

        verifyNoInteractions(
                userRoles,
                users,
                members
        );
    }


    @Test
    void institutionSpaceTenantMustMatchProvisioningTenant() {

        UUID requestedTenant =
                UUID.randomUUID();

        UUID spaceTenant =
                UUID.randomUUID();

        ConnectSpace institutionSpace =
                mock(ConnectSpace.class);


        when(
                institutionSpace.getTenantId()
        ).thenReturn(
                spaceTenant
        );


        assertThrows(
                TenantScopeViolationException.class,
                () ->
                        service.ensureSchoolAdministrators(
                                requestedTenant,
                                institutionSpace
                        )
        );


        verifyNoInteractions(
                roles,
                userRoles,
                users,
                members
        );
    }


    @Test
    void missingUserAccountIsIgnoredSafely() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID roleId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        ConnectSpace institutionSpace =
                mock(ConnectSpace.class);

        Role schoolAdminRole =
                mock(Role.class);

        UserRole assignment =
                mock(UserRole.class);


        when(
                institutionSpace.getTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                institutionSpace.getId()
        ).thenReturn(
                spaceId
        );

        when(
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        "SCHOOL_ADMIN"
                )
        ).thenReturn(
                Optional.of(
                        schoolAdminRole
                )
        );

        when(
                schoolAdminRole.getId()
        ).thenReturn(
                roleId
        );

        when(
                userRoles.findAllByTenantIdAndRoleId(
                        tenantId,
                        roleId
                )
        ).thenReturn(
                List.of(
                        assignment
                )
        );

        when(
                assignment.getUserId()
        ).thenReturn(
                userId
        );

        when(
                users.findByIdAndTenantId(
                        userId,
                        tenantId
                )
        ).thenReturn(
                Optional.empty()
        );


        int created =
                service.ensureSchoolAdministrators(
                        tenantId,
                        institutionSpace
                );


        assertEquals(
                0,
                created
        );

        verifyNoInteractions(
                members
        );
    }
}
