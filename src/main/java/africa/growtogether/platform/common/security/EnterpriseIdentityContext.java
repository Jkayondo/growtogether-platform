package africa.growtogether.platform.common.security;

import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reusable EIAM integration contract for all GT shared platform services.
 * Product and platform modules must use this component instead of parsing JWTs
 * or reading Spring Security internals directly.
 */
@Component
public final class EnterpriseIdentityContext {

    public GtPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
            || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof GtPrincipal principal)) {
            throw new AccessDeniedException(
                "An authenticated GrowTogether principal is required."
            );
        }

        return principal;
    }

    public UUID userId() {
        return requirePrincipal().userId();
    }

    public UUID requireUserId() {
        UUID userId = userId();

        if (userId == null) {
            throw new AccessDeniedException(
                "The authenticated GrowTogether principal has no user ID."
            );
        }

        return userId;
    }

    public UUID tenantId() {
        return requirePrincipal().tenantId();
    }

    public UUID requireTenantId() {
        UUID tenantId = tenantId();

        if (tenantId == null) {
            throw new AccessDeniedException(
                "The authenticated GrowTogether principal has no tenant ID."
            );
        }

        return tenantId;
    }

    public UUID sessionId() {
        return requirePrincipal().sessionId();
    }

    public Set<String> roles() {
        return requirePrincipal().roles();
    }

    public Set<String> permissions() {
        return requirePrincipal().permissions();
    }

    public boolean hasPermission(String permission) {
        return permissions().contains(permission);
    }

    public boolean hasRole(String role) {
        return roles().contains(role);
    }

    public void requireTenant(UUID requestedTenantId) {
        if (requestedTenantId == null) {
            throw new AccessDeniedException("A tenant ID is required.");
        }

        if (!requireTenantId().equals(requestedTenantId)) {
            throw new AccessDeniedException(
                "Cross-tenant access is not permitted."
            );
        }
    }
}