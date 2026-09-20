package africa.growtogether.platform.school.teacher.programme;

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
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeacherProgrammeController.class)
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
class TeacherProgrammeControllerSecurityTest {

    private static final String ENDPOINT =
            "/api/v1/school/teacher/programme/today";

    private static final String PROGRAMME_PERMISSION =
            "school.teacher.programme.read";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TeacherProgrammeTodayService service;

    @Test
    void authorizedTeacherCanReadOwnTodayProgramme()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        TeacherProgrammeToday response =
                new TeacherProgrammeToday(
                        LocalDate.of(
                                2026,
                                9,
                                19
                        ),
                        ZoneId.of(
                                "Africa/Kampala"
                        ),
                        List.of(),
                        List.of()
                );

        org.mockito.Mockito.when(
                service.currentProgramme()
        ).thenReturn(
                response
        );

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                PROGRAMME_PERMISSION
                        )
                );

        mockMvc.perform(
                        get(
                                ENDPOINT
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
                        jsonPath("$.date")
                                .value("2026-09-19")
                )
                .andExpect(
                        jsonPath("$.zone")
                                .value("Africa/Kampala")
                )
                .andExpect(
                        jsonPath("$.lessons")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.calendarEvents")
                                .isArray()
                );

        verify(
                service
        ).currentProgramme();
    }

    @Test
    void rejectsTeacherWithoutProgrammeReadPermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.teaching-assignment.read"
                        )
                );

        mockMvc.perform(
                        get(
                                ENDPOINT
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
                                .value("GT-AUTH-003")
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void rejectsCrossTenantTeacherRequestBeforeProgrammeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                PROGRAMME_PERMISSION
                        )
                );

        mockMvc.perform(
                        get(
                                ENDPOINT
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
                                .value("GT-TENANT-002")
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void rejectsAuthenticatedTeacherRequestWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                PROGRAMME_PERMISSION
                        )
                );

        mockMvc.perform(
                        get(
                                ENDPOINT
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
                                .value("GT-TENANT-001")
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void rejectsUnauthenticatedTeacherProgrammeRequest()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        mockMvc.perform(
                        get(
                                ENDPOINT
                        )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("GT-AUTH-001")
                );

        verifyNoInteractions(
                service
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
                        "teacher-programme-test-user",
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
