package africa.growtogether.platform.school.timetable.bell;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({
        BellScheduleController.class,
        BellPeriodController.class
})
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
class BellControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private BellScheduleService scheduleService;

    @MockitoBean
    private BellPeriodService periodService;


    @ParameterizedTest
    @ValueSource(
            strings = {
                    "schedule-create",
                    "schedule-read",
                    "schedule-campus",
                    "schedule-approve",
                    "schedule-activate",
                    "schedule-suspend",
                    "period-create",
                    "period-read",
                    "period-schedule"
            }
    )
    void bellEndpointsAcceptTheirAuthoritativePermission(
            String operation
    ) throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID targetId =
                UUID.randomUUID();

        String permission =
                switch (operation) {

                    case "schedule-create",
                         "period-create" ->
                            "school.timetable.create";

                    case "schedule-read",
                         "schedule-campus",
                         "period-read",
                         "period-schedule" ->
                            "school.timetable.read";

                    case "schedule-approve" ->
                            "school.timetable.approve";

                    case "schedule-activate" ->
                            "school.timetable.activate";

                    case "schedule-suspend" ->
                            "school.timetable.suspend";

                    default ->
                            throw new IllegalArgumentException(
                                    "Unknown Bell operation: "
                                            + operation
                            );
                };

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                permission
                        )
                );

        MockHttpServletRequestBuilder request =
                requestFor(
                        operation,
                        targetId
                );

        mockMvc.perform(
                        request
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
                        jsonPath("$.success")
                                .value(true)
                );

        switch (operation) {

            case "schedule-create" ->
                    verify(
                            scheduleService
                    ).create(
                            eq(tenantId),
                            any(
                                    CreateBellScheduleCommand.class
                            )
                    );

            case "schedule-read" ->
                    verify(
                            scheduleService
                    ).get(
                            tenantId,
                            targetId
                    );

            case "schedule-campus" ->
                    verify(
                            scheduleService
                    ).findByCampus(
                            tenantId,
                            targetId
                    );

            case "schedule-approve" ->
                    verify(
                            scheduleService
                    ).approve(
                            tenantId,
                            targetId
                    );

            case "schedule-activate" ->
                    verify(
                            scheduleService
                    ).activate(
                            tenantId,
                            targetId
                    );

            case "schedule-suspend" ->
                    verify(
                            scheduleService
                    ).suspend(
                            tenantId,
                            targetId
                    );

            case "period-create" ->
                    verify(
                            periodService
                    ).create(
                            eq(tenantId),
                            any(
                                    CreateBellPeriodCommand.class
                            )
                    );

            case "period-read" ->
                    verify(
                            periodService
                    ).get(
                            tenantId,
                            targetId
                    );

            case "period-schedule" ->
                    verify(
                            periodService
                    ).findBySchedule(
                            tenantId,
                            targetId
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown Bell operation: "
                                    + operation
                    );
        }
    }


    @ParameterizedTest
    @ValueSource(
            strings = {
                    "schedule-create",
                    "schedule-read",
                    "schedule-campus",
                    "schedule-approve",
                    "schedule-activate",
                    "schedule-suspend",
                    "period-create",
                    "period-read",
                    "period-schedule"
            }
    )
    void bellEndpointsRejectMissingOperationPermission(
            String operation
    ) throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID targetId =
                UUID.randomUUID();

        /*
         * review is deliberately unrelated to every Bell endpoint.
         */
        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.timetable.review"
                        )
                );

        mockMvc.perform(
                        requestFor(
                                operation,
                                targetId
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
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );

        verifyNoInteractions(
                scheduleService,
                periodService
        );
    }


    @Test
    void rejectsCrossTenantBellRequestBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                "school.timetable.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/timetables/bell-schedules/campus/"
                                        + campusId
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
                scheduleService,
                periodService
        );
    }


    @Test
    void rejectsBellRequestWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.timetable.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/timetables/bell-schedules/campus/"
                                        + campusId
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
                scheduleService,
                periodService
        );
    }


    @Test
    void rejectsUnauthenticatedBellRequest()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/timetables/bell-schedules/campus/"
                                        + campusId
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
                                .value(
                                        "GT-AUTH-001"
                                )
                );

        verifyNoInteractions(
                scheduleService,
                periodService
        );
    }


    private MockHttpServletRequestBuilder requestFor(
            String operation,
            UUID targetId
    ) {

        return switch (operation) {

            case "schedule-create" ->
                    post(
                            "/api/v1/school/timetables/bell-schedules"
                    )
                            .param(
                                    "campusId",
                                    targetId.toString()
                            )
                            .param(
                                    "scheduleCode",
                                    "REGULAR-001"
                            )
                            .param(
                                    "scheduleName",
                                    "Regular School Day"
                            )
                            .param(
                                    "scheduleType",
                                    "REGULAR"
                            )
                            .param(
                                    "effectiveFrom",
                                    "2026-01-01"
                            );

            case "schedule-read" ->
                    get(
                            "/api/v1/school/timetables/bell-schedules/"
                                    + targetId
                    );

            case "schedule-campus" ->
                    get(
                            "/api/v1/school/timetables/bell-schedules/campus/"
                                    + targetId
                    );

            case "schedule-approve" ->
                    patch(
                            "/api/v1/school/timetables/bell-schedules/"
                                    + targetId
                                    + "/approve"
                    );

            case "schedule-activate" ->
                    patch(
                            "/api/v1/school/timetables/bell-schedules/"
                                    + targetId
                                    + "/activate"
                    );

            case "schedule-suspend" ->
                    patch(
                            "/api/v1/school/timetables/bell-schedules/"
                                    + targetId
                                    + "/suspend"
                    );

            case "period-create" ->
                    post(
                            "/api/v1/school/timetables/bell-periods"
                    )
                            .param(
                                    "bellScheduleId",
                                    targetId.toString()
                            )
                            .param(
                                    "periodCode",
                                    "P1"
                            )
                            .param(
                                    "periodName",
                                    "Period 1"
                            )
                            .param(
                                    "sequenceNumber",
                                    "1"
                            )
                            .param(
                                    "periodType",
                                    "TEACHING"
                            )
                            .param(
                                    "startTime",
                                    "08:00"
                            )
                            .param(
                                    "endTime",
                                    "08:40"
                            );

            case "period-read" ->
                    get(
                            "/api/v1/school/timetables/bell-periods/"
                                    + targetId
                    );

            case "period-schedule" ->
                    get(
                            "/api/v1/school/timetables/bell-periods/schedule/"
                                    + targetId
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown Bell operation: "
                                    + operation
                    );
        };
    }


    private String token(
            UUID userId,
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        userId,
                        "bell-test-user",
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
