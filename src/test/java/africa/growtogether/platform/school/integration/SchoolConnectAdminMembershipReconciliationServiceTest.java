package africa.growtogether.platform.school.integration;

import africa.growtogether.platform.connect.ConnectMemberRole;
import africa.growtogether.platform.connect.ConnectMembershipStatus;
import africa.growtogether.platform.connect.ConnectRoleAuthoritySources;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolConnectAdminMembershipReconciliationServiceTest {

    @Mock
    private RoleRepository roles;

    @Mock
    private UserRoleRepository userRoles;

    @Mock
    private UserAccountRepository users;

    @Mock
    private ConnectSpaceMemberRepository members;

    private SchoolConnectAdminMembershipReconciliationService service;

    private UUID tenantId;
    private UUID spaceId;
    private UUID userId;

    private ConnectSpace institutionSpace;


    @BeforeEach
    void setUp() {

        service =
                new SchoolConnectAdminMembershipReconciliationService(
                        roles,
                        userRoles,
                        users,
                        members
                );

        tenantId = UUID.randomUUID();
        spaceId = UUID.randomUUID();
        userId = UUID.randomUUID();

        institutionSpace =
                mock(
                        ConnectSpace.class
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
    }


    @Test
    void eligibleSchoolAdministratorGetsAuthorityDerivedAdminMembership() {

        stubEligibleSchoolAdmin();

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

        service.reconcile(
                tenantId,
                userId,
                institutionSpace
        );

        ArgumentCaptor<ConnectSpaceMember> captor =
                ArgumentCaptor.forClass(
                        ConnectSpaceMember.class
                );

        verify(
                members
        ).save(
                captor.capture()
        );

        ConnectSpaceMember membership =
                captor.getValue();

        assertEquals(
                ConnectMemberRole.ADMIN,
                membership.getMemberRole()
        );

        assertEquals(
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN,
                membership.getRoleAuthoritySource()
        );

        assertNull(
                membership.getPreviousMemberRole()
        );

        assertEquals(
                ConnectMembershipStatus.ACTIVE,
                membership.getMembershipStatus()
        );
    }


    @Test
    void existingMemberIsPromotedAndLaterRestorable() {

        stubEligibleSchoolAdmin();

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        userId,
                        ConnectMemberRole.MEMBER
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
                        membership
                )
        );

        service.reconcile(
                tenantId,
                userId,
                institutionSpace
        );

        assertEquals(
                ConnectMemberRole.ADMIN,
                membership.getMemberRole()
        );

        assertEquals(
                ConnectMemberRole.MEMBER,
                membership.getPreviousMemberRole()
        );

        assertEquals(
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN,
                membership.getRoleAuthoritySource()
        );

        verify(
                members
        ).save(
                membership
        );
    }


    @Test
    void revokedSchoolAdminRestoresPreviousMemberRole() {

        stubActiveUserWithoutSchoolAdmin();

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        userId,
                        ConnectMemberRole.MEMBER
                );

        membership.applyAuthoritativeRole(
                ConnectMemberRole.ADMIN,
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
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
                        membership
                )
        );

        service.reconcile(
                tenantId,
                userId,
                institutionSpace
        );

        assertEquals(
                ConnectMemberRole.MEMBER,
                membership.getMemberRole()
        );

        assertNull(
                membership.getRoleAuthoritySource()
        );

        assertNull(
                membership.getPreviousMemberRole()
        );

        assertEquals(
                ConnectMembershipStatus.ACTIVE,
                membership.getMembershipStatus()
        );
    }


    @Test
    void revokedAuthorityCreatedAdminIsRemoved() {

        stubActiveUserWithoutSchoolAdmin();

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        userId,
                        ConnectMemberRole.ADMIN
                );

        membership.applyAuthoritativeRole(
                ConnectMemberRole.ADMIN,
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
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
                        membership
                )
        );

        service.reconcile(
                tenantId,
                userId,
                institutionSpace
        );

        assertEquals(
                ConnectMembershipStatus.REMOVED,
                membership.getMembershipStatus()
        );

        assertNotNull(
                membership.getLeftAt()
        );
    }


    @Test
    void inactiveUserCannotRetainDerivedSchoolAdminAuthority() {

        UserAccount user =
                mock(
                        UserAccount.class
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

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        userId,
                        ConnectMemberRole.ADMIN
                );

        membership.applyAuthoritativeRole(
                ConnectMemberRole.ADMIN,
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
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
                        membership
                )
        );

        service.reconcile(
                tenantId,
                userId,
                institutionSpace
        );

        assertEquals(
                ConnectMembershipStatus.REMOVED,
                membership.getMembershipStatus()
        );

        verify(
                roles,
                never()
        ).findByTenantIdAndCodeIgnoreCase(
                any(),
                any()
        );
    }


    private void stubEligibleSchoolAdmin() {

        UserAccount user =
                mock(
                        UserAccount.class
                );

        Role role =
                mock(
                        Role.class
                );

        UserRole assignment =
                mock(
                        UserRole.class
                );

        UUID roleId =
                UUID.randomUUID();

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
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        "SCHOOL_ADMIN"
                )
        ).thenReturn(
                Optional.of(
                        role
                )
        );

        when(
                role.getId()
        ).thenReturn(
                roleId
        );

        when(
                assignment.getRoleId()
        ).thenReturn(
                roleId
        );

        when(
                userRoles.findAllByTenantIdAndUserId(
                        tenantId,
                        userId
                )
        ).thenReturn(
                List.of(
                        assignment
                )
        );
    }


    private void stubActiveUserWithoutSchoolAdmin() {

        UserAccount user =
                mock(
                        UserAccount.class
                );

        Role role =
                mock(
                        Role.class
                );

        UUID roleId =
                UUID.randomUUID();

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
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        "SCHOOL_ADMIN"
                )
        ).thenReturn(
                Optional.of(
                        role
                )
        );

        when(
                userRoles.findAllByTenantIdAndUserId(
                        tenantId,
                        userId
                )
        ).thenReturn(
                List.of()
        );
    }
}
