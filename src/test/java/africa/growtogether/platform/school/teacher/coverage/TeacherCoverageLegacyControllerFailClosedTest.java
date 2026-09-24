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

import africa.growtogether.platform.school.academic.coverage.TeacherCoverageController;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverageService;
import africa.growtogether.platform.school.teacher.workspace.TeacherWorkspaceController;
import africa.growtogether.platform.school.teacher.workspace.TeacherWorkspaceService;

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

import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
                TeacherCoverageController.class,
                TeacherWorkspaceController.class
        }
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
class TeacherCoverageLegacyControllerFailClosedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TeacherCoverageService coverageService;

    @MockitoBean
    private TeacherWorkspaceService workspaceService;

    @Test
    void legacyCallerSelectedTeacherCoverageRouteIsFailClosed()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID teacherId =
                UUID.randomUUID();

        String token =
                token(
                        tenantId
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/academic/"
                                        + "teacher-coverage/teacher/"
                                        + teacherId
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
                coverageService
        );
    }

    @Test
    void legacyCallerSelectedWorkspaceRouteIsFailClosed()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID teacherId =
                UUID.randomUUID();

        String token =
                token(
                        tenantId
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/teacher/workspace"
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
                                        "teacherProfileId",
                                        teacherId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verifyNoInteractions(
                workspaceService
        );
    }

    private String token(
            UUID tenantId
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "legacy-coverage-test-user",
                        tenantId,
                        Set.of(
                                "TEACHER",
                                "SCHOOL_ADMIN"
                        ),
                        Set.of(
                                "school.teacher.coverage.read",
                                "school.teacher.coverage.update",
                                "school.academic.curriculum.read",
                                "school.academic.teaching-assignment.read"
                        ),
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }
}
