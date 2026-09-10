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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CurriculumClassGradeController.class)
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
class CurriculumClassGradeControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CurriculumClassGradeService service;

    @MockitoBean
    private CurriculumVersionRepository versionRepository;


    @Test
    void createWithoutCreatePermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of(
                        "school.academic.curriculum.version.read"
                )
        );

        mockMvc.perform(
                        authenticated(
                                post(base(versionId)),
                                token,
                                tenantId,
                                tenantId
                        )
                                .param(
                                        "classGradeId",
                                        classGradeId.toString()
                                )
                                .param(
                                        "sequenceNumber",
                                        "1"
                                )
                )
                .andExpect(status().isForbidden());

        verify(
                versionRepository,
                never()
        ).findByTenantIdAndId(any(), any());

        verify(
                service,
                never()
        ).create(any(), any(), any(), any());
    }


    @Test
    void readWithoutReadPermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of(
                        "school.academic.curriculum.version.create"
                )
        );

        mockMvc.perform(
                        authenticated(
                                get(base(versionId)),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isForbidden());

        verify(
                service,
                never()
        ).findByCurriculumVersion(any(), any());
    }


    @Test
    void manageWithoutManagePermissionIsForbidden()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of(
                        "school.academic.curriculum.version.read"
                )
        );

        mockMvc.perform(
                        authenticated(
                                patch(
                                        base(versionId)
                                                + "/"
                                                + classGradeId
                                                + "/archive"
                                ),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isForbidden());

        verify(
                service,
                never()
        ).findMapping(any(), any(), any());
    }


    @Test
    void createCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of(
                        "school.academic.curriculum.version.create"
                )
        );

        mockMvc.perform(
                        authenticated(
                                post(base(versionId)),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                                .param(
                                        "classGradeId",
                                        classGradeId.toString()
                                )
                                .param(
                                        "sequenceNumber",
                                        "1"
                                )
                )
                .andExpect(status().isForbidden());

        verify(
                versionRepository,
                never()
        ).findByTenantIdAndId(any(), any());
    }


    @Test
    void listCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of(
                        "school.academic.curriculum.version.read"
                )
        );

        mockMvc.perform(
                        authenticated(
                                get(base(versionId)),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                )
                .andExpect(status().isForbidden());

        verify(
                service,
                never()
        ).findByCurriculumVersion(any(), any());
    }


    @Test
    void getCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of(
                        "school.academic.curriculum.version.read"
                )
        );

        mockMvc.perform(
                        authenticated(
                                get(
                                        base(versionId)
                                                + "/"
                                                + classGradeId
                                ),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                )
                .andExpect(status().isForbidden());

        verify(
                service,
                never()
        ).findMapping(any(), any(), any());
    }


    @Test
    void archiveCannotCrossAuthenticatedTenantBoundary()
            throws Exception {

        UUID principalTenant = UUID.randomUUID();
        UUID requestedTenant = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        String token = token(
                UUID.randomUUID(),
                principalTenant,
                Set.of(
                        "school.academic.curriculum.version.manage"
                )
        );

        mockMvc.perform(
                        authenticated(
                                patch(
                                        base(versionId)
                                                + "/"
                                                + classGradeId
                                                + "/archive"
                                ),
                                token,
                                principalTenant,
                                requestedTenant
                        )
                )
                .andExpect(status().isForbidden());

        verify(
                service,
                never()
        ).findMapping(any(), any(), any());
    }


    @Test
    void authorizedCreateUsesTenantScopedCurriculumVersion()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        CurriculumVersion version =
                org.mockito.Mockito.mock(
                        CurriculumVersion.class
                );

        when(
                versionRepository.findByTenantIdAndId(
                        tenantId,
                        versionId
                )
        ).thenReturn(
                Optional.of(version)
        );

        when(
                service.create(
                        tenantId,
                        version,
                        classGradeId,
                        1
                )
        ).thenReturn(null);

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of(
                        "school.academic.curriculum.version.create"
                )
        );

        mockMvc.perform(
                        authenticated(
                                post(base(versionId)),
                                token,
                                tenantId,
                                tenantId
                        )
                                .param(
                                        "classGradeId",
                                        classGradeId.toString()
                                )
                                .param(
                                        "sequenceNumber",
                                        "1"
                                )
                )
                .andExpect(status().isOk());

        verify(
                versionRepository
        ).findByTenantIdAndId(
                tenantId,
                versionId
        );

        verify(
                service
        ).create(
                tenantId,
                version,
                classGradeId,
                1
        );
    }


    @Test
    void authorizedListUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        when(
                service.findByCurriculumVersion(
                        tenantId,
                        versionId
                )
        ).thenReturn(
                List.of()
        );

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of(
                        "school.academic.curriculum.version.read"
                )
        );

        mockMvc.perform(
                        authenticated(
                                get(base(versionId)),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk());

        verify(
                service
        ).findByCurriculumVersion(
                tenantId,
                versionId
        );
    }


    @Test
    void authorizedGetUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        when(
                service.findMapping(
                        tenantId,
                        versionId,
                        classGradeId
                )
        ).thenReturn(null);

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of(
                        "school.academic.curriculum.version.read"
                )
        );

        mockMvc.perform(
                        authenticated(
                                get(
                                        base(versionId)
                                                + "/"
                                                + classGradeId
                                ),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk());

        verify(
                service
        ).findMapping(
                tenantId,
                versionId,
                classGradeId
        );
    }


    @Test
    void authorizedArchiveUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        CurriculumClassGrade mapping =
                org.mockito.Mockito.mock(
                        CurriculumClassGrade.class
                );

        when(
                service.findMapping(
                        tenantId,
                        versionId,
                        classGradeId
                )
        ).thenReturn(
                mapping
        );

        when(
                service.archive(
                        mapping
                )
        ).thenReturn(null);

        String token = token(
                UUID.randomUUID(),
                tenantId,
                Set.of(
                        "school.academic.curriculum.version.manage"
                )
        );

        mockMvc.perform(
                        authenticated(
                                patch(
                                        base(versionId)
                                                + "/"
                                                + classGradeId
                                                + "/archive"
                                ),
                                token,
                                tenantId,
                                tenantId
                        )
                )
                .andExpect(status().isOk());

        verify(
                service
        ).findMapping(
                tenantId,
                versionId,
                classGradeId
        );

        verify(
                service
        ).archive(
                mapping
        );
    }


    @Test
    void unauthenticatedCurriculumClassGradeRequestIsRejected()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        mockMvc.perform(
                        get(base(versionId))
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .param(
                                        "tenantId",
                                        tenantId.toString()
                                )
                )
                .andExpect(status().isUnauthorized());

        verify(
                service,
                never()
        ).findByCurriculumVersion(any(), any());
    }


    private String base(
            UUID curriculumVersionId
    ) {

        return "/api/v1/school/academic/curriculum-version/"
                + curriculumVersionId
                + "/grades";
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
                        "curriculum-grade-test-user",
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
