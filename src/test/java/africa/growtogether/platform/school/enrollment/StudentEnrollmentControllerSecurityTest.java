package africa.growtogether.platform.school.enrollment;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(StudentEnrollmentController.class)
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
class StudentEnrollmentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private StudentEnrollmentService service;


    @Test
    void createWithoutCreatePermissionIsForbidden() throws Exception {

        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.enrollment.read")
                                ),
                                tenantId
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).create(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any()
        );
    }


    @Test
    void readWithoutReadPermissionIsForbidden() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();

        mockMvc.perform(
                        tenantRequest(
                                get(
                                        "/api/v1/school/enrollments/{enrollmentId}",
                                        enrollmentId
                                ),
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.enrollment.create")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).findById(any(), any());
    }


    @Test
    void manageWithoutManagePermissionIsForbidden() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();

        mockMvc.perform(
                        tenantRequest(
                                patch(
                                        "/api/v1/school/enrollments/{enrollmentId}/suspend",
                                        enrollmentId
                                ),
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.enrollment.read")
                                )
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).suspend(any(), any());
    }


    @Test
    void everyEndpointRejectsCrossTenantRequest() throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        UUID enrollmentId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        String createToken =
                token(
                        principalTenant,
                        Set.of("school.enrollment.create")
                );

        String readToken =
                token(
                        principalTenant,
                        Set.of("school.enrollment.read")
                );

        String manageToken =
                token(
                        principalTenant,
                        Set.of("school.enrollment.manage")
                );


        expectForbidden(
                createRequest(
                        requestedTenant,
                        createToken,
                        principalTenant
                )
        );


        expectForbidden(
                tenantRequest(
                        get(
                                "/api/v1/school/enrollments/{enrollmentId}",
                                enrollmentId
                        ),
                        requestedTenant,
                        principalTenant,
                        readToken
                )
        );


        expectForbidden(
                tenantRequest(
                        get(
                                "/api/v1/school/enrollments/number/{enrollmentNumber}",
                                "ENR-SEC-001"
                        ),
                        requestedTenant,
                        principalTenant,
                        readToken
                )
        );


        expectForbidden(
                tenantRequest(
                        get(
                                "/api/v1/school/enrollments/student/{studentId}",
                                studentId
                        ),
                        requestedTenant,
                        principalTenant,
                        readToken
                )
        );


        expectForbidden(
                tenantRequest(
                        get(
                                "/api/v1/school/enrollments/academic-year/{academicYearId}",
                                academicYearId
                        ),
                        requestedTenant,
                        principalTenant,
                        readToken
                )
        );


        expectForbidden(
                tenantRequest(
                        get(
                                "/api/v1/school/enrollments/campus/{campusId}",
                                campusId
                        ),
                        requestedTenant,
                        principalTenant,
                        readToken
                )
        );


        expectForbidden(
                tenantRequest(
                        get(
                                "/api/v1/school/enrollments/class-grade/{classGradeId}",
                                classGradeId
                        ),
                        requestedTenant,
                        principalTenant,
                        readToken
                )
        );


        expectForbidden(
                tenantRequest(
                        get(
                                "/api/v1/school/enrollments/stream/{streamId}",
                                streamId
                        ),
                        requestedTenant,
                        principalTenant,
                        readToken
                )
        );


        expectForbidden(
                tenantRequest(
                        get(
                                "/api/v1/school/enrollments/status/{enrollmentStatus}",
                                "ACTIVE"
                        ),
                        requestedTenant,
                        principalTenant,
                        readToken
                )
        );


        expectForbidden(
                tenantRequest(
                        patch(
                                "/api/v1/school/enrollments/{enrollmentId}/pending",
                                enrollmentId
                        ),
                        requestedTenant,
                        principalTenant,
                        manageToken
                )
        );


        expectForbidden(
                tenantRequest(
                        patch(
                                "/api/v1/school/enrollments/{enrollmentId}/activate",
                                enrollmentId
                        )
                                .param(
                                        "approvedBy",
                                        UUID.randomUUID().toString()
                                ),
                        requestedTenant,
                        principalTenant,
                        manageToken
                )
        );


        expectForbidden(
                tenantRequest(
                        patch(
                                "/api/v1/school/enrollments/{enrollmentId}/suspend",
                                enrollmentId
                        ),
                        requestedTenant,
                        principalTenant,
                        manageToken
                )
        );


        expectForbidden(
                tenantRequest(
                        patch(
                                "/api/v1/school/enrollments/{enrollmentId}/complete",
                                enrollmentId
                        ),
                        requestedTenant,
                        principalTenant,
                        manageToken
                )
        );


        expectForbidden(
                tenantRequest(
                        patch(
                                "/api/v1/school/enrollments/{enrollmentId}/withdraw",
                                enrollmentId
                        )
                                .param(
                                        "exitDate",
                                        "2026-09-01"
                                ),
                        requestedTenant,
                        principalTenant,
                        manageToken
                )
        );


        expectForbidden(
                tenantRequest(
                        patch(
                                "/api/v1/school/enrollments/{enrollmentId}/transfer",
                                enrollmentId
                        )
                                .param(
                                        "exitDate",
                                        "2026-09-01"
                                ),
                        requestedTenant,
                        principalTenant,
                        manageToken
                )
        );


        expectForbidden(
                tenantRequest(
                        patch(
                                "/api/v1/school/enrollments/{enrollmentId}/cancel",
                                enrollmentId
                        ),
                        requestedTenant,
                        principalTenant,
                        manageToken
                )
        );


        verifyNoInteractions(service);
    }


    @Test
    void authorizedCreateUsesAuthenticatedTenant() throws Exception {

        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        createRequest(
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.enrollment.create")
                                ),
                                tenantId
                        )
                )
                .andExpect(status().isOk());

        verify(service).create(
                eq(tenantId),
                any(UUID.class),
                any(UUID.class),
                isNull(),
                any(UUID.class),
                any(UUID.class),
                isNull(),
                eq("TEST-ENROLLMENT"),
                eq(LocalDate.of(2026, 9, 1)),
                eq(LocalDate.of(2026, 9, 1)),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void authorizedReadUsesAuthenticatedTenant() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();

        mockMvc.perform(
                        tenantRequest(
                                get(
                                        "/api/v1/school/enrollments/{enrollmentId}",
                                        enrollmentId
                                ),
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.enrollment.read")
                                )
                        )
                )
                .andExpect(status().isOk());

        verify(service).findById(
                tenantId,
                enrollmentId
        );
    }


    @Test
    void authorizedManageUsesAuthenticatedTenant() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();

        mockMvc.perform(
                        tenantRequest(
                                patch(
                                        "/api/v1/school/enrollments/{enrollmentId}/suspend",
                                        enrollmentId
                                ),
                                tenantId,
                                tenantId,
                                token(
                                        tenantId,
                                        Set.of("school.enrollment.manage")
                                )
                        )
                )
                .andExpect(status().isOk());

        verify(service).suspend(
                tenantId,
                enrollmentId
        );
    }


    @Test
    void unauthenticatedEnrollmentRequestIsRejected() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/enrollments/{enrollmentId}",
                                enrollmentId
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

        verify(service, never()).findById(any(), any());
    }


    private void expectForbidden(
            MockHttpServletRequestBuilder request
    ) throws Exception {

        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));
    }


    private MockHttpServletRequestBuilder tenantRequest(
            MockHttpServletRequestBuilder request,
            UUID requestedTenant,
            UUID headerTenant,
            String token
    ) {

        return request
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
            String token,
            UUID headerTenant
    ) {

        return post(
                "/api/v1/school/enrollments"
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
                        "studentId",
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
                        "enrollmentNumber",
                        "TEST-ENROLLMENT"
                )
                .param(
                        "enrollmentDate",
                        "2026-09-01"
                )
                .param(
                        "effectiveFrom",
                        "2026-09-01"
                );
    }


    private String token(
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "student-enrollment-test-user",
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
