package africa.growtogether.platform.school.teacher.coverage;

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
import africa.growtogether.platform.school.academic.coverage.api.TeacherCoverageResponse;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        TeacherCoverageSelfServiceController.class
)
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
class TeacherCoverageSelfServiceControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TeacherCoverageSelfServiceService service;

    @Test
    void unauthenticatedCoverageReadIsRejected()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/school/teacher/coverage"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-001"
                                )
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void authenticatedReadWithoutTenantHeaderIsRejected()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.teacher.coverage.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/teacher/coverage"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-TENANT-001"
                                )
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void crossTenantCoverageReadIsRejected()
            throws Exception {

        UUID authenticatedTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        authenticatedTenant,
                        Set.of(
                                "school.teacher.coverage.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/teacher/coverage"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-TENANT-002"
                                )
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void coverageReadRequiresDedicatedReadPermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.teacher.programme.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/teacher/coverage"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void authorisedTeacherCanReadOwnCoverage()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID teacherId =
                UUID.randomUUID();

        when(
                service.currentCoverage()
        ).thenReturn(
                new TeacherCoverageView(
                        teacherId,
                        null,
                        new TeacherCoverageView.CoverageSummary(
                                0,
                                0,
                                0,
                                0,
                                0,
                                0
                        ),
                        List.of()
                )
        );

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.teacher.coverage.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/teacher/coverage"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$.teacherProfileId"
                        ).value(
                                teacherId.toString()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.summary.total"
                        ).value(
                                0
                        )
                );

        verify(
                service
        ).currentCoverage();
    }

    @Test
    void readPermissionDoesNotGrantCoverageUpdate()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID coverageId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.teacher.coverage.read"
                        )
                );

        mockMvc.perform(
                        patch(
                                "/api/v1/school/teacher/coverage/"
                                        + coverageId
                                        + "/progress"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );

        verify(
                service,
                never()
        ).markInProgress(
                any()
        );
    }

    @Test
    void updatePermissionCanReachProgressMutation()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID coverageId =
                UUID.randomUUID();

        when(
                service.markInProgress(
                        coverageId
                )
        ).thenReturn(
                response(
                        coverageId,
                        "IN_PROGRESS"
                )
        );

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.teacher.coverage.update"
                        )
                );

        mockMvc.perform(
                        patch(
                                "/api/v1/school/teacher/coverage/"
                                        + coverageId
                                        + "/progress"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$.coverageStatus"
                        ).value(
                                "IN_PROGRESS"
                        )
                );

        verify(
                service
        ).markInProgress(
                coverageId
        );
    }

    @Test
    void updatePermissionDoesNotGrantCoverageRead()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                "school.teacher.coverage.update"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/teacher/coverage"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verifyNoInteractions(
                service
        );
    }

    private TeacherCoverageResponse response(
            UUID coverageId,
            String status
    ) {

        return new TeacherCoverageResponse(
                coverageId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "TOPIC",
                "Coverage item",
                1,
                status,
                null,
                null
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
                        "teacher-coverage-test-user",
                        tenantId,
                        Set.of(
                                "TEACHER"
                        ),
                        permissions,
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }
}
