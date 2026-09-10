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

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeacherProfileController.class)
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
class TeacherProfileControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TeacherProfileService service;

    @Test
    void createWithoutCreatePermissionIsForbidden() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.academic.teacher-profile.read")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).create(
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any()
        );
    }

    @Test
    void readWithoutReadPermissionIsForbidden() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/school/academic/teachers/TEST")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.teacher-profile.create")
                                        )
                                )
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).findByTeacherNumber(any(), any());
    }

    @Test
    void manageWithoutManagePermissionIsForbidden() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/v1/school/academic/teachers/TEST/activate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.teacher-profile.read")
                                        )
                                )
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).findByTeacherNumber(any(), any());
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
                                        Set.of("school.academic.teacher-profile.create")
                                ),
                                principalTenant
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).create(
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any()
        );
    }

    @Test
    void readCannotCrossAuthenticatedTenantBoundary() throws Exception {
        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/school/academic/teachers/TEST")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                principalTenant,
                                                Set.of("school.academic.teacher-profile.read")
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

        verify(service, never()).findByTeacherNumber(any(), any());
    }

    @Test
    void manageCannotCrossAuthenticatedTenantBoundary() throws Exception {
        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/v1/school/academic/teachers/TEST/activate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                principalTenant,
                                                Set.of("school.academic.teacher-profile.manage")
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

        verify(service, never()).findByTeacherNumber(any(), any());
    }

    @Test
    void authorizedCreateUsesAuthenticatedTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.academic.teacher-profile.create")
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-TEACHER-PROFILE-001")
                );

        verify(service).create(
                eq(tenantId),
                any(UUID.class),
                eq("TEST-TEACHER"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(false),
                eq(false),
                eq(false),
                isNull(),
                isNull()
        );
    }

    @Test
    void authorizedReadUsesAuthenticatedTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/school/academic/teachers/TEST")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.teacher-profile.read")
                                        )
                                )
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-TEACHER-PROFILE-002")
                );

        verify(service).findByTeacherNumber(
                tenantId,
                "TEST"
        );
    }

    @Test
    void authorizedManageUsesAuthenticatedTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();

        TeacherProfile profile = mock(TeacherProfile.class);

        when(
                service.findByTeacherNumber(
                        tenantId,
                        "TEST"
                )
        ).thenReturn(profile);

        mockMvc.perform(
                        patch("/api/v1/school/academic/teachers/TEST/activate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(
                                                tenantId,
                                                Set.of("school.academic.teacher-profile.manage")
                                        )
                                )
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-TEACHER-PROFILE-006")
                );

        verify(service).findByTeacherNumber(
                tenantId,
                "TEST"
        );

        verify(service).activate(profile);
    }

    @Test
    void unauthenticatedTeacherProfileRequestIsRejected() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/school/academic/teachers/TEST")
                                .header("X-Tenant-ID", tenantId.toString())
                                .param("tenantId", tenantId.toString())
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("GT-AUTH-001"));

        verify(service, never()).findByTeacherNumber(any(), any());
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
        return post("/api/v1/school/academic/teachers")
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
                        "workforceMemberId",
                        UUID.randomUUID().toString()
                )
                .param(
                        "teacherNumber",
                        "TEST-TEACHER"
                );
    }

    private String token(
            UUID tenantId,
            Set<String> permissions
    ) {
        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "teacher-profile-test-user",
                        tenantId,
                        Set.of("SCHOOL_ADMIN"),
                        permissions,
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(principal);
    }
}
