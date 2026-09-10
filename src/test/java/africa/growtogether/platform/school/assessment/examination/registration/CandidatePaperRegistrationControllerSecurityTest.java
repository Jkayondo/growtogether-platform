package africa.growtogether.platform.school.assessment.examination.registration;

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


@WebMvcTest(CandidatePaperRegistrationController.class)
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
class CandidatePaperRegistrationControllerSecurityTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private JwtService jwtService;


    @MockitoBean
    private CandidatePaperRegistrationService service;


    @Test
    void authenticatedCreatorUsesJwtTenantAndActor()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
                UUID.randomUUID();

        UUID scheduleId =
                UUID.randomUUID();


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
                                "/api/v1/school/candidate-paper-registrations"
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
                                          "examinationCandidateId": "%s",
                                          "assessmentPaperId": "%s",
                                          "examinationScheduleId": "%s",
                                          "registrationType": "STANDARD"
                                        }
                                        """
                                                .formatted(
                                                        candidateId,
                                                        paperId,
                                                        scheduleId
                                                )
                                )
                )
                .andExpect(
                        status().isOk()
                );


        verify(
                service
        ).register(
                eq(tenantId),
                eq(userId),
                any(
                        CreateCandidatePaperRegistrationCommand.class
                )
        );
    }


    @Test
    void authenticatedReaderUsesJwtTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
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
                                "/api/v1/school/candidate-paper-registrations"
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
                                        "examinationCandidateId",
                                        candidateId.toString()
                                )
                                .param(
                                        "assessmentPaperId",
                                        paperId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );


        verify(
                service
        ).get(
                tenantId,
                candidateId,
                paperId
        );
    }


    @Test
    void authenticatedManagerUsesJwtTenantForVerify()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
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
                                "/api/v1/school/candidate-paper-registrations/verify"
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
                                        "examinationCandidateId",
                                        candidateId.toString()
                                )
                                .param(
                                        "assessmentPaperId",
                                        paperId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );


        verify(
                service
        ).verify(
                tenantId,
                candidateId,
                paperId
        );
    }


    @Test
    void rejectsRegisterWithoutCreatePermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
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
                        post(
                                "/api/v1/school/candidate-paper-registrations"
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
                                          "examinationCandidateId": "%s",
                                          "assessmentPaperId": "%s",
                                          "examinationScheduleId": null,
                                          "registrationType": "STANDARD"
                                        }
                                        """
                                                .formatted(
                                                        candidateId,
                                                        paperId
                                                )
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );


        verify(
                service,
                never()
        ).register(
                any(),
                any(),
                any()
        );
    }


    @Test
    void rejectsReadWithoutReadPermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
                UUID.randomUUID();


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
                                "/api/v1/school/candidate-paper-registrations"
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
                                        "examinationCandidateId",
                                        candidateId.toString()
                                )
                                .param(
                                        "assessmentPaperId",
                                        paperId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );


        verify(
                service,
                never()
        ).get(
                any(),
                any(),
                any()
        );
    }


    @Test
    void rejectsVerifyWithoutManagePermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
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
                        post(
                                "/api/v1/school/candidate-paper-registrations/verify"
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
                                        "examinationCandidateId",
                                        candidateId.toString()
                                )
                                .param(
                                        "assessmentPaperId",
                                        paperId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );


        verify(
                service,
                never()
        ).verify(
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

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
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
                                "/api/v1/school/candidate-paper-registrations"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                                .param(
                                        "examinationCandidateId",
                                        candidateId.toString()
                                )
                                .param(
                                        "assessmentPaperId",
                                        paperId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-TENANT-002"
                                )
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

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
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
                                "/api/v1/school/candidate-paper-registrations"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .param(
                                        "examinationCandidateId",
                                        candidateId.toString()
                                )
                                .param(
                                        "assessmentPaperId",
                                        paperId.toString()
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-TENANT-001"
                                )
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

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
                UUID.randomUUID();


        mockMvc.perform(
                        get(
                                "/api/v1/school/candidate-paper-registrations"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .param(
                                        "examinationCandidateId",
                                        candidateId.toString()
                                )
                                .param(
                                        "assessmentPaperId",
                                        paperId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-001"
                                )
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
                        "candidate-paper-registration-test-user",
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
