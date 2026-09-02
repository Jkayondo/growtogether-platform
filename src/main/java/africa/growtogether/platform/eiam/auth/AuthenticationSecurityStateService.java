package africa.growtogether.platform.eiam.auth;

import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationSecurityStateService {

    private final UserAccountRepository users;

    public AuthenticationSecurityStateService(
            UserAccountRepository users
    ) {
        this.users = users;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(
            UUID tenantId,
            UUID userId,
            Instant now,
            int maxAttempts,
            long lockSeconds
    ) {
        UserAccount user =
                users.findByIdAndTenantIdForAuthenticationSecurityUpdate(
                        userId,
                        tenantId
                ).orElseThrow(
                        () -> new AuthenticationException(
                                "Authentication security account is unavailable."
                        )
                );

        user.recordFailedLogin(
                now,
                maxAttempts,
                lockSeconds
        );

        users.saveAndFlush(
                user
        );
    }
}
