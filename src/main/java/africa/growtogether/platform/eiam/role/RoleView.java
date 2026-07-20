package africa.growtogether.platform.eiam.role;

import java.time.Instant;
import java.util.UUID;

public record RoleView(UUID id, String code, String name, String description, boolean systemRole,
                       long version, Instant createdAt, Instant updatedAt) {
    static RoleView from(Role role) {
        return new RoleView(role.getId(), role.getCode(), role.getName(), role.getDescription(),
            role.isSystemRole(), role.getVersion(), role.getCreatedAt(), role.getUpdatedAt());
    }
}
