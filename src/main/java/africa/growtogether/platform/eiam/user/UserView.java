package africa.growtogether.platform.eiam.user;

import java.time.Instant;
import java.util.UUID;

public record UserView(UUID id, UUID tenantId, String username, String email, String displayName,
                       UserAccountStatus accountStatus, Instant createdAt, long version) {
    static UserView from(UserAccount user) {
        return new UserView(user.getId(), user.getTenantId(), user.getUsername(), user.getEmail(),
            user.getDisplayName(), user.getAccountStatus(), user.getCreatedAt(), user.getVersion());
    }
}
