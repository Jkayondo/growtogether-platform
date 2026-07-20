package africa.growtogether.platform.common.persistence;

import africa.growtogether.platform.common.security.GtPrincipal;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public final class RequestContextAuditorAware implements AuditorAware<String> {
    public static final String SYSTEM_AUDITOR = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof GtPrincipal principal) {
            return Optional.of(principal.userId().toString());
        }
        return Optional.of(SYSTEM_AUDITOR);
    }
}
