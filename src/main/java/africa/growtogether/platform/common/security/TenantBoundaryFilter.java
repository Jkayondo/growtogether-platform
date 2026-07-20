package africa.growtogether.platform.common.security;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Enforces that an authenticated EIAM principal can operate only in its JWT tenant. */
@Component
public final class TenantBoundaryFilter extends OncePerRequestFilter {
    private final SecurityErrorWriter errors;

    public TenantBoundaryFilter(SecurityErrorWriter errors) {
        this.errors = errors;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof GtPrincipal principal)) {
            chain.doFilter(request, response);
            return;
        }

        String requestTenant = RequestContextHolder.current().map(RequestContext::tenantId).orElse(null);
        if (requestTenant == null) {
            errors.write(response, HttpServletResponse.SC_BAD_REQUEST,
                "GT-TENANT-001", "X-Tenant-ID is required for authenticated requests.");
            return;
        }

        if (!principal.tenantId().equals(UUID.fromString(requestTenant))) {
            errors.write(response, HttpServletResponse.SC_FORBIDDEN,
                "GT-TENANT-002", "The authenticated identity cannot access the requested tenant.");
            return;
        }
        chain.doFilter(request, response);
    }
}
