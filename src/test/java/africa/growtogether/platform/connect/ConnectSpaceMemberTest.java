package africa.growtogether.platform.connect;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConnectSpaceMemberTest {

    @Test
    void createsActiveMembershipWithDefaultRole() {
        UUID tenantId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ConnectSpaceMember member =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        userId,
                        null
                );

        assertEquals(tenantId, member.getTenantId());
        assertEquals(spaceId, member.getSpaceId());
        assertEquals(userId, member.getUserId());

        assertEquals(
                ConnectMemberRole.MEMBER,
                member.getMemberRole()
        );

        assertEquals(
                ConnectMembershipStatus.ACTIVE,
                member.getMembershipStatus()
        );

        assertNotNull(member.getJoinedAt());
        assertNull(member.getLeftAt());
    }

    @Test
    void preservesExplicitMembershipRole() {
        ConnectSpaceMember member =
                new ConnectSpaceMember(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ConnectMemberRole.MODERATOR
                );

        assertEquals(
                ConnectMemberRole.MODERATOR,
                member.getMemberRole()
        );
    }

    @Test
    void leavingMembershipRecordsLifecycle() {
        ConnectSpaceMember member = member();

        member.leave();

        assertEquals(
                ConnectMembershipStatus.LEFT,
                member.getMembershipStatus()
        );

        assertNotNull(member.getLeftAt());
    }

    @Test
    void removingMembershipRecordsLifecycle() {
        ConnectSpaceMember member = member();

        member.remove();

        assertEquals(
                ConnectMembershipStatus.REMOVED,
                member.getMembershipStatus()
        );

        assertNotNull(member.getLeftAt());
    }

    @Test
    void inactiveMembershipCannotLeaveAgain() {
        ConnectSpaceMember member = member();

        member.leave();

        assertThrows(
                IllegalStateException.class,
                member::leave
        );
    }

    @Test
    void rejectsMissingIdentifiers() {
        UUID id = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectSpaceMember(
                        null,
                        id,
                        id,
                        null
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectSpaceMember(
                        id,
                        null,
                        id,
                        null
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectSpaceMember(
                        id,
                        id,
                        null,
                        null
                )
        );
    }

    private static ConnectSpaceMember member() {
        return new ConnectSpaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                ConnectMemberRole.MEMBER
        );
    }
}
