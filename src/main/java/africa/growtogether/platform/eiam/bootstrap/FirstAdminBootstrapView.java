package africa.growtogether.platform.eiam.bootstrap;

import java.util.UUID;

public record FirstAdminBootstrapView(
        UUID tenantId,
        UUID administratorUserId,
        UUID tenantAdminRoleId
) {}
