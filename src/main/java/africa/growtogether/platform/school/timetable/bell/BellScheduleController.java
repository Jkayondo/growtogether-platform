package africa.growtogether.platform.school.timetable.bell;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/timetables/bell-schedules"
)
public class BellScheduleController {

    private final BellScheduleService service;
    private final EnterpriseIdentityContext identity;
    private final ApiResponses responses;


    public BellScheduleController(
            BellScheduleService service,
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
    public ApiResponse<BellSchedule> create(
            @RequestParam UUID campusId,
            @RequestParam String scheduleCode,
            @RequestParam String scheduleName,
            @RequestParam(required = false) String description,
            @RequestParam String scheduleType,
            @RequestParam LocalDate effectiveFrom,
            @RequestParam(required = false) LocalDate effectiveTo,
            @RequestParam(required = false) Boolean mondayEnabled,
            @RequestParam(required = false) Boolean tuesdayEnabled,
            @RequestParam(required = false) Boolean wednesdayEnabled,
            @RequestParam(required = false) Boolean thursdayEnabled,
            @RequestParam(required = false) Boolean fridayEnabled,
            @RequestParam(required = false) Boolean saturdayEnabled,
            @RequestParam(required = false) Boolean sundayEnabled
    ) {

        CreateBellScheduleCommand command =
                new CreateBellScheduleCommand(
                        campusId,
                        scheduleCode,
                        scheduleName,
                        description,
                        scheduleType,
                        effectiveFrom,
                        effectiveTo,
                        mondayEnabled,
                        tuesdayEnabled,
                        wednesdayEnabled,
                        thursdayEnabled,
                        fridayEnabled,
                        saturdayEnabled,
                        sundayEnabled
                );

        return responses.success(
                "GT-SCHOOL-TIMETABLE-BELL-001",
                "Bell schedule created.",
                service.create(
                        identity.requireTenantId(),
                        command
                )
        );
    }


    @GetMapping("/{scheduleId}")
    @PreAuthorize(
            "hasAuthority('school.timetable.read')"
    )
    public ApiResponse<BellSchedule> get(
            @PathVariable UUID scheduleId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-BELL-002",
                "Bell schedule retrieved.",
                service.get(
                        identity.requireTenantId(),
                        scheduleId
                )
        );
    }


    @GetMapping("/campus/{campusId}")
    @PreAuthorize(
            "hasAuthority('school.timetable.read')"
    )
    public ApiResponse<List<BellSchedule>> findByCampus(
            @PathVariable UUID campusId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-BELL-003",
                "Bell schedules retrieved by campus.",
                service.findByCampus(
                        identity.requireTenantId(),
                        campusId
                )
        );
    }


    @PatchMapping("/{scheduleId}/approve")
    @PreAuthorize(
            "hasAuthority('school.timetable.approve')"
    )
    public ApiResponse<BellSchedule> approve(
            @PathVariable UUID scheduleId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-BELL-004",
                "Bell schedule approved.",
                service.approve(
                        identity.requireTenantId(),
                        scheduleId
                )
        );
    }


    @PatchMapping("/{scheduleId}/activate")
    @PreAuthorize(
            "hasAuthority('school.timetable.activate')"
    )
    public ApiResponse<BellSchedule> activate(
            @PathVariable UUID scheduleId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-BELL-005",
                "Bell schedule activated.",
                service.activate(
                        identity.requireTenantId(),
                        scheduleId
                )
        );
    }


    @PatchMapping("/{scheduleId}/suspend")
    @PreAuthorize(
            "hasAuthority('school.timetable.suspend')"
    )
    public ApiResponse<BellSchedule> suspend(
            @PathVariable UUID scheduleId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-BELL-006",
                "Bell schedule suspended.",
                service.suspend(
                        identity.requireTenantId(),
                        scheduleId
                )
        );
    }
}
