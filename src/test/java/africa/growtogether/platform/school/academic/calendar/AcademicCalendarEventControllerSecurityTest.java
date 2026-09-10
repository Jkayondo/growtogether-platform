package africa.growtogether.platform.school.academic.calendar;

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

@WebMvcTest(AcademicCalendarEventController.class)
@EnableWebSecurity
@Import({AcademicCalendarEventService.class,
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

class AcademicCalendarEventControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AcademicCalendarEventRepository repository;
    @MockitoBean
    private africa.growtogether.platform.common.events.EventPublisher publisher;
    @MockitoBean
    private africa.growtogether.platform.school.academic.year.AcademicYearRepository years;
    @MockitoBean
    private africa.growtogether.platform.school.academic.term.AcademicTermRepository terms;

    private static final String ENDPOINT =
            "/api/v1/school/academic/calendar/events/upcoming";
    private static final java.time.Instant START =
            java.time.Instant.parse("2026-09-10T00:00:00Z");
    private static final java.time.Instant END =
            java.time.Instant.parse("2026-09-11T00:00:00Z");
    private static final String READ = "school.academic.calendar.read";

    @Test
    void queryUsesAuthenticatedTenantDespiteSpoofedQueryParameter() throws Exception {
        UUID tenant = UUID.randomUUID();
        org.mockito.Mockito.when(
                repository.findByTenantIdAndStartAtBetween(tenant, START, END))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get(ENDPOINT)
                .header("Authorization", "Bearer " + token(tenant, Set.of(READ)))
                .header("X-Tenant-ID", tenant.toString())
                .param("tenantId", UUID.randomUUID().toString())
                .param("start", START.toString())
                .param("end", END.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(repository).findByTenantIdAndStartAtBetween(tenant, START, END);
        org.mockito.Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    void missingPermissionCannotQueryCalendar() throws Exception {
        UUID tenant = UUID.randomUUID();
        mockMvc.perform(get(ENDPOINT)
                .header("Authorization", "Bearer " + token(tenant, Set.of()))
                .header("X-Tenant-ID", tenant.toString())
                .param("start", START.toString())
                .param("end", END.toString()))
                .andExpect(status().isForbidden());
        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    @Test
    void signedOutRequestCannotQueryCalendar() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .param("start", START.toString())
                .param("end", END.toString()))
                .andExpect(status().isUnauthorized());
        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    @Test
    void conflictingTenantHeaderCannotQueryCalendar() throws Exception {
        UUID tenant = UUID.randomUUID();
        mockMvc.perform(get(ENDPOINT)
                .header("Authorization", "Bearer " + token(tenant, Set.of(READ)))
                .header("X-Tenant-ID", UUID.randomUUID().toString())
                .param("start", START.toString())
                .param("end", END.toString()))
                .andExpect(status().isForbidden());
        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    private String token(UUID tenant, Set<String> permissions) {
        return jwtService.issueAccessToken(new GtPrincipal(
                UUID.randomUUID(), "calendar-security-test", tenant,
                Set.of("TEACHER"), permissions, UUID.randomUUID()));
    }
}
