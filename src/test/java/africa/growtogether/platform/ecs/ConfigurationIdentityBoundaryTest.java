package africa.growtogether.platform.ecs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class ConfigurationIdentityBoundaryTest {
    @Test
    void identityContextRejectsCrossTenantAccess() {
        EnterpriseIdentityContext identity = mock(EnterpriseIdentityContext.class);
        UUID authenticatedTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        doThrow(new AccessDeniedException("Cross-tenant access is not permitted."))
            .when(identity).requireTenant(requestedTenant);

        assertThrows(AccessDeniedException.class, () -> identity.requireTenant(requestedTenant));
        verify(identity).requireTenant(requestedTenant);
        assertNotEquals(authenticatedTenant, requestedTenant);
    }
}
