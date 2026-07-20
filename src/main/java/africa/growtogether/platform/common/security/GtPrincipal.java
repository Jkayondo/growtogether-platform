package africa.growtogether.platform.common.security;

import java.util.Set;
import java.util.UUID;

public record GtPrincipal(
    UUID userId,
    String username,
    UUID tenantId,
    Set<String> roles,
    Set<String> permissions,
    UUID sessionId
) {
    public GtPrincipal {
        roles = Set.copyOf(roles);
        permissions = Set.copyOf(permissions);
    }
}
