package africa.growtogether.platform.school.assessment.examination;

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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ExaminationSessionController.class)
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
class ExaminationSessionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ExaminationSessionService service;


    @Test
    void authenticatedCreatorUsesJwtTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.create"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/examination-sessions"
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
                                          "sessionCode": "EX-SEC-001",
                                          "sessionName": "Security Test Session",
                                          "description": "HTTP security verification",
                                          "academicYearId": "%s",
                                          "academicTermId": null,
                                          "campusId": "%s",
                                          "examinationType": "INTERNAL",
                                          "startDate": "2026-09-10",
                                          "endDate": "2026-09-30",
                                          "registrationOpenDate": "2026-09-01",
                                          "registrationCloseDate": "2026-09-09",
                                          "externalAuthority": null,
                                          "externalSessionReference": null,
                                          "workflowInstanceId": null
                                        }
                                        """
                                                .formatted(
                                                        academicYearId,
                                                        campusId
                                                )
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(service).create(
                eq(tenantId),
                any(CreateExaminationSessionCommand.class)
        );
    }


    @Test
    void authenticatedReaderUsesJwtTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/examination-sessions/"
                                        + sessionId
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
                        status().isOk()
                );

        verify(service).get(
                tenantId,
                sessionId
        );
    }


    @Test
    void authenticatedManagerUsesJwtTenantAndActor()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        UUID hostileTenant =
                UUID.randomUUID();

        UUID hostileActor =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/examination-sessions/"
                                        + sessionId
                                        + "/approve"
                        )
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
                                        hostileTenant.toString()
                                )
                                .param(
                                        "approvedBy",
                                        hostileActor.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(service).approve(
                tenantId,
                sessionId,
                userId
        );
    }


    @Test
    void rejectsCreateWithoutCreatePermission()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/examination-sessions"
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
                                          "sessionCode": "EX-DENIED-001",
                                          "sessionName": "Denied",
                                          "academicYearId": "%s",
                                          "campusId": "%s",
                                          "examinationType": "INTERNAL",
                                          "startDate": "2026-09-10",
                                          "endDate": "2026-09-30"
                                        }
                                        """
                                                .formatted(
                                                        academicYearId,
                                                        campusId
                                                )
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                service,
                never()
        ).create(
                any(),
                any()
        );
    }


    @Test
    void rejectsReadWithoutReadPermission()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.create"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/examination-sessions/"
                                        + sessionId
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
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                service,
                never()
        ).get(
                any(),
                any()
        );
    }


    @Test
    void rejectsLifecycleWithoutManagePermission()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/examination-sessions/"
                                        + sessionId
                                        + "/approve"
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
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                service,
                never()
        ).approve(
                any(),
                any(),
                any()
        );
    }


    @Test
    void rejectsCrossTenantRequestBeforeControllerService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID sessionId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/examination-sessions/"
                                        + sessionId
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
                        jsonPath("$.code")
                                .value("GT-TENANT-002")
                );

        verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsAuthenticatedRequestWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID sessionId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/examination-sessions/"
                                        + sessionId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-TENANT-001")
                );

        verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsUnauthenticatedRequest()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID sessionId =
                UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/examination-sessions/"
                                        + sessionId
                        )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-001")
                );

        verifyNoInteractions(
                service
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
                        "examination-candidate-test-user",
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
