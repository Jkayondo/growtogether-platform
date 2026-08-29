package africa.growtogether.platform.school.profile;

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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(SchoolProfileController.class)
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
class SchoolProfileControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private SchoolProfileService service;


    @Test
    void unauthenticatedCurrentProfileRequestIsRejected()
            throws Exception {

        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/school/profiles/current")
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .param(
                                        "tenantId",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verify(
                service,
                never()
        ).getForTenant(
                any()
        );
    }


    @Test
    void currentProfileCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        principalTenant
                );

        mockMvc.perform(
                        get("/api/v1/school/profiles/current")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        principalTenant.toString()
                                )
                                .param(
                                        "tenantId",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verify(
                service,
                never()
        ).getForTenant(
                any()
        );
    }


    @Test
    void authenticatedCurrentProfileUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId
                );

        mockMvc.perform(
                        get("/api/v1/school/profiles/current")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .param(
                                        "tenantId",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                service
        ).getForTenant(
                tenantId
        );
    }


    private String token(
            UUID userId,
            UUID tenantId
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        userId,
                        "school-profile-test-user",
                        tenantId,
                        Set.of("SCHOOL_ADMIN"),
                        Set.of(),
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }
}
