package africa.growtogether.platform.eaif.approval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.security.GtPrincipal;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class EaifApprovalControllerAuthorizationContractTest {

    private final EaifApprovalService service =
            mock(EaifApprovalService.class);

    private final EnterpriseIdentityContext identity =
            new EnterpriseIdentityContext();

    private final EaifApprovalController controller =
            new EaifApprovalController(
                    service,
                    identity
            );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void approveUsesAuthenticatedTenantAndActor() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        authenticate(
                tenantId,
                userId,
                Set.of("ai.request.approval")
        );

        EaifApprovalRecord expected =
                new EaifApprovalRecord(
                        tenantId,
                        requestId
                );

        when(
                service.approveAndRelease(
                        tenantId,
                        requestId,
                        userId,
                        "Approved for controlled execution"
                )
        ).thenReturn(expected);

        EaifApprovalRecord actual =
                controller.approve(
                        requestId,
                        "Approved for controlled execution"
                );

        assertSame(expected, actual);

        verify(service).approveAndRelease(
                tenantId,
                requestId,
                userId,
                "Approved for controlled execution"
        );
    }

    @Test
    void rejectUsesAuthenticatedTenantAndActor() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        authenticate(
                tenantId,
                userId,
                Set.of("ai.request.approval")
        );

        EaifApprovalRecord expected =
                new EaifApprovalRecord(
                        tenantId,
                        requestId
                );

        when(
                service.rejectAndBlock(
                        tenantId,
                        requestId,
                        userId,
                        "Rejected by governance policy"
                )
        ).thenReturn(expected);

        EaifApprovalRecord actual =
                controller.reject(
                        requestId,
                        "Rejected by governance policy"
                );

        assertSame(expected, actual);

        verify(service).rejectAndBlock(
                tenantId,
                requestId,
                userId,
                "Rejected by governance policy"
        );
    }

    @Test
    void unauthenticatedIdentityCannotReachApprovalService() {
        SecurityContextHolder.clearContext();

        assertThrows(
                AccessDeniedException.class,
                () ->
                        controller.approve(
                                UUID.randomUUID(),
                                "Should not execute"
                        )
        );

        verifyNoInteractions(service);
    }

    @Test
    void approvalEndpointsRequireApprovalPermission()
            throws Exception {

        assertApprovalPermission(
                EaifApprovalController.class.getDeclaredMethod(
                        "approve",
                        UUID.class,
                        String.class
                )
        );

        assertApprovalPermission(
                EaifApprovalController.class.getDeclaredMethod(
                        "reject",
                        UUID.class,
                        String.class
                )
        );
    }

    @Test
    void callerCannotSupplyTenantOrDecisionActor()
            throws Exception {

        Method approve =
                EaifApprovalController.class.getDeclaredMethod(
                        "approve",
                        UUID.class,
                        String.class
                );

        Method reject =
                EaifApprovalController.class.getDeclaredMethod(
                        "reject",
                        UUID.class,
                        String.class
                );

        assertEquals(2, approve.getParameterCount());
        assertEquals(UUID.class, approve.getParameterTypes()[0]);
        assertEquals(String.class, approve.getParameterTypes()[1]);

        assertEquals(2, reject.getParameterCount());
        assertEquals(UUID.class, reject.getParameterTypes()[0]);
        assertEquals(String.class, reject.getParameterTypes()[1]);
    }

    private static void assertApprovalPermission(
            Method method
    ) {
        PreAuthorize preAuthorize =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertNotNull(preAuthorize);

        assertEquals(
                "hasAuthority('ai.request.approval')",
                preAuthorize.value()
        );
    }

    private static void authenticate(
            UUID tenantId,
            UUID userId,
            Set<String> permissions
    ) {
        GtPrincipal principal =
                new GtPrincipal(
                        userId,
                        "step24-approval-user",
                        tenantId,
                        Set.of("AI_ADMIN"),
                        permissions,
                        UUID.randomUUID()
                );

        var authorities =
                permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        principal,
                        null,
                        authorities
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}
