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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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


@WebMvcTest(CurriculumController.class)
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
class CurriculumControllerSecurityTest {

    private static final String BASE =
            "/api/v1/school/academic/curriculum";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CurriculumService service;


    @Test
    void createWithoutCreatePermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.read")
        );

        mockMvc.perform(
                        authenticated(
                                post(BASE),
                                token,
                                tenantId,
                                tenantId
                        )
                                .param("curriculumCode", "UG-NCDC")
                                .param("curriculumName", "Uganda National Curriculum")
                                .param("curriculumType", "NATIONAL")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).create(
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

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.create")
        );

        mockMvc.perform(
                        authenticated(
                                get(BASE + "/active"),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                service,
                never()
        ).findActiveCurricula(any());
    }


    @Test
    void manageWithoutManagePermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.read")
        );

        mockMvc.perform(
                        authenticated(
                                patch(BASE + "/UG-NCDC/activate"),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

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

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.create")
        );

        mockMvc.perform(
                        authenticated(
                                post(BASE),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                                .param("curriculumCode", "UG-NCDC")
                                .param("curriculumName", "Uganda National Curriculum")
                                .param("curriculumType", "NATIONAL")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(service, never()).create(
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void getCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(BASE + "/UG-NCDC"),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                service,
                never()
        ).findByCode(
                any(),
                any()
        );
    }


    @Test
    void activeCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(BASE + "/active"),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                service,
                never()
        ).findActiveCurricula(any());
    }


    @Test
    void manageCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.manage")
        );

        mockMvc.perform(
                        authenticated(
                                patch(BASE + "/UG-NCDC/activate"),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

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

        UUID tenantId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.create")
        );

        mockMvc.perform(
                        authenticated(
                                post(BASE),
                                token,
                                tenantId,
                                tenantId
                        )
                                .param("curriculumCode", "UG-NCDC")
                                .param("curriculumName", "Uganda National Curriculum")
                                .param("curriculumType", "NATIONAL")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-CURRICULUM-001")
                );

        verify(service).create(
                eq(tenantId),
                eq("UG-NCDC"),
                eq("Uganda National Curriculum"),
                eq("NATIONAL")
        );
    }


    @Test
    void authorizedGetUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(BASE + "/UG-NCDC"),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-CURRICULUM-002")
                );

        verify(service).findByCode(
                tenantId,
                "UG-NCDC"
        );
    }


    @Test
    void authorizedActiveUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(BASE + "/active"),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-CURRICULUM-003")
                );

        verify(service).findActiveCurricula(
                tenantId
        );
    }


    @Test
    void authorizedManageUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();

        Curriculum curriculum =
                mock(Curriculum.class);

        when(
                service.findByCode(
                        tenantId,
                        "UG-NCDC"
                )
        ).thenReturn(curriculum);

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.manage")
        );

        mockMvc.perform(
                        authenticated(
                                patch(BASE + "/UG-NCDC/activate"),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-CURRICULUM-004")
                );

        verify(service).findByCode(
                tenantId,
                "UG-NCDC"
        );

        verify(service).activate(
                tenantId,
                curriculum
        );
    }


    @Test
    void listWithoutReadPermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.create")
        );

        mockMvc.perform(
                        authenticated(
                                get(BASE),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                service,
                never()
        ).findAllCurricula(any());
    }


    @Test
    void listCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(BASE),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                service,
                never()
        ).findAllCurricula(any());
    }


    @Test
    void authorizedListUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();

        when(
                service.findAllCurricula(
                        tenantId
                )
        ).thenReturn(
                java.util.List.of()
        );

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(BASE),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-SCHOOL-CURRICULUM-005")
                );

        verify(
                service
        ).findAllCurricula(
                tenantId
        );
    }


    @Test
    void unauthenticatedCurriculumRequestIsRejected()
            throws Exception {

        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        get(BASE + "/active")
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
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("GT-AUTH-001"));

        verify(
                service,
                never()
        ).findActiveCurricula(any());
    }


    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request,
            String token,
            UUID principalTenant,
            UUID requestedTenant
    ) {

        return request
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
                        "curriculum-test-user",
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
