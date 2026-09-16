package africa.growtogether.platform.eiam.bootstrap;

import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.error.GlobalExceptionHandler;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.security.JwtAuthenticationFilter;
import africa.growtogether.platform.common.security.JwtService;
import africa.growtogether.platform.common.security.SecurityConfiguration;
import africa.growtogether.platform.common.security.SecurityErrorWriter;
import africa.growtogether.platform.common.security.TenantBoundaryFilter;
import africa.growtogether.platform.common.web.RequestContextFilter;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FirstAdminBootstrapController.class)
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
class FirstAdminBootstrapHttpSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FirstAdminBootstrapService service;

    @Test
    void anonymousBootstrapRequestReachesControllerAndReturnsCreated()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(
                service.bootstrap(
                        eq("controlled-bootstrap-token"),
                        any(FirstAdminBootstrapCommand.class)
                )
        ).thenReturn(
                new FirstAdminBootstrapView(
                        tenantId,
                        userId,
                        roleId
                )
        );

        mockMvc.perform(
                post("/api/v1/eiam/bootstrap/first-admin")
                        .header(
                                "X-Tenant-ID",
                                tenantId.toString()
                        )
                        .header(
                                "X-GT-Bootstrap-Token",
                                "controlled-bootstrap-token"
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "administratorEmail":
                                    "pilot-admin@growtogether.africa",
                                  "administratorUsername":
                                    "pilot-admin",
                                  "administratorPassword":
                                    "Strong-Pilot-Administrator-Password-2026",
                                  "administratorDisplayName":
                                    "Pilot Tenant Administrator"
                                }
                                """
                        )
        )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-EIAM-BOOTSTRAP-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.tenantId")
                                .value(
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.administratorUserId")
                                .value(
                                        userId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.tenantAdminRoleId")
                                .value(
                                        roleId.toString()
                                )
                );

        verify(service).bootstrap(
                eq("controlled-bootstrap-token"),
                any(FirstAdminBootstrapCommand.class)
        );
    }

    @Test
    void unrelatedEndpointRemainsProtectedWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/eiam/roles")
                        .header(
                                "X-Tenant-ID",
                                UUID.randomUUID().toString()
                        )
        )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(service);
    }
}
