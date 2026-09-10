package africa.growtogether.platform.school.academic.curriculum;

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


@WebMvcTest(ClassGradeController.class)
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
class ClassGradeControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ClassGradeService service;


    @Test
    void createWithoutCreatePermissionIsForbidden()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.academic.class-grade.read"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/academic/class-grades"
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
                                        tenantId.toString()
                                )
                                .param(
                                        "educationLevelId",
                                        educationLevelId.toString()
                                )
                                .param(
                                        "classCode",
                                        "P1"
                                )
                                .param(
                                        "className",
                                        "Primary One"
                                )
                                .param(
                                        "sequenceNumber",
                                        "1"
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
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
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void readWithoutReadPermissionIsForbidden()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.academic.class-grade.create"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/class-grades"
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
                                        tenantId.toString()
                                )
                                .param(
                                        "educationLevelId",
                                        educationLevelId.toString()
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
        ).findByEducationLevel(
                any(),
                any()
        );
    }


    @Test
    void manageWithoutManagePermissionIsForbidden()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.academic.class-grade.read"
                        )
                );

        mockMvc.perform(
                        patch(
                                "/api/v1/school/academic/class-grades/P1/activate"
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
        ).findByCode(
                any(),
                any()
        );
    }


    @Test
    void createCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        principalTenant,
                        Set.of(
                                "school.academic.class-grade.create"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/academic/class-grades"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        principalTenant.toString()
                                )
                                .param(
                                        "tenantId",
                                        requestedTenant.toString()
                                )
                                .param(
                                        "educationLevelId",
                                        UUID.randomUUID().toString()
                                )
                                .param(
                                        "classCode",
                                        "P1"
                                )
                                .param(
                                        "className",
                                        "Primary One"
                                )
                                .param(
                                        "sequenceNumber",
                                        "1"
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
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void readCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        principalTenant,
                        Set.of(
                                "school.academic.class-grade.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/class-grades"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        principalTenant.toString()
                                )
                                .param(
                                        "tenantId",
                                        requestedTenant.toString()
                                )
                                .param(
                                        "educationLevelId",
                                        UUID.randomUUID().toString()
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
        ).findByEducationLevel(
                any(),
                any()
        );
    }


    @Test
    void manageCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        principalTenant,
                        Set.of(
                                "school.academic.class-grade.manage"
                        )
                );

        mockMvc.perform(
                        patch(
                                "/api/v1/school/academic/class-grades/P1/activate"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
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
        ).findByCode(
                any(),
                any()
        );
    }


    @Test
    void authorizedCreateUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.academic.class-grade.create"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/academic/class-grades"
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
                                        tenantId.toString()
                                )
                                .param(
                                        "educationLevelId",
                                        educationLevelId.toString()
                                )
                                .param(
                                        "classCode",
                                        "P1"
                                )
                                .param(
                                        "className",
                                        "Primary One"
                                )
                                .param(
                                        "sequenceNumber",
                                        "1"
                                )
                                .param(
                                        "capacity",
                                        "40"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-CLASS-GRADE-001"
                                )
                );

        verify(service).create(
                eq(tenantId),
                eq(educationLevelId),
                eq("P1"),
                eq("Primary One"),
                eq(1),
                eq(40)
        );
    }


    @Test
    void authorizedReadUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.academic.class-grade.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/class-grades"
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
                                        tenantId.toString()
                                )
                                .param(
                                        "educationLevelId",
                                        educationLevelId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-CLASS-GRADE-002"
                                )
                );

        verify(
                service
        ).findByEducationLevel(
                tenantId,
                educationLevelId
        );
    }


    @Test
    void authorizedManageUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        ClassGrade classGrade =
                mock(ClassGrade.class);

        when(
                service.findByCode(
                        tenantId,
                        "P1"
                )
        ).thenReturn(
                classGrade
        );

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.academic.class-grade.manage"
                        )
                );

        mockMvc.perform(
                        patch(
                                "/api/v1/school/academic/class-grades/P1/activate"
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
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-CLASS-GRADE-004"
                                )
                );

        verify(
                service
        ).findByCode(
                tenantId,
                "P1"
        );

        verify(
                service
        ).activate(
                classGrade
        );
    }


    @Test
    void unauthenticatedClassGradeRequestIsRejected()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/class-grades"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .param(
                                        "tenantId",
                                        tenantId.toString()
                                )
                                .param(
                                        "educationLevelId",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
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
        ).findByEducationLevel(
                any(),
                any()
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
                        "class-grade-test-user",
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
