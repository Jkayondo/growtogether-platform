package africa.growtogether.platform.school.academic.learner360.dashboard;


import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/school/teacher/intelligence")
public class TeacherIntelligenceDashboardController {


    private final TeacherIntelligenceDashboardService service;


    public TeacherIntelligenceDashboardController(
            TeacherIntelligenceDashboardService service
    ) {

        this.service = service;

    }


    @GetMapping("/learner/{learnerId}")
    public LearnerIntelligenceDashboardView getLearnerDashboard(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID learnerId
    ) {


        return service.getLearnerView(
                tenantId,
                learnerId
        );

    }

}
