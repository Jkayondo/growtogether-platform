package africa.growtogether.platform.eip;

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

import static africa.growtogether.platform.eip.EipGatewayDtos.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EipGatewayController.class)
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
@TestPropertySource(properties = {
    "gt.security.jwt.issuer=gt-test",
    "gt.security.jwt.secret=01234567890123456789012345678901",
    "gt.security.jwt.access-token-seconds=300"
})
class EipGatewayControllerCredentialRotationSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockitoBean private EipGatewayService service;

    @Test
    void connectorAdministratorCanRotateCredential() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID connectorId = UUID.randomUUID();
        String token = token(UUID.randomUUID(), tenantId, Set.of("integration.connector.manage"));
        when(service.rotateConnectorCredential(eq(connectorId), any(CredentialRotationCommand.class)))
                .thenReturn(new ConnectorView(connectorId,"BREVO_PRIMARY_EMAIL","BREVO_EMAIL","https://api.brevo.com","API_KEY","{\"senderName\":\"GrowTogether\"}",true));

        mockMvc.perform(patch("/api/v1/integration/connectors/{connectorId}/credential", connectorId)
                .header("Authorization","Bearer "+ token)
                .header("X-Tenant-ID",tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"credential\":\"replacement-provider-secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.connectorCode").value("BREVO_PRIMARY_EMAIL"))
                .andExpect(jsonPath("$.data.credential").doesNotExist())
                .andExpect(jsonPath("$.data.credentialCiphertext").doesNotExist())
                .andExpect(jsonPath("$.data.credentialKeyId").doesNotExist());

        verify(service).rotateConnectorCredential(eq(connectorId), any(CredentialRotationCommand.class));
    }

    @Test
    void missingConnectorManageAuthorityIsForbiddenBeforeService() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID connectorId = UUID.randomUUID();
        String token = token(UUID.randomUUID(), tenantId, Set.of("integration.connector.read"));

        mockMvc.perform(patch("/api/v1/integration/connectors/{connectorId}/credential", connectorId)
                .header("Authorization","Bearer "+ token)
                .header("X-Tenant-ID",tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"credential\":\"replacement\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service,never()).rotateConnectorCredential(any(),any());
    }

    @Test
    void crossTenantRotationIsRejectedBeforeService() throws Exception {
        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID connectorId = UUID.randomUUID();
        String token = token(UUID.randomUUID(), principalTenant, Set.of("integration.connector.manage"));

        mockMvc.perform(patch("/api/v1/integration/connectors/{connectorId}/credential", connectorId)
                .header("Authorization","Bearer "+ token)
                .header("X-Tenant-ID",requestedTenant.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"credential\":\"replacement\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-TENANT-002"));

        verify(service,never()).rotateConnectorCredential(any(),any());
    }

    @Test
    void unauthenticatedRotationIsRejected() throws Exception {
        mockMvc.perform(patch("/api/v1/integration/connectors/{connectorId}/credential", UUID.randomUUID())
                .header("X-Tenant-ID",UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"credential\":\"replacement\"}"))
                .andExpect(status().isUnauthorized());

        verify(service,never()).rotateConnectorCredential(any(),any());
    }

    private String token(UUID userId,UUID tenantId,Set<String> permissions) {
        GtPrincipal principal = new GtPrincipal(
                userId,
                "b8-eip-connector-http-user",
                tenantId,
                Set.of("INTEGRATION_ADMIN"),
                permissions,
                UUID.randomUUID()
        );
        return jwtService.issueAccessToken(principal);
    }
}
