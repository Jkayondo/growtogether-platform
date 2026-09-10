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

@WebMvcTest(CurrentTeacherAssignmentController.class)
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
class CurrentTeacherAssignmentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TeachingAssignmentService service;


    @MockitoBean
    private africa.growtogether.platform.connect.ConnectTeacherAuthorizationService teachers;

    private static final String ENDPOINT =
            "/api/v1/school/academic/teaching-assignments/me/active";

    @Test
    void authenticatedTeacherReceivesOnlyActiveAssignments() throws Exception {
        UUID tenant = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        TeacherProfile teacher = org.mockito.Mockito.mock(TeacherProfile.class);
        TeachingAssignment active = org.mockito.Mockito.mock(TeachingAssignment.class);
        TeachingAssignment suspended = org.mockito.Mockito.mock(TeachingAssignment.class);

        org.mockito.Mockito.when(teacher.getId()).thenReturn(teacherId);
        org.mockito.Mockito.when(teachers.requireUniqueCurrentTeacherProfile())
                .thenReturn(teacher);
        org.mockito.Mockito.when(active.getAssignmentStatus()).thenReturn("ACTIVE");
        org.mockito.Mockito.when(suspended.getAssignmentStatus()).thenReturn("SUSPENDED");
        org.mockito.Mockito.when(service.findByTeacher(tenant, teacherId))
                .thenReturn(java.util.List.of(active, suspended));

        mockMvc.perform(get(ENDPOINT)
                .header("Authorization", "Bearer " + token(tenant,
                        Set.of("school.academic.teaching-assignment.read")))
                .header("X-Tenant-ID", tenant.toString())
                .param("teacherProfileId", UUID.randomUUID().toString())
                .param("tenantId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].assignmentStatus").value("ACTIVE"));

        verify(service).findByTeacher(tenant, teacherId);
        verify(service, never()).findByStatus(any(), any());
    }

    @Test
    void missingPermissionCannotResolveTeacher() throws Exception {
        UUID tenant = UUID.randomUUID();
        mockMvc.perform(get(ENDPOINT)
                .header("Authorization", "Bearer " + token(tenant, Set.of()))
                .header("X-Tenant-ID", tenant.toString()))
                .andExpect(status().isForbidden());
        org.mockito.Mockito.verifyNoInteractions(teachers, service);
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isUnauthorized());
        org.mockito.Mockito.verifyNoInteractions(teachers, service);
    }

    @Test
    void crossTenantHeaderIsRejected() throws Exception {
        UUID tenant = UUID.randomUUID();
        mockMvc.perform(get(ENDPOINT)
                .header("Authorization", "Bearer " + token(tenant,
                        Set.of("school.academic.teaching-assignment.read")))
                .header("X-Tenant-ID", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
        org.mockito.Mockito.verifyNoInteractions(teachers, service);
    }

    @Test
    void unresolvedTeacherCannotReadAssignments() throws Exception {
        UUID tenant = UUID.randomUUID();
        org.mockito.Mockito.when(teachers.requireUniqueCurrentTeacherProfile())
                .thenThrow(new org.springframework.security.access.AccessDeniedException(
                        "Teacher mapping unavailable"));

        mockMvc.perform(get(ENDPOINT)
                .header("Authorization", "Bearer " + token(tenant,
                        Set.of("school.academic.teaching-assignment.read")))
                .header("X-Tenant-ID", tenant.toString()))
                .andExpect(status().isForbidden());
        org.mockito.Mockito.verifyNoInteractions(service);
    }

    @Test
    void resolvedTeacherWithNoAssignmentsReceivesEmptyList() throws Exception {
        UUID tenant = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        TeacherProfile teacher = org.mockito.Mockito.mock(TeacherProfile.class);
        org.mockito.Mockito.when(teacher.getId()).thenReturn(teacherId);
        org.mockito.Mockito.when(teachers.requireUniqueCurrentTeacherProfile())
                .thenReturn(teacher);
        org.mockito.Mockito.when(service.findByTeacher(tenant, teacherId))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get(ENDPOINT)
                .header("Authorization", "Bearer " + token(tenant,
                        Set.of("school.academic.teaching-assignment.read")))
                .header("X-Tenant-ID", tenant.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
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
