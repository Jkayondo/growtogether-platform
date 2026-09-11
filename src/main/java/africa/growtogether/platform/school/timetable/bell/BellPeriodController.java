package africa.growtogether.platform.school.timetable.bell;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/timetables/bell-periods"
)
public class BellPeriodController {

    private final BellPeriodService service;
    private final EnterpriseIdentityContext identity;
    private final ApiResponses responses;


    public BellPeriodController(
            BellPeriodService service,
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
    public ApiResponse<BellPeriod> create(
            @RequestParam UUID bellScheduleId,
            @RequestParam String periodCode,
            @RequestParam String periodName,
            @RequestParam Integer sequenceNumber,
            @RequestParam String periodType,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime,
            @RequestParam(required = false) Integer instructionalMinutes,
            @RequestParam(required = false) Boolean attendanceRequired,
            @RequestParam(required = false) Boolean schedulingAllowed
    ) {

        CreateBellPeriodCommand command =
                new CreateBellPeriodCommand(
                        bellScheduleId,
                        periodCode,
                        periodName,
                        sequenceNumber,
                        periodType,
                        startTime,
                        endTime,
                        instructionalMinutes,
                        attendanceRequired,
                        schedulingAllowed
                );

        return responses.success(
                "GT-SCHOOL-TIMETABLE-BELL-PERIOD-001",
                "Bell period created.",
                service.create(
                        identity.requireTenantId(),
                        command
                )
        );
    }


    @GetMapping("/{periodId}")
    @PreAuthorize(
            "hasAuthority('school.timetable.read')"
    )
    public ApiResponse<BellPeriod> get(
            @PathVariable UUID periodId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-BELL-PERIOD-002",
                "Bell period retrieved.",
                service.get(
                        identity.requireTenantId(),
                        periodId
                )
        );
    }


    @GetMapping("/schedule/{scheduleId}")
    @PreAuthorize(
            "hasAuthority('school.timetable.read')"
    )
    public ApiResponse<List<BellPeriod>> findBySchedule(
            @PathVariable UUID scheduleId
    ) {

        return responses.success(
                "GT-SCHOOL-TIMETABLE-BELL-PERIOD-003",
                "Bell periods retrieved by schedule.",
                service.findBySchedule(
                        identity.requireTenantId(),
                        scheduleId
                )
        );
    }
}
