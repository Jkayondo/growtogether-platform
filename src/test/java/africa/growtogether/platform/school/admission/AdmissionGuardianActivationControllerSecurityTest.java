package africa.growtogether.platform.school.admission;

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
import africa.growtogether.platform.ens.NotificationChannel;

import java.time.Instant;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AdmissionGuardianActivationController.class)
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
class AdmissionGuardianActivationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AdmissionGuardianActivationNotificationService service;


    @Test
    void authorisedSchoolAdministratorCanCreateSecureActivationNotification()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID provisioningId =
                UUID.randomUUID();

        UUID invitationId =
                UUID.randomUUID();

        UUID notificationId =
                UUID.randomUUID();

        Instant expiresAt =
                Instant.now().plusSeconds(900);

        String correlationId =
                "B8-ACTIVATION-HTTP-001";

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.admission.parent-activation.manage"
                        )
                );

        when(
                service.provisionAndNotify(
                        guardianId
                )
        ).thenReturn(
                new AdmissionGuardianActivationNotificationResult(
                        provisioningId,
                        guardianId,
                        invitationId,
                        notificationId,
                        NotificationChannel.EMAIL,
                        expiresAt
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-guardians/"
                                        + guardianId
                                        + "/parent-activation/notifications"
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

                                /*
                                 * Hostile legacy-style identifiers are
                                 * deliberately not controller inputs.
                                 */
                                .param(
                                        "tenantId",
                                        UUID.randomUUID().toString()
                                )
                                .param(
                                        "actorId",
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
                                .value(
                                        "GT-SCHOOL-ADMISSION-PARENT-ACT-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.admissionGuardianId")
                                .value(
                                        guardianId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.notificationRequestId")
                                .value(
                                        notificationId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.channel")
                                .value("EMAIL")
                )
                .andExpect(
                        header()
                                .string(
                                        "X-Correlation-ID",
                                        correlationId
                                )
                );

        verify(service)
                .provisionAndNotify(
                        guardianId
                );
    }


    @Test
    void requestWithoutActivationAuthorityIsForbiddenBeforeService()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.admission.payment.read"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-guardians/"
                                        + guardianId
                                        + "/parent-activation/notifications"
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
                service,
                never()
        ).provisionAndNotify(
                any()
        );
    }


    @Test
    void crossTenantActivationRequestIsRejectedBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        principalTenant,
                        Set.of(
                                "school.admission.parent-activation.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-guardians/"
                                        + guardianId
                                        + "/parent-activation/notifications"
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
                service,
                never()
        ).provisionAndNotify(
                any()
        );
    }


    @Test
    void unauthenticatedActivationRequestIsRejected()
            throws Exception {

        UUID guardianId =
                UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-guardians/"
                                        + guardianId
                                        + "/parent-activation/notifications"
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
                service,
                never()
        ).provisionAndNotify(
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
                        "b8-activation-http-user",
                        tenantId,
                        Set.of(
                                "SCHOOL_ADMIN"
                        ),
                        permissions,
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }
}
