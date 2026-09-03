package africa.growtogether.platform.school.assessment.examination.schedule;


import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/school/examination-schedules")
public class ExaminationScheduleController {


    private final ExaminationScheduleService service;


    public ExaminationScheduleController(
            ExaminationScheduleService service
    ) {

        this.service = service;

    }


    @GetMapping("/{reference}")
    public ExaminationSchedule get(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String reference
    ) {

        return service.get(
                tenantId,
                reference
        );

    }

}
