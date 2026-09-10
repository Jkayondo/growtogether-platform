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

import java.time.LocalDate;
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


@WebMvcTest(CurriculumVersionController.class)
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
class CurriculumVersionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CurriculumVersionService service;

    @MockitoBean
    private CurriculumRepository curriculumRepository;


    @Test
    void createWithoutCreatePermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.version.read")
        );

        mockMvc.perform(
                        authenticated(
                                post(base(curriculumId)),
                                token,
                                tenantId,
                                tenantId
                        )
                                .param("versionCode", "2026")
                                .param("versionName", "2026 Curriculum")
                                .param("effectiveFrom", "2026-01-01")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                curriculumRepository,
                never()
        ).findByTenantIdAndId(any(), any());

        verify(
                service,
                never()
        ).create(any(), any(), any(), any(), any());
    }


    @Test
    void readWithoutReadPermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.version.create")
        );

        mockMvc.perform(
                        authenticated(
                                get(base(curriculumId)),
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
        ).findByCurriculum(any(), any());
    }


    @Test
    void manageWithoutManagePermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.version.read")
        );

        mockMvc.perform(
                        authenticated(
                                patch(base(curriculumId) + "/2026/activate"),
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
        ).findByCode(any(), any(), any());
    }


    @Test
    void createCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.version.create")
        );

        mockMvc.perform(
                        authenticated(
                                post(base(curriculumId)),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                                .param("versionCode", "2026")
                                .param("versionName", "2026 Curriculum")
                                .param("effectiveFrom", "2026-01-01")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                curriculumRepository,
                never()
        ).findByTenantIdAndId(any(), any());
    }


    @Test
    void listCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.version.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(base(curriculumId)),
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
        ).findByCurriculum(any(), any());
    }


    @Test
    void getCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.version.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(base(curriculumId) + "/2026"),
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
        ).findByCode(any(), any(), any());
    }


    @Test
    void approveCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.version.manage")
        );

        mockMvc.perform(
                        authenticated(
                                patch(base(curriculumId) + "/2026/approve"),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                                .param(
                                        "approvalReference",
                                        "HQ-APP-001"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GT-AUTH-003"));

        verify(
                service,
                never()
        ).findByCode(any(), any(), any());
    }


    @Test
    void activateCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of("school.academic.curriculum.version.manage")
        );

        mockMvc.perform(
                        authenticated(
                                patch(base(curriculumId) + "/2026/activate"),
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
        ).findByCode(any(), any(), any());
    }


    @Test
    void authorizedCreateUsesTenantScopedCurriculumParent()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        Curriculum curriculum =
                mock(Curriculum.class);

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        when(
                curriculumRepository.findByTenantIdAndId(
                        tenantId,
                        curriculumId
                )
        ).thenReturn(
                java.util.Optional.of(curriculum)
        );

        when(
                service.create(
                        eq(tenantId),
                        eq(curriculum),
                        eq("2026"),
                        eq("2026 Curriculum"),
                        eq(LocalDate.of(2026, 1, 1))
                )
        ).thenReturn(
                version
        );

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.version.create")
        );

        mockMvc.perform(
                        authenticated(
                                post(base(curriculumId)),
                                token,
                                tenantId,
                                tenantId
                        )
                                .param("versionCode", "2026")
                                .param("versionName", "2026 Curriculum")
                                .param("effectiveFrom", "2026-01-01")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-CURRICULUM-VERSION-001"
                                )
                );

        verify(
                curriculumRepository
        ).findByTenantIdAndId(
                tenantId,
                curriculumId
        );

        verify(
                service
        ).create(
                tenantId,
                curriculum,
                "2026",
                "2026 Curriculum",
                LocalDate.of(2026, 1, 1)
        );
    }


    @Test
    void authorizedListUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.version.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(base(curriculumId)),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-CURRICULUM-VERSION-002"
                                )
                );

        verify(service).findByCurriculum(
                tenantId,
                curriculumId
        );
    }


    @Test
    void authorizedGetUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.version.read")
        );

        mockMvc.perform(
                        authenticated(
                                get(base(curriculumId) + "/2026"),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-CURRICULUM-VERSION-003"
                                )
                );

        verify(service).findByCode(
                tenantId,
                curriculumId,
                "2026"
        );
    }


    @Test
    void approvalUsesAuthenticatedUserNotSuppliedApprover()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        UUID forgedApproverId = UUID.randomUUID();

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        when(
                service.findByCode(
                        tenantId,
                        curriculumId,
                        "2026"
                )
        ).thenReturn(
                version
        );

        when(
                service.approve(
                        version,
                        authenticatedUserId,
                        "HQ-APP-001"
                )
        ).thenReturn(
                version
        );

        String token = token(
                authenticatedUserId,
                tenantId,
                Set.of("school.academic.curriculum.version.manage")
        );

        mockMvc.perform(
                        authenticated(
                                patch(base(curriculumId) + "/2026/approve"),
                                token,
                                tenantId,
                                tenantId
                        )
                                .param(
                                        "approvalReference",
                                        "HQ-APP-001"
                                )
                                .param(
                                        "approvedBy",
                                        forgedApproverId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-CURRICULUM-VERSION-004"
                                )
                );

        verify(service).approve(
                version,
                authenticatedUserId,
                "HQ-APP-001"
        );

        verify(
                service,
                never()
        ).approve(
                version,
                forgedApproverId,
                "HQ-APP-001"
        );
    }


    @Test
    void authorizedActivateUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        when(
                service.findByCode(
                        tenantId,
                        curriculumId,
                        "2026"
                )
        ).thenReturn(
                version
        );

        when(
                service.activate(version)
        ).thenReturn(
                version
        );

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of("school.academic.curriculum.version.manage")
        );

        mockMvc.perform(
                        authenticated(
                                patch(base(curriculumId) + "/2026/activate"),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-CURRICULUM-VERSION-005"
                                )
                );

        verify(service).findByCode(
                tenantId,
                curriculumId,
                "2026"
        );

        verify(service).activate(
                version
        );
    }


    @Test
    void unauthenticatedCurriculumVersionRequestIsRejected()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        mockMvc.perform(
                        get(base(curriculumId))
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
        ).findByCurriculum(any(), any());
    }


    private String base(
            UUID curriculumId
    ) {

        return "/api/v1/school/academic/curriculum/"
                + curriculumId
                + "/versions";
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
                        "curriculum-version-test-user",
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
