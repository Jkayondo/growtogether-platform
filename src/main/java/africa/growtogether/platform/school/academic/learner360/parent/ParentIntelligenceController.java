package africa.growtogether.platform.school.academic.learner360.parent;


import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/school/parent/intelligence")
public class ParentIntelligenceController {


    private final ParentIntelligenceService service;


    public ParentIntelligenceController(
            ParentIntelligenceService service
    ) {

        this.service = service;

    }


    @GetMapping("/learner/{learnerId}")
    public ParentIntelligenceView getParentIntelligence(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID learnerId
    ) {


        return service.getParentView(
                tenantId,
                learnerId
        );

    }

}
