package africa.growtogether.platform.connect;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectSpaceMemberRoleTransitionTest {

    @Test
    void activeMemberCanBePromotedWithoutReplacingMembership() {

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ConnectMemberRole.MEMBER
                );

        membership.changeRole(
                ConnectMemberRole.ADMIN
        );

        assertEquals(
                ConnectMemberRole.ADMIN,
                membership.getMemberRole()
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
    void inactiveMembershipCannotChangeRole() {

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ConnectMemberRole.ADMIN
                );

        membership.remove();

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

        assertEquals(
                ConnectMembershipStatus.REMOVED,
                membership.getMembershipStatus()
        );
    }


    @Test
    void roleCannotBeChangedToNull() {

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ConnectMemberRole.MEMBER
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> membership.changeRole(
                        null
                )
        );

        assertEquals(
                ConnectMemberRole.MEMBER,
                membership.getMemberRole()
        );
    }
}
