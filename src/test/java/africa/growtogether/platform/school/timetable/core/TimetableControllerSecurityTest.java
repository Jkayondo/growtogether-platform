package africa.growtogether.platform.school.timetable.core;

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

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TimetableController.class)
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
class TimetableControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TimetableService service;


    @Test
    void authenticatedApproverUsesJwtTenantAndJwtActor()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID timetableId =
                UUID.randomUUID();

        UUID suppliedTenantOverride =
                UUID.randomUUID();

        UUID suppliedActorOverride =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.timetable.approve"
                        )
                );

        mockMvc.perform(
                        patch(
                                "/api/v1/school/timetables/"
                                        + timetableId
                                        + "/approve"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .header(
                                        "X-Correlation-ID",
                                        "A11-HTTP-APPROVE-001"
                                )

                                /*
                                 * These deliberately hostile parameters
                                 * are not controller inputs.
                                 *
                                 * They prove that callers cannot replace
                                 * the authenticated tenant or actor.
                                 */
                                .param(
                                        "tenantId",
                                        suppliedTenantOverride.toString()
                                )
                                .param(
                                        "approvedBy",
                                        suppliedActorOverride.toString()
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
                                        "GT-SCHOOL-TIMETABLE-006"
                                )
                )
                .andExpect(
                        jsonPath("$.metadata.tenantId")
                                .value(
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.metadata.correlationId")
                                .value(
                                        "A11-HTTP-APPROVE-001"
                                )
                );

        verify(
                service
        ).approve(
                tenantId,
                timetableId,
                userId
        );
    }


    @Test
    void publishPermissionCannotApproveTimetable()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID timetableId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.timetable.publish"
                        )
                );

        mockMvc.perform(
                        patch(
                                "/api/v1/school/timetables/"
                                        + timetableId
                                        + "/approve"
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

        verify(
                service,
                never()
        ).approve(
                any(),
                any(),
                any()
        );
    }


    @Test
    void rejectsCrossTenantRequestBeforeControllerService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID timetableId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                "school.timetable.approve"
                        )
                );

        mockMvc.perform(
                        patch(
                                "/api/v1/school/timetables/"
                                        + timetableId
                                        + "/approve"
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
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-TENANT-002"
                                )
                );

        verify(
                service,
                never()
        ).approve(
                any(),
                any(),
                any()
        );
    }


    @Test
    void rejectsAuthenticatedRequestWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID timetableId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.timetable.approve"
                        )
                );

        mockMvc.perform(
                        patch(
                                "/api/v1/school/timetables/"
                                        + timetableId
                                        + "/approve"
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
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-TENANT-001"
                                )
                );

        verify(
                service,
                never()
        ).approve(
                any(),
                any(),
                any()
        );
    }


    @Test
    void rejectsUnauthenticatedTimetableRequest()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID timetableId =
                UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/v1/school/timetables/"
                                        + timetableId
                                        + "/approve"
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
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-001"
                                )
                );

        verify(
                service,
                never()
        ).approve(
                any(),
                any(),
                any()
        );
    }


    @Test
    void manualHttpCreateCannotForgeGeneratedProvenance()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID bellScheduleId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.timetable.create"
                        )
                );

        var commandCaptor =
                org.mockito.ArgumentCaptor.forClass(
                        CreateTimetableCommand.class
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/timetables"
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
                                        "timetableCode",
                                        "TT-MANUAL-001"
                                )
                                .param(
                                        "timetableName",
                                        "Manual Timetable"
                                )
                                .param(
                                        "academicYearId",
                                        academicYearId.toString()
                                )
                                .param(
                                        "campusId",
                                        campusId.toString()
                                )
                                .param(
                                        "bellScheduleId",
                                        bellScheduleId.toString()
                                )
                                .param(
                                        "timetableType",
                                        "MASTER"
                                )
                                .param(
                                        "effectiveFrom",
                                        "2026-02-02"
                                )

                                /*
                                 * Deliberately hostile provenance fields.
                                 * They are not accepted by the controller.
                                 */
                                .param(
                                        "generatedBy",
                                        "AI_ASSISTED"
                                )
                                .param(
                                        "generationReference",
                                        UUID.randomUUID().toString()
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
                                        "GT-SCHOOL-TIMETABLE-001"
                                )
                );

        verify(
                service
        ).create(
                org.mockito.ArgumentMatchers.eq(
                        tenantId
                ),
                commandCaptor.capture()
        );

        CreateTimetableCommand command =
                commandCaptor.getValue();

        assertEquals(
                "MANUAL",
                command.generatedBy()
        );

        assertNull(
                command.generationReference()
        );
    }


    @ParameterizedTest
    @ValueSource(
            strings = {
                    "read-by-id",
                    "read-by-academic-year",
                    "read-by-campus",
                    "submit-review",
                    "publish",
                    "activate",
                    "suspend"
            }
    )
    void remainingTimetableEndpointsAcceptOnlyTheirAuthoritativePermission(
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
                    case "read-by-id",
                         "read-by-academic-year",
                         "read-by-campus" ->
                            "school.timetable.read";

                    case "submit-review" ->
                            "school.timetable.review";

                    case "publish" ->
                            "school.timetable.publish";

                    case "activate" ->
                            "school.timetable.activate";

                    case "suspend" ->
                            "school.timetable.suspend";

                    default ->
                            throw new IllegalArgumentException(
                                    "Unknown test operation: "
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

        var request =
                switch (operation) {

                    case "read-by-id" ->
                            get(
                                    "/api/v1/school/timetables/"
                                            + targetId
                            );

                    case "read-by-academic-year" ->
                            get(
                                    "/api/v1/school/timetables/academic-year/"
                                            + targetId
                            );

                    case "read-by-campus" ->
                            get(
                                    "/api/v1/school/timetables/campus/"
                                            + targetId
                            );

                    case "submit-review" ->
                            patch(
                                    "/api/v1/school/timetables/"
                                            + targetId
                                            + "/submit-review"
                            );

                    case "publish" ->
                            patch(
                                    "/api/v1/school/timetables/"
                                            + targetId
                                            + "/publish"
                            );

                    case "activate" ->
                            patch(
                                    "/api/v1/school/timetables/"
                                            + targetId
                                            + "/activate"
                            );

                    case "suspend" ->
                            patch(
                                    "/api/v1/school/timetables/"
                                            + targetId
                                            + "/suspend"
                            );

                    default ->
                            throw new IllegalArgumentException(
                                    "Unknown test operation: "
                                            + operation
                            );
                };

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

            case "read-by-id" ->
                    verify(
                            service
                    ).get(
                            tenantId,
                            targetId
                    );

            case "read-by-academic-year" ->
                    verify(
                            service
                    ).findByAcademicYear(
                            tenantId,
                            targetId
                    );

            case "read-by-campus" ->
                    verify(
                            service
                    ).findByCampus(
                            tenantId,
                            targetId
                    );

            case "submit-review" ->
                    verify(
                            service
                    ).submitForReview(
                            tenantId,
                            targetId,
                            userId
                    );

            case "publish" ->
                    verify(
                            service
                    ).publish(
                            tenantId,
                            targetId,
                            userId
                    );

            case "activate" ->
                    verify(
                            service
                    ).activate(
                            tenantId,
                            targetId,
                            userId
                    );

            case "suspend" ->
                    verify(
                            service
                    ).suspend(
                            tenantId,
                            targetId,
                            userId
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown test operation: "
                                    + operation
                    );
        }
    }


    @ParameterizedTest
    @ValueSource(
            strings = {
                    "read-by-id",
                    "read-by-academic-year",
                    "read-by-campus",
                    "submit-review",
                    "publish",
                    "activate",
                    "suspend"
            }
    )
    void remainingTimetableEndpointsRejectMissingOperationPermission(
            String operation
    ) throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID targetId =
                UUID.randomUUID();

        /*
         * Deliberately give an unrelated Timetable permission.
         *
         * This proves each endpoint's own @PreAuthorize rule rather
         * than merely proving that the caller is authenticated.
         */
        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.timetable.create"
                        )
                );

        var request =
                switch (operation) {

                    case "read-by-id" ->
                            get(
                                    "/api/v1/school/timetables/"
                                            + targetId
                            );

                    case "read-by-academic-year" ->
                            get(
                                    "/api/v1/school/timetables/academic-year/"
                                            + targetId
                            );

                    case "read-by-campus" ->
                            get(
                                    "/api/v1/school/timetables/campus/"
                                            + targetId
                            );

                    case "submit-review" ->
                            patch(
                                    "/api/v1/school/timetables/"
                                            + targetId
                                            + "/submit-review"
                            );

                    case "publish" ->
                            patch(
                                    "/api/v1/school/timetables/"
                                            + targetId
                                            + "/publish"
                            );

                    case "activate" ->
                            patch(
                                    "/api/v1/school/timetables/"
                                            + targetId
                                            + "/activate"
                            );

                    case "suspend" ->
                            patch(
                                    "/api/v1/school/timetables/"
                                            + targetId
                                            + "/suspend"
                            );

                    default ->
                            throw new IllegalArgumentException(
                                    "Unknown test operation: "
                                            + operation
                            );
                };

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
                        "a11-test-user",
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
