package africa.growtogether.platform.school.student;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(StudentController.class)
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
class StudentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private StudentService service;


    @Test
    void createWithoutCreatePermissionIsForbidden() throws Exception {

        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.student.read")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).create(
                any(),
                any(CreateStudentCommand.class)
        );
    }


    @Test
    void readWithoutReadPermissionIsForbidden() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        mockMvc.perform(
                        readRequest(
                                studentId,
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.student.create")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).get(any(), any());
    }


    @Test
    void createCannotCrossAuthenticatedTenantBoundary() throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                requestedTenant,
                                principalTenant,
                                token(
                                        principalTenant,
                                        Set.of("school.student.create")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).create(
                any(),
                any(CreateStudentCommand.class)
        );
    }


    @Test
    void readCannotCrossAuthenticatedTenantBoundary() throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        mockMvc.perform(
                        readRequest(
                                studentId,
                                requestedTenant,
                                principalTenant,
                                token(
                                        principalTenant,
                                        Set.of("school.student.read")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).get(any(), any());
    }


    @Test
    void authorizedCreateUsesAuthenticatedTenant() throws Exception {

        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.student.create")
                                )
                        )
                )
                .andExpect(status().isOk());

        verify(service).create(
                eq(tenantId),
                any(CreateStudentCommand.class)
        );
    }


    @Test
    void authorizedReadUsesAuthenticatedTenant() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        mockMvc.perform(
                        readRequest(
                                studentId,
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.student.read")
                                )
                        )
                )
                .andExpect(status().isOk());

        verify(service).get(
                tenantId,
                studentId
        );
    }


    @Test
    void listWithoutReadPermissionIsForbidden() throws Exception {

        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        listRequest(
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.student.create")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                service,
                never()
        ).findActiveStudents(
                any()
        );
    }


    @Test
    void listCannotCrossAuthenticatedTenantBoundary() throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        mockMvc.perform(
                        listRequest(
                                requestedTenant,
                                principalTenant,
                                token(
                                        principalTenant,
                                        Set.of("school.student.read")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                service,
                never()
        ).findActiveStudents(
                any()
        );
    }


    @Test
    void authorizedListUsesAuthenticatedTenant() throws Exception {

        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        listRequest(
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.student.read")
                                )
                        )
                )
                .andExpect(status().isOk());

        verify(
                service
        ).findActiveStudents(
                eq(tenantId)
        );
    }


    @Test
    void unauthenticatedStudentListIsRejected() throws Exception {

        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/school/students")
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
                .andExpect(jsonPath("$.code").value("GT-AUTH-001"));

        verify(
                service,
                never()
        ).findActiveStudents(
                any()
        );
    }


    @Test
    void unauthenticatedStudentRequestIsRejected() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/students/{id}",
                                studentId
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
                .andExpect(jsonPath("$.code").value("GT-AUTH-001"));

        verify(service, never()).get(any(), any());
    }


    private MockHttpServletRequestBuilder listRequest(
            UUID requestedTenant,
            UUID headerTenant,
            String token
    ) {

        return get("/api/v1/school/students")
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
                );
    }


    private MockHttpServletRequestBuilder createRequest(
            UUID requestedTenant,
            UUID headerTenant,
            String token
    ) {

        return post("/api/v1/school/students")
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
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "studentNumber": "E2E-STUDENT-001",
                          "permanentLearnerNumber": "E2E-PLN-001",
                          "firstName": "Test",
                          "lastName": "Learner",
                          "dateOfBirth": "2015-01-01"
                        }
                        """);
    }


    private MockHttpServletRequestBuilder readRequest(
            UUID studentId,
            UUID requestedTenant,
            UUID headerTenant,
            String token
    ) {

        return get(
                "/api/v1/school/students/{id}",
                studentId
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
                );
    }


    private String token(
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "student-security-test-user",
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
