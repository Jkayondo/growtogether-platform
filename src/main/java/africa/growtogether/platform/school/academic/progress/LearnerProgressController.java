package africa.growtogether.platform.school.academic.progress;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/learner-progress"
)
public class LearnerProgressController {


    private final LearnerProgressService service;


    private final LearnerProgressRepository repository;


    public LearnerProgressController(
            LearnerProgressService service,
            LearnerProgressRepository repository
    ) {

        this.service = service;
        this.repository = repository;
    }


    @PostMapping
    public LearnerProgress create(
            @RequestParam UUID tenantId,
            @RequestParam UUID learnerId,
            @RequestParam UUID assessmentId,
            @RequestParam UUID learningOutcomeId,
            @RequestParam Double score,
            @RequestParam Double maximumScore
    ) {


        return service.create(
                tenantId,
                learnerId,
                assessmentId,
                learningOutcomeId,
                score,
                maximumScore
        );
    }



    @GetMapping("/learner/{learnerId}")
    public List<LearnerProgress> findByLearner(
            @RequestParam UUID tenantId,
            @PathVariable UUID learnerId
    ) {


        return service.findByLearner(
                tenantId,
                learnerId
        );
    }



    @GetMapping("/outcome/{learningOutcomeId}")
    public List<LearnerProgress> findByOutcome(
            @RequestParam UUID tenantId,
            @PathVariable UUID learningOutcomeId
    ) {


        return service.findByOutcome(
                tenantId,
                learningOutcomeId
        );
    }



    @PatchMapping("/{id}/archive")
    public LearnerProgress archive(
            @RequestParam UUID tenantId,
            @PathVariable UUID id
    ) {


        LearnerProgress progress =
                repository.findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Learner progress not found"
                                )
                        );


        return service.archive(
                progress
        );
    }

}
