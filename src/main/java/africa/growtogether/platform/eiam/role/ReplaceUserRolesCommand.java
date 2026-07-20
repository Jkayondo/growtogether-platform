package africa.growtogether.platform.eiam.role;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record ReplaceUserRolesCommand(@NotNull Set<UUID> roleIds) {}
