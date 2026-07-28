package africa.growtogether.platform.school.academic.learner360;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/learner360"
)
public class Learner360ProfileController {


    private final Learner360ProfileService service;

    private final Learner360ProfileRepository repository;


    public Learner360ProfileController(
            Learner360ProfileService service,
            Learner360ProfileRepository repository
    ) {

        this.service = service;
        this.repository = repository;
    }



    @PostMapping
    public Learner360Profile create(
            @RequestParam UUID tenantId,
            @RequestParam UUID learnerId,
            @RequestParam UUID academicProfileId
    ) {

        return service.create(
                tenantId,
                learnerId,
                academicProfileId
        );
    }



    @GetMapping("/learner/{learnerId}")
    public Learner360Profile findByLearner(
            @RequestParam UUID tenantId,
            @PathVariable UUID learnerId
    ) {

        return service.findByLearner(
                tenantId,
                learnerId
        );
    }



    @GetMapping("/class/{classGradeId}")
    public List<Learner360Profile> findByClass(
            @RequestParam UUID tenantId,
            @PathVariable UUID classGradeId
    ) {

        return service.findByClass(
                tenantId,
                classGradeId
        );
    }



    @GetMapping("/curriculum/{curriculumVersionId}")
    public List<Learner360Profile> findByCurriculum(
            @RequestParam UUID tenantId,
            @PathVariable UUID curriculumVersionId
    ) {

        return service.findByCurriculum(
                tenantId,
                curriculumVersionId
        );
    }



    @PatchMapping("/{id}/risk")
    public Learner360Profile updateRisk(
            @PathVariable UUID id,
            @RequestParam String riskLevel
    ) {


        Learner360Profile profile =
                repository.findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Learner 360 profile not found"
                                )
                        );


        return service.updateLearningRisk(
                profile,
                riskLevel
        );
    }



    @PatchMapping("/{id}/summary")
    public Learner360Profile updateSummary(
            @PathVariable UUID id,
            @RequestParam String summary
    ) {


        Learner360Profile profile =
                repository.findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Learner 360 profile not found"
                                )
                        );


        return service.updateGrowthSummary(
                profile,
                summary
        );
    }



    @PatchMapping("/{id}/archive")
    public Learner360Profile archive(
            @PathVariable UUID id
    ) {


        Learner360Profile profile =
                repository.findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Learner 360 profile not found"
                                )
                        );


        return service.archive(
                profile
        );
    }

}
