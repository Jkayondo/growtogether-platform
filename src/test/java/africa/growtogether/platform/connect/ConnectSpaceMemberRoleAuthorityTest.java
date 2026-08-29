package africa.growtogether.platform.connect;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectSpaceMemberRoleAuthorityTest {

    @Test
    void authorityTemporarilyOverridesExistingMemberRole() {

        ConnectSpaceMember membership =
                member(
                        ConnectMemberRole.MEMBER
                );

        membership.applyAuthoritativeRole(
                ConnectMemberRole.ADMIN,
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
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

        assertEquals(
                ConnectMembershipStatus.ACTIVE,
                membership.getMembershipStatus()
        );
    }


    @Test
    void releasingAuthorityRestoresPreviousRole() {

        ConnectSpaceMember membership =
                member(
                        ConnectMemberRole.MEMBER
                );

        membership.applyAuthoritativeRole(
                ConnectMemberRole.ADMIN,
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
        );

        membership.releaseAuthoritativeRole(
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
        );

        assertEquals(
                ConnectMemberRole.MEMBER,
                membership.getMemberRole()
        );

        assertNull(
                membership.getPreviousMemberRole()
        );

        assertNull(
                membership.getRoleAuthoritySource()
        );

        assertEquals(
                ConnectMembershipStatus.ACTIVE,
                membership.getMembershipStatus()
        );

        assertNull(
                membership.getLeftAt()
        );
    }


    @Test
    void authorityCreatedAdminIsRemovedWhenAuthorityEnds() {

        ConnectSpaceMember membership =
                member(
                        ConnectMemberRole.ADMIN
                );

        membership.applyAuthoritativeRole(
                ConnectMemberRole.ADMIN,
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
        );

        assertNull(
                membership.getPreviousMemberRole()
        );

        membership.releaseAuthoritativeRole(
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
        );

        assertEquals(
                ConnectMembershipStatus.REMOVED,
                membership.getMembershipStatus()
        );

        assertNotNull(
                membership.getLeftAt()
        );

        assertEquals(
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN,
                membership.getRoleAuthoritySource()
        );
    }


    @Test
    void anotherAuthorityCannotOverwriteCurrentAuthority() {

        ConnectSpaceMember membership =
                member(
                        ConnectMemberRole.MEMBER
                );

        membership.applyAuthoritativeRole(
                ConnectMemberRole.ADMIN,
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
        );

        assertThrows(
                IllegalStateException.class,
                () -> membership.applyAuthoritativeRole(
                        ConnectMemberRole.MODERATOR,
                        "ANOTHER_AUTHORITY"
                )
        );

        assertEquals(
                ConnectMemberRole.ADMIN,
                membership.getMemberRole()
        );

        assertEquals(
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN,
                membership.getRoleAuthoritySource()
        );
    }


    @Test
    void ordinaryChangeCannotBypassAuthoritativeRole() {

        ConnectSpaceMember membership =
                member(
                        ConnectMemberRole.MEMBER
                );

        membership.applyAuthoritativeRole(
                ConnectMemberRole.ADMIN,
                ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
        );

        assertThrows(
                IllegalStateException.class,
                () -> membership.changeRole(
                        ConnectMemberRole.MEMBER
                )
        );

        assertEquals(
                ConnectMemberRole.ADMIN,
                membership.getMemberRole()
        );
    }


    private static ConnectSpaceMember member(
            ConnectMemberRole role
    ) {

        return new ConnectSpaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                role
        );
    }
}
