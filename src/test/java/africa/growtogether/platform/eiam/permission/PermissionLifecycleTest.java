package africa.growtogether.platform.eiam.permission;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PermissionLifecycleTest {
    @Test void normalizesCodeAndModule() {
        Permission permission = new Permission(" EIAM.Users.Read ", "Read users", "eiam", null, false);
        assertEquals("eiam.users.read", permission.getCode());
        assertEquals("EIAM", permission.getModule());
    }

    @Test void protectsSystemPermissionCode() {
        Permission permission = new Permission("eiam.users.read", "Read users", "EIAM", null, true);
        assertThrows(PermissionLifecycleException.class,
            () -> permission.update("eiam.users.write", "Write users", "EIAM", null));
    }
}
