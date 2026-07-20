package africa.growtogether.platform.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class EnterpriseIdentityContextTest {
    private final EnterpriseIdentityContext context = new EnterpriseIdentityContext();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void exposesAuthenticatedEiamIdentity() {
        UUID user = UUID.randomUUID();
        UUID tenant = UUID.randomUUID();
        UUID session = UUID.randomUUID();
        GtPrincipal principal = new GtPrincipal(user, "john", tenant,
            Set.of("TENANT_ADMIN"), Set.of("workflow.instance.start"), session);
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(principal, "token", Set.of()));

        assertEquals(user, context.userId());
        assertEquals(tenant, context.tenantId());
        assertEquals(session, context.sessionId());
        assertTrue(context.hasRole("TENANT_ADMIN"));
        assertTrue(context.hasPermission("workflow.instance.start"));
    }

    @Test
    void rejectsCrossTenantOperation() {
        UUID tenant = UUID.randomUUID();
        GtPrincipal principal = new GtPrincipal(UUID.randomUUID(), "john", tenant,
            Set.of(), Set.of(), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(principal, "token", Set.of()));

        assertThrows(AccessDeniedException.class, () -> context.requireTenant(UUID.randomUUID()));
    }
}
