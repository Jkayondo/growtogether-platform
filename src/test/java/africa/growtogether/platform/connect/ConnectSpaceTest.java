package africa.growtogether.platform.connect;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConnectSpaceTest {

    @Test
    void createsTenantScopedSpace() {
        UUID tenantId = UUID.randomUUID();

        ConnectSpace space = new ConnectSpace(
                tenantId,
                ConnectSpaceType.CLASS,
                " Primary Seven ",
                " CLASS ",
                " P7-A "
        );

        assertEquals(tenantId, space.getTenantId());
        assertEquals(ConnectSpaceType.CLASS, space.getSpaceType());
        assertEquals("Primary Seven", space.getName());
        assertEquals("CLASS", space.getContextType());
        assertEquals("P7-A", space.getContextReference());
    }

    @Test
    void rejectsMissingTenant() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectSpace(
                        null,
                        ConnectSpaceType.GROUP,
                        "Staff",
                        null,
                        null
                )
        );
    }

    @Test
    void rejectsMissingSpaceType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectSpace(
                        UUID.randomUUID(),
                        null,
                        "Staff",
                        null,
                        null
                )
        );
    }

    @Test
    void convertsBlankOptionalValuesToNull() {
        ConnectSpace space = new ConnectSpace(
                UUID.randomUUID(),
                ConnectSpaceType.GROUP,
                "   ",
                "   ",
                "   "
        );

        assertNull(space.getName());
        assertNull(space.getContextType());
        assertNull(space.getContextReference());
    }
}
