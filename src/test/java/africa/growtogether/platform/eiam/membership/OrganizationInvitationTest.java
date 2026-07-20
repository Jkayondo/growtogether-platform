package africa.growtogether.platform.eiam.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class OrganizationInvitationTest {
    @Test
    void acceptsPendingInvitationOnce() {
        OrganizationInvitation invitation = new OrganizationInvitation("USER@EXAMPLE.COM", "hash", Instant.now().plusSeconds(60), null);
        invitation.accept(Instant.now());
        assertEquals(InvitationStatus.ACCEPTED, invitation.getInvitationStatus());
        assertThrows(MembershipException.class, () -> invitation.accept(Instant.now()));
    }

    @Test
    void rejectsExpiredInvitation() {
        OrganizationInvitation invitation = new OrganizationInvitation("user@example.com", "hash", Instant.now().minusSeconds(1), null);
        assertThrows(MembershipException.class, () -> invitation.assertAcceptable(Instant.now()));
        assertEquals(InvitationStatus.EXPIRED, invitation.getInvitationStatus());
    }

    @Test
    void removedMembershipIsTerminal() {
        TenantMembership membership = new TenantMembership(java.util.UUID.randomUUID(), Instant.now());
        membership.changeStatus(MembershipStatus.REMOVED, Instant.now());
        assertThrows(MembershipException.class, () -> membership.changeStatus(MembershipStatus.ACTIVE, Instant.now()));
    }
}
