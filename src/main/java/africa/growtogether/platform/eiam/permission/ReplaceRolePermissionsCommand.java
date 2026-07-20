package africa.growtogether.platform.eiam.permission;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;
public record ReplaceRolePermissionsCommand(@NotNull Set<UUID> permissionIds) {}
