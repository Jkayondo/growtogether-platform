package africa.growtogether.platform.school.assessment.examination.candidate;

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


@WebMvcTest(ExaminationCandidateController.class)
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
class ExaminationCandidateControllerSecurityTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private JwtService jwtService;


    @MockitoBean
    private ExaminationCandidateService service;


    @Test
    void authenticatedCreatorUsesJwtTenantForCandidateRegistration()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID examinationSessionId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID studentEnrollmentId =
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
                                "/api/v1/school/examination-candidates"
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
                                          "candidateNumber": "CAND-HTTP-001",
                                          "examinationSessionId": "%s",
                                          "studentId": "%s",
                                          "studentEnrollmentId": "%s"
                                        }
                                        """
                                                .formatted(
                                                        examinationSessionId,
                                                        studentId,
                                                        studentEnrollmentId
                                                )
                                )
                )
                .andExpect(
                        status().isOk()
                );


        verify(service).register(
                eq(tenantId),
                eq("CAND-HTTP-001"),
                eq(examinationSessionId),
                eq(studentId),
                eq(studentEnrollmentId)
        );
    }


    @Test
    void authenticatedReaderUsesJwtTenantForCandidateGet()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
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
                                "/api/v1/school/examination-candidates/CAND-HTTP-002"
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
                "CAND-HTTP-002"
        );
    }


    @Test
    void rejectsCandidateRegistrationWithoutCreatePermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID examinationSessionId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID studentEnrollmentId =
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
                                "/api/v1/school/examination-candidates"
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
                                          "candidateNumber": "CAND-NO-CREATE",
                                          "examinationSessionId": "%s",
                                          "studentId": "%s",
                                          "studentEnrollmentId": "%s"
                                        }
                                        """
                                                .formatted(
                                                        examinationSessionId,
                                                        studentId,
                                                        studentEnrollmentId
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
        ).register(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void rejectsCandidateReadWithoutReadPermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
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
                                "/api/v1/school/examination-candidates/CAND-NO-READ"
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
    void rejectsCrossTenantCandidateRequestBeforeControllerService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
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
                                "/api/v1/school/examination-candidates/CAND-CROSS"
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
    void rejectsAuthenticatedCandidateRequestWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
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
                                "/api/v1/school/examination-candidates/CAND-NO-TENANT"
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
    void rejectsUnauthenticatedCandidateRequest()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();


        mockMvc.perform(
                        get(
                                "/api/v1/school/examination-candidates/CAND-UNAUTH"
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
