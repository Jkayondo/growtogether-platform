package africa.growtogether.platform.eiam.role;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RoleLifecycleTest {
    @Test void normalizesCodeAndUpdatesCustomRole() {
        Role role = new Role("school admin", "School Admin", "Manages school", false);
        assertEquals("SCHOOL_ADMIN", role.getCode());
        role.update("campus_admin", "Campus Admin", "Updated");
        assertEquals("CAMPUS_ADMIN", role.getCode());
        assertEquals("Campus Admin", role.getName());
    }

    @Test void protectsSystemRoleIdentity() {
        Role role = new Role("TENANT_ADMIN", "Tenant Admin", null, true);
        assertThrows(RoleLifecycleException.class,
            () -> role.update("OTHER", "Other", null));
    }
}
