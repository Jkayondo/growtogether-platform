package africa.growtogether.platform.school.academic.teaching;

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

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeachingAssignmentController.class)
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
class TeachingAssignmentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TeachingAssignmentService service;

    @Test
    void createWithoutCreatePermissionIsForbidden() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.academic.teaching-assignment.read")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).create(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                anyInt(), any(), any(), any(), any()
        );
    }

    @Test
    void readWithoutReadPermissionIsForbidden() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/teaching-assignments/{assignmentId}",
                                assignmentId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.teaching-assignment.create")
                                        )
                                )
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).findById(any(), any());
    }

    @Test
    void manageWithoutManagePermissionIsForbidden() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/v1/school/academic/teaching-assignments/{assignmentId}/suspend",
                                assignmentId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.teaching-assignment.read")
                                        )
                                )
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).suspend(any(), any());
    }

    @Test
    void createCannotCrossAuthenticatedTenantBoundary() throws Exception {
        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                requestedTenant,
                                token(
                                        principalTenant,
                                        Set.of("school.academic.teaching-assignment.create")
                                ),
                                principalTenant
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).create(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                anyInt(), any(), any(), any(), any()
        );
    }

    @Test
    void readCannotCrossAuthenticatedTenantBoundary() throws Exception {
        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/teaching-assignments/{assignmentId}",
                                assignmentId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                principalTenant,
                                                Set.of("school.academic.teaching-assignment.read")
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
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).findById(any(), any());
    }

    @Test
    void manageCannotCrossAuthenticatedTenantBoundary() throws Exception {
        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/v1/school/academic/teaching-assignments/{assignmentId}/suspend",
                                assignmentId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                principalTenant,
                                                Set.of("school.academic.teaching-assignment.manage")
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
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).suspend(any(), any());
    }

    @Test
    void authorizedCreateUsesAuthenticatedTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.academic.teaching-assignment.create")
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-TEACHING-ASSIGNMENT-001")
                );

        verify(service).create(
                eq(tenantId),
                eq("TEST-ASSIGNMENT"),
                any(UUID.class),
                any(UUID.class),
                isNull(),
                any(UUID.class),
                any(UUID.class),
                isNull(),
                any(UUID.class),
                isNull(),
                eq(5),
                isNull(),
                eq(LocalDate.of(2026, 1, 1)),
                isNull(),
                isNull()
        );
    }

    @Test
    void authorizedReadUsesAuthenticatedTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/teaching-assignments/{assignmentId}",
                                assignmentId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.teaching-assignment.read")
                                        )
                                )
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-TEACHING-ASSIGNMENT-002")
                );

        verify(service).findById(
                tenantId,
                assignmentId
        );
    }

    @Test
    void authorizedManageUsesAuthenticatedTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/v1/school/academic/teaching-assignments/{assignmentId}/suspend",
                                assignmentId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.teaching-assignment.manage")
                                        )
                                )
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-TEACHING-ASSIGNMENT-012")
                );

        verify(service).suspend(
                tenantId,
                assignmentId
        );
    }

    @Test
    void unauthenticatedTeachingAssignmentRequestIsRejected() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/teaching-assignments/{assignmentId}",
                                assignmentId
                        )
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("GT-AUTH-001"));

        verify(service, never()).findById(any(), any());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
    createRequest(
            UUID requestedTenant,
            String token
    ) {
        return createRequest(
                requestedTenant,
                token,
                requestedTenant
        );
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
    createRequest(
            UUID requestedTenant,
            String token,
            UUID headerTenant
    ) {
        return post("/api/v1/school/academic/teaching-assignments")
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
                        "assignmentReference",
                        "TEST-ASSIGNMENT"
                )
                .param(
                        "teacherProfileId",
                        UUID.randomUUID().toString()
                )
                .param(
                        "academicYearId",
                        UUID.randomUUID().toString()
                )
                .param(
                        "campusId",
                        UUID.randomUUID().toString()
                )
                .param(
                        "classGradeId",
                        UUID.randomUUID().toString()
                )
                .param(
                        "subjectId",
                        UUID.randomUUID().toString()
                )
                .param(
                        "weeklyPeriods",
                        "5"
                )
                .param(
                        "effectiveFrom",
                        "2026-01-01"
                );
    }

    private String token(
            UUID tenantId,
            Set<String> permissions
    ) {
        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "teaching-assignment-test-user",
                        tenantId,
                        Set.of("SCHOOL_ADMIN"),
                        permissions,
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(principal);
    }
}
