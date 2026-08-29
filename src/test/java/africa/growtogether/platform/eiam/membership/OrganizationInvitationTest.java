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
    @Test
    void supportsPhoneTargetInvitation() {
        OrganizationInvitation invitation =
            new OrganizationInvitation(
                null,
                "+256701234567",
                "hash",
                Instant.now().plusSeconds(60),
                null
            );

        assertEquals(
            "+256701234567",
            invitation.getPhoneNumber()
        );

        assertEquals(
            null,
            invitation.getEmail()
        );

        assertEquals(
            true,
            invitation.isPhoneTarget()
        );

        assertEquals(
            false,
            invitation.isEmailTarget()
        );
    }

    @Test
    void rejectsInvitationWithBothEmailAndPhoneTargets() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new OrganizationInvitation(
                "user@example.com",
                "+256701234567",
                "hash",
                Instant.now().plusSeconds(60),
                null
            )
        );
    }

    @Test
    void rejectsInvitationWithoutContactIdentity() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new OrganizationInvitation(
                null,
                null,
                "hash",
                Instant.now().plusSeconds(60),
                null
            )
        );
    }

    @Test
    void rejectsNonCanonicalPhoneTarget() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new OrganizationInvitation(
                null,
                "0701234567",
                "hash",
                Instant.now().plusSeconds(60),
                null
            )
        );
    }

}
