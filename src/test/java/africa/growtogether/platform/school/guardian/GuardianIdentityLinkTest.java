package africa.growtogether.platform.school.guardian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class GuardianIdentityLinkTest {

    @Test
    void linksGuardianToEiamUser() {

        Guardian guardian =
                guardian(
                        null
                );

        UUID userId =
                UUID.randomUUID();

        guardian.linkEiamUser(
                userId
        );

        assertEquals(
                userId,
                guardian.getEiamUserId()
        );

        assertTrue(
                guardian.hasEiamUserLink()
        );
    }

    @Test
    void linkingSameEiamUserIsIdempotent() {

        UUID userId =
                UUID.randomUUID();

        Guardian guardian =
                guardian(
                        userId
                );

        guardian.linkEiamUser(
                userId
        );

        assertEquals(
                userId,
                guardian.getEiamUserId()
        );
    }

    @Test
    void rejectsRelinkingGuardianToDifferentEiamUser() {

        UUID firstUserId =
                UUID.randomUUID();

        UUID secondUserId =
                UUID.randomUUID();

        Guardian guardian =
                guardian(
                        firstUserId
                );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> guardian.linkEiamUser(
                                secondUserId
                        )
                );

        assertEquals(
                "Guardian is already linked to a different EIAM user",
                error.getMessage()
        );

        assertEquals(
                firstUserId,
                guardian.getEiamUserId()
        );
    }

    @Test
    void rejectsNullEiamUserLink() {

        Guardian guardian =
                guardian(
                        null
                );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> guardian.linkEiamUser(
                                null
                        )
                );

        assertEquals(
                "eiamUserId must not be null",
                error.getMessage()
        );

        assertFalse(
                guardian.hasEiamUserLink()
        );
    }

    private static Guardian guardian(
            UUID eiamUserId
    ) {

        return new Guardian(
                "PPIS-GDN-000001",
                "Sarah",
                null,
                "Nakato",
                null,
                null,
                null,
                "UG",
                null,
                null,
                "+256701234567",
                null,
                "sarah@example.com",
                null,
                null,
                null,
                null,
                eiamUserId,
                UUID.randomUUID(),
                "en"
        );
    }
}
