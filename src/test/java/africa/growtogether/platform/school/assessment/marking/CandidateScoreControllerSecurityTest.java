package africa.growtogether.platform.school.assessment.marking;


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

import java.math.BigDecimal;
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


@WebMvcTest(CandidateScoreController.class)
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
class CandidateScoreControllerSecurityTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private JwtService jwtService;


    @MockitoBean
    private CandidateScoreService service;


    @Test
    void authenticatedCreatorUsesJwtTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID markSheetId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID enrollmentId =
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
                                "/api/v1/school/candidate-scores"
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
                                          "markSheetId": "%s",
                                          "studentId": "%s",
                                          "studentEnrollmentId": "%s"
                                        }
                                        """
                                                .formatted(
                                                        markSheetId,
                                                        studentId,
                                                        enrollmentId
                                                )
                                )
                )
                .andExpect(
                        status().isOk()
                );


        verify(service).create(
                eq(tenantId),
                eq(
                        new CreateCandidateScoreCommand(
                                markSheetId,
                                null,
                                null,
                                studentId,
                                enrollmentId
                        )
                )
        );
    }


    @Test
    void authenticatedReaderUsesJwtTenantForGet()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID scoreId =
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
                                "/api/v1/school/candidate-scores/{id}",
                                scoreId
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
                scoreId
        );
    }


    @Test
    void authenticatedReaderUsesJwtTenantForMarkSheetStudentLookup()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID markSheetId =
                UUID.randomUUID();

        UUID studentId =
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
                                "/api/v1/school/candidate-scores/mark-sheet/{markSheetId}/student/{studentId}",
                                markSheetId,
                                studentId
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


        verify(service).getByMarkSheetAndStudent(
                tenantId,
                markSheetId,
                studentId
        );
    }


    @Test
    void authenticatedManagerUsesJwtTenantAndActorForScoreEntry()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID scoreId =
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
                                "/api/v1/school/candidate-scores/{id}/score",
                                scoreId
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
                                          "score": 76.50
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                );


        verify(service).enterScore(
                eq(tenantId),
                eq(scoreId),
                eq(new BigDecimal("76.50")),
                eq(userId)
        );
    }


    @Test
    void authenticatedManagerUsesJwtTenantAndActorForAbsent()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID scoreId =
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
                                "/api/v1/school/candidate-scores/{id}/absent",
                                scoreId
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


        verify(service).markAbsent(
                tenantId,
                scoreId,
                userId
        );
    }


    @Test
    void rejectsCreateWithoutCreatePermission()
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
                        post(
                                "/api/v1/school/candidate-scores"
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
                                          "markSheetId": "%s",
                                          "studentId": "%s",
                                          "studentEnrollmentId": "%s"
                                        }
                                        """
                                                .formatted(
                                                        UUID.randomUUID(),
                                                        UUID.randomUUID(),
                                                        UUID.randomUUID()
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
        )
                .create(
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

        UUID scoreId =
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
                                "/api/v1/school/candidate-scores/{id}",
                                scoreId
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
                                .value(
                                        "GT-AUTH-003"
                                )
                );


        verify(
                service,
                never()
        )
                .get(
                        any(),
                        any()
                );
    }


    @Test
    void rejectsScoreEntryWithoutManagePermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID scoreId =
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
                                "/api/v1/school/candidate-scores/{id}/score",
                                scoreId
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
                                          "score": 60
                                        }
                                        """
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
        )
                .enterScore(
                        any(),
                        any(),
                        any(),
                        any()
                );
    }


    @Test
    void rejectsCrossTenantBeforeControllerService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID scoreId =
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
                                "/api/v1/school/candidate-scores/{id}",
                                scoreId
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

        UUID scoreId =
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
                                "/api/v1/school/candidate-scores/{id}",
                                scoreId
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

        UUID scoreId =
                UUID.randomUUID();


        mockMvc.perform(
                        get(
                                "/api/v1/school/candidate-scores/{id}",
                                scoreId
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
                        "candidate-score-test-user",
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
