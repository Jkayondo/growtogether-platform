package africa.growtogether.platform.school.academic.learner360.intelligence;


import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/school/learner-intelligence")
public class LearnerIntelligenceController {


    private final LearnerIntelligenceSnapshotRepository repository;


    public LearnerIntelligenceController(
            LearnerIntelligenceSnapshotRepository repository
    ) {

        this.repository = repository;

    }


    @GetMapping("/{learnerId}")
    public LearnerIntelligenceSnapshot getLearnerIntelligence(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID learnerId
    ) {


        return repository
                .findByTenantIdAndLearnerId(
                        tenantId,
                        learnerId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Learner intelligence snapshot not found"
                                )
                );

    }

}
