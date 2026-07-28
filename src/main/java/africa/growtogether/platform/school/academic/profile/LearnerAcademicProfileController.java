package africa.growtogether.platform.school.academic.profile;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/profile"
)
public class LearnerAcademicProfileController {


    private final LearnerAcademicProfileService service;


    private final LearnerAcademicProfileRepository repository;


    public LearnerAcademicProfileController(
            LearnerAcademicProfileService service,
            LearnerAcademicProfileRepository repository
    ) {

        this.service = service;
        this.repository = repository;
    }



    @PostMapping
    public LearnerAcademicProfile create(
            @RequestParam UUID tenantId,
            @RequestParam UUID learnerId
    ) {


        return service.create(
                tenantId,
                learnerId
        );
    }



    @GetMapping("/learner/{learnerId}")
    public LearnerAcademicProfile findByLearner(
            @RequestParam UUID tenantId,
            @PathVariable UUID learnerId
    ) {


        return service.findByLearner(
                tenantId,
                learnerId
        );
    }



    @GetMapping("/class/{classGradeId}")
    public List<LearnerAcademicProfile> findByClass(
            @RequestParam UUID tenantId,
            @PathVariable UUID classGradeId
    ) {


        return service.findByClassGrade(
                tenantId,
                classGradeId
        );
    }



    @GetMapping("/curriculum/{curriculumVersionId}")
    public List<LearnerAcademicProfile> findByCurriculum(
            @RequestParam UUID tenantId,
            @PathVariable UUID curriculumVersionId
    ) {


        return service.findByCurriculumVersion(
                tenantId,
                curriculumVersionId
        );
    }



    @PatchMapping("/{id}/archive")
    public LearnerAcademicProfile archive(
            @PathVariable UUID id
    ) {


        LearnerAcademicProfile profile =
                repository.findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Learner academic profile not found"
                                )
                        );


        return service.archive(
                profile
        );
    }

}
