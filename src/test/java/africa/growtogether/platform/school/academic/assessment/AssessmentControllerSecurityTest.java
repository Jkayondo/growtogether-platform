package africa.growtogether.platform.school.academic.assessment;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AssessmentController.class)
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
class AssessmentControllerSecurityTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AssessmentService service;


    @Test
    void createWithoutCreatePermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                learningOutcomeId,
                                token(
                                        tenantId,
                                        Set.of("school.academic.assessment.read")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                service,
                never()
        ).create(
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void readWithoutReadPermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments",
                                learningOutcomeId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.assessment.create")
                                        )
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
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                service,
                never()
        ).findByLearningOutcome(
                any(),
                any()
        );
    }


    @Test
    void manageWithoutManagePermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments/TEST/archive",
                                learningOutcomeId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.assessment.read")
                                        )
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
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                service,
                never()
        ).findByCode(
                any(),
                any(),
                any()
        );
    }


    @Test
    void createCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                requestedTenant,
                                learningOutcomeId,
                                token(
                                        principalTenant,
                                        Set.of("school.academic.assessment.create")
                                ),
                                principalTenant
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                service,
                never()
        ).create(
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void readCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments",
                                learningOutcomeId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                principalTenant,
                                                Set.of("school.academic.assessment.read")
                                        )
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
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                service,
                never()
        ).findByLearningOutcome(
                any(),
                any()
        );
    }


    @Test
    void manageCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments/TEST/archive",
                                learningOutcomeId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                principalTenant,
                                                Set.of("school.academic.assessment.manage")
                                        )
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
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-003")
                );

        verify(
                service,
                never()
        ).findByCode(
                any(),
                any(),
                any()
        );
    }


    @Test
    void authorizedCreateUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                learningOutcomeId,
                                token(
                                        tenantId,
                                        Set.of("school.academic.assessment.create")
                                )
                        )
                )
                .andExpect(status().isOk());

        verify(service).create(
                eq(tenantId),
                eq(learningOutcomeId),
                eq("TEST-ASSESSMENT"),
                eq("Test Assessment")
        );
    }


    @Test
    void authorizedReadUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments",
                                learningOutcomeId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.assessment.read")
                                        )
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
                .andExpect(status().isOk());

        verify(service).findByLearningOutcome(
                tenantId,
                learningOutcomeId
        );
    }


    @Test
    void authorizedManageUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        Assessment assessment =
                mock(Assessment.class);

        when(
                service.findByCode(
                        tenantId,
                        learningOutcomeId,
                        "TEST"
                )
        ).thenReturn(assessment);

        mockMvc.perform(
                        patch(
                                "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments/TEST/archive",
                                learningOutcomeId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.assessment.manage")
                                        )
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
                .andExpect(status().isOk());

        verify(service).findByCode(
                tenantId,
                learningOutcomeId,
                "TEST"
        );

        verify(service).archive(
                assessment
        );
    }


    @Test
    void unauthenticatedAssessmentRequestIsRejected()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments",
                                learningOutcomeId
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
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-001")
                );

        verify(
                service,
                never()
        ).findByLearningOutcome(
                any(),
                any()
        );
    }


    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
    createRequest(
            UUID requestedTenant,
            UUID learningOutcomeId,
            String token
    ) {

        return createRequest(
                requestedTenant,
                learningOutcomeId,
                token,
                requestedTenant
        );
    }


    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
    createRequest(
            UUID requestedTenant,
            UUID learningOutcomeId,
            String token,
            UUID headerTenant
    ) {

        return post(
                "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments",
                learningOutcomeId
        )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .header(
                        "X-Tenant-ID",
                        headerTenant.toString()
                )
                .param(
                        "tenantId",
                        requestedTenant.toString()
                )
                .param(
                        "assessmentCode",
                        "TEST-ASSESSMENT"
                )
                .param(
                        "assessmentTitle",
                        "Test Assessment"
                );
    }


    private String token(
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "assessment-test-user",
                        tenantId,
                        Set.of("SCHOOL_ADMIN"),
                        permissions,
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }
}
