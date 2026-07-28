package africa.growtogether.platform.school.academic.learner360.learner;


import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/school/learner/intelligence")
public class LearnerSelfViewController {


    private final LearnerSelfViewService service;


    public LearnerSelfViewController(
            LearnerSelfViewService service
    ) {

        this.service = service;

    }


    @GetMapping("/{learnerId}")
    public LearnerSelfView getLearnerView(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID learnerId
    ) {


        return service.getLearnerView(
                tenantId,
                learnerId
        );

    }

}
