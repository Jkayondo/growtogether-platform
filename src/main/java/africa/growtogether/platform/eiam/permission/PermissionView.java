package africa.growtogether.platform.eiam.permission;
import africa.growtogether.platform.common.persistence.EntityStatus;
import java.util.UUID;
public record PermissionView(UUID id, String code, String name, String module, String description, boolean systemPermission, EntityStatus status) {
    static PermissionView from(Permission p) { return new PermissionView(p.getId(), p.getCode(), p.getName(), p.getModule(), p.getDescription(), p.isSystemPermission(), p.getStatus()); }
}
