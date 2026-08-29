package africa.growtogether.platform.eip.integration;

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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static africa.growtogether.platform.eip.integration.EipRuntimeConfigurationDtos.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(EipRuntimeConfigurationController.class)
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
class EipRuntimeConfigurationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private EipRuntimeConfigurationService service;


    @Test
    void runtimeAdministratorCanReadExternalDeliveryState()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String correlationId =
                "B8-EIP-RUNTIME-READ-001";

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "integration.runtime.manage"
                        )
                );

        when(
                service.externalDelivery()
        ).thenReturn(
                new ExternalDeliveryView(
                        false
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/integration/runtime/external-delivery"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .header(
                                        "X-Correlation-ID",
                                        correlationId
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
                        jsonPath("$.data.enabled")
                                .value(false)
                )
                .andExpect(
                        header()
                                .string(
                                        "X-Correlation-ID",
                                        correlationId
                                )
                );

        verify(service)
                .externalDelivery();
    }


    @Test
    void runtimeAdministratorCanSetExternalDeliveryWithoutTenantInput()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "integration.runtime.manage"
                        )
                );

        when(
                service.setExternalDelivery(
                        any(SetExternalDeliveryCommand.class)
                )
        ).thenReturn(
                new ExternalDeliveryView(
                        true
                )
        );

        mockMvc.perform(
                        put(
                                "/api/v1/integration/runtime/external-delivery"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "enabled": true,
                                          "reason": "B8 controlled live EMAIL validation"
                                        }
                                        """
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
                        jsonPath("$.data.enabled")
                                .value(true)
                );

        var command =
                org.mockito.ArgumentCaptor.forClass(
                        SetExternalDeliveryCommand.class
                );

        verify(service)
                .setExternalDelivery(
                        command.capture()
                );

        org.assertj.core.api.Assertions
                .assertThat(
                        command.getValue().enabled()
                )
                .isTrue();

        org.assertj.core.api.Assertions
                .assertThat(
                        command.getValue().reason()
                )
                .isEqualTo(
                        "B8 controlled live EMAIL validation"
                );
    }


    @Test
    void missingRuntimeAuthorityIsForbiddenBeforeService()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "integration.connector.read"
                        )
                );

        mockMvc.perform(
                        put(
                                "/api/v1/integration/runtime/external-delivery"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "enabled": true
                                        }
                                        """
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
                service,
                never()
        ).setExternalDelivery(
                any()
        );
    }


    @Test
    void crossTenantRuntimeRequestIsRejectedBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        principalTenant,
                        Set.of(
                                "integration.runtime.manage"
                        )
                );

        mockMvc.perform(
                        put(
                                "/api/v1/integration/runtime/external-delivery"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "enabled": true
                                        }
                                        """
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
                service,
                never()
        ).setExternalDelivery(
                any()
        );
    }


    @Test
    void unauthenticatedRuntimeMutationIsRejected()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/v1/integration/runtime/external-delivery"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        UUID.randomUUID().toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "enabled": true
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verify(
                service,
                never()
        ).setExternalDelivery(
                any()
        );
    }


    private String token(
            UUID userId,
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        userId,
                        "b8-eip-runtime-http-user",
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
