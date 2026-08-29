package africa.growtogether.platform.school.timetable.core;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/timetables")
public class TimetableController {

    private final TimetableService service;
    private final EnterpriseIdentityContext identity;
    private final ApiResponses responses;


    public TimetableController(
            TimetableService service,
            EnterpriseIdentityContext identity,
            ApiResponses responses
    ) {
        this.service = service;
        this.identity = identity;
        this.responses = responses;
    }


    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.timetable.create')"
    )
    public ApiResponse<Timetable> create(
            @RequestParam String timetableCode,
            @RequestParam String timetableName,
            @RequestParam(required = false) String description,
            @RequestParam UUID academicYearId,
            @RequestParam(required = false) UUID academicTermId,
            @RequestParam UUID campusId,
            @RequestParam UUID bellScheduleId,
            @RequestParam String timetableType,
            @RequestParam(required = false) Integer versionNumber,
            @RequestParam LocalDate effectiveFrom,
            @RequestParam(required = false) LocalDate effectiveTo,
            @RequestParam(required = false) UUID workflowInstanceId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        CreateTimetableCommand command =
                new CreateTimetableCommand(
                        timetableCode,
                        timetableName,
                        description,
                        academicYearId,
                        academicTermId,
                        campusId,
                        bellScheduleId,
                        timetableType,
                        versionNumber,
                        effectiveFrom,
                        effectiveTo,
                        "MANUAL",
                        null,
                        workflowInstanceId
                );

        return responses.success(
                "GT-SCHOOL-TIMETABLE-001",
                "Timetable created.",
                service.create(
                        tenantId,
                        command
                )
        );
    }


    @GetMapping("/{timetableId}")
    @PreAuthorize(
            "hasAuthority('school.timetable.read')"
    )
    public ApiResponse<Timetable> get(
            @PathVariable UUID timetableId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-002",
                "Timetable retrieved.",
                service.get(
                        identity.requireTenantId(),
                        timetableId
                )
        );
    }


    @GetMapping("/academic-year/{academicYearId}")
    @PreAuthorize(
            "hasAuthority('school.timetable.read')"
    )
    public ApiResponse<List<Timetable>> findByAcademicYear(
            @PathVariable UUID academicYearId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-003",
                "Timetables retrieved by academic year.",
                service.findByAcademicYear(
                        identity.requireTenantId(),
                        academicYearId
                )
        );
    }


    @GetMapping("/campus/{campusId}")
    @PreAuthorize(
            "hasAuthority('school.timetable.read')"
    )
    public ApiResponse<List<Timetable>> findByCampus(
            @PathVariable UUID campusId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-004",
                "Timetables retrieved by campus.",
                service.findByCampus(
                        identity.requireTenantId(),
                        campusId
                )
        );
    }


    @PatchMapping("/{timetableId}/submit-review")
    @PreAuthorize(
            "hasAuthority('school.timetable.review')"
    )
    public ApiResponse<Timetable> submitForReview(
            @PathVariable UUID timetableId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID actorId =
                identity.requireUserId();

        return responses.success(
                "GT-SCHOOL-TIMETABLE-005",
                "Timetable submitted for review.",
                service.submitForReview(
                        tenantId,
                        timetableId,
                        actorId
                )
        );
    }


    @PatchMapping("/{timetableId}/approve")
    @PreAuthorize(
            "hasAuthority('school.timetable.approve')"
    )
    public ApiResponse<Timetable> approve(
            @PathVariable UUID timetableId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID actorId =
                identity.requireUserId();

        return responses.success(
                "GT-SCHOOL-TIMETABLE-006",
                "Timetable approved.",
                service.approve(
                        tenantId,
                        timetableId,
                        actorId
                )
        );
    }


    @PatchMapping("/{timetableId}/publish")
    @PreAuthorize(
            "hasAuthority('school.timetable.publish')"
    )
    public ApiResponse<Timetable> publish(
            @PathVariable UUID timetableId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID actorId =
                identity.requireUserId();

        return responses.success(
                "GT-SCHOOL-TIMETABLE-007",
                "Timetable published.",
                service.publish(
                        tenantId,
                        timetableId,
                        actorId
                )
        );
    }


    @PatchMapping("/{timetableId}/activate")
    @PreAuthorize(
            "hasAuthority('school.timetable.activate')"
    )
    public ApiResponse<Timetable> activate(
            @PathVariable UUID timetableId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID actorId =
                identity.requireUserId();

        return responses.success(
                "GT-SCHOOL-TIMETABLE-008",
                "Timetable activated.",
                service.activate(
                        tenantId,
                        timetableId,
                        actorId
                )
        );
    }


    @PatchMapping("/{timetableId}/suspend")
    @PreAuthorize(
            "hasAuthority('school.timetable.suspend')"
    )
    public ApiResponse<Timetable> suspend(
            @PathVariable UUID timetableId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID actorId =
                identity.requireUserId();

        return responses.success(
                "GT-SCHOOL-TIMETABLE-009",
                "Timetable suspended.",
                service.suspend(
                        tenantId,
                        timetableId,
                        actorId
                )
        );
    }
}
