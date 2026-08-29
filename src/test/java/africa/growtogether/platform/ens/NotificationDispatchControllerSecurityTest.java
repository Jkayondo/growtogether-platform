package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.error.GlobalExceptionHandler;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.security.JwtAuthenticationFilter;
import africa.growtogether.platform.common.security.JwtService;
import africa.growtogether.platform.common.security.SecurityConfiguration;
import africa.growtogether.platform.common.security.SecurityErrorWriter;
import africa.growtogether.platform.common.security.TenantBoundaryFilter;
import africa.growtogether.platform.common.web.RequestContextFilter;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(NotificationDispatchController.class)
@EnableWebSecurity
@Import({
        ApiResponses.class,
        RequestContextFilter.class,
        GlobalExceptionHandler.class,
        SecurityErrorWriter.class,
        SecurityConfiguration.class,
        JwtAuthenticationFilter.class,
        TenantBoundaryFilter.class,
        EnterpriseIdentityContext.class,
        JwtService.class
})
@TestPropertySource(
        properties = {
                "gt.security.jwt.issuer=gt-test",
                "gt.security.jwt.secret=01234567890123456789012345678901",
                "gt.security.jwt.access-token-seconds=300"
        }
)
class NotificationDispatchControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private NotificationFailoverDispatcher dispatcher;


    @Test
    void authorisedIntegrationAdministratorCanDispatchUsingActiveTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID notificationId =
                UUID.randomUUID();

        String token =
                token(
                        tenantId,
                        Set.of(
                                "notification.dispatch.manage"
                        )
                );

        when(
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                )
        ).thenReturn(
                NotificationStatus.SENT
        );

        mockMvc.perform(
                        post(
                                "/api/v1/notifications/admin/"
                                        + notificationId
                                        + "/dispatch"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                /*
                                 * Hostile legacy-style tenant input is ignored.
                                 * Tenant authority comes from authenticated
                                 * request context.
                                 */
                                .param(
                                        "tenantId",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-ENS-DISPATCH-001")
                )
                .andExpect(
                        jsonPath("$.data.notificationId")
                                .value(notificationId.toString())
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("SENT")
                );

        verify(dispatcher)
                .dispatch(
                        tenantId,
                        notificationId
                );
    }


    @Test
    void routeAuthorityAloneCannotDispatch()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID notificationId =
                UUID.randomUUID();

        String token =
                token(
                        tenantId,
                        Set.of(
                                "notification.route.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/notifications/admin/"
                                        + notificationId
                                        + "/dispatch"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                dispatcher,
                never()
        ).dispatch(
                any(),
                any()
        );
    }


    @Test
    void crossTenantDispatchIsRejectedBeforeDispatcher()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID notificationId =
                UUID.randomUUID();

        String token =
                token(
                        principalTenant,
                        Set.of(
                                "notification.dispatch.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/notifications/admin/"
                                        + notificationId
                                        + "/dispatch"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-TENANT-002")
                );

        verify(
                dispatcher,
                never()
        ).dispatch(
                any(),
                any()
        );
    }


    @Test
    void unauthenticatedDispatchIsRejected()
            throws Exception {

        UUID notificationId =
                UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/notifications/admin/"
                                        + notificationId
                                        + "/dispatch"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verify(
                dispatcher,
                never()
        ).dispatch(
                any(),
                any()
        );
    }


    private String token(
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "notification-dispatch-http-user",
                        tenantId,
                        Set.of(
                                "INTEGRATION_ADMIN"
                        ),
                        permissions,
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }
}
