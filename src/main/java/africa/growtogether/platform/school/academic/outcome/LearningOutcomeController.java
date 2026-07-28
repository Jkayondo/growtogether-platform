package africa.growtogether.platform.school.academic.outcome;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/curriculum-version/{curriculumVersionId}/grades/{classGradeId}/subjects/{subjectId}/outcomes"
)
public class LearningOutcomeController {


    private final LearningOutcomeService service;


    public LearningOutcomeController(
            LearningOutcomeService service
    ) {
        this.service = service;
    }


    @PostMapping
    public LearningOutcome create(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @PathVariable UUID subjectId,
            @RequestParam UUID tenantId,
            @RequestParam String outcomeCode,
            @RequestParam String outcomeTitle,
            @RequestParam Integer sequenceNumber
    ) {

        return service.create(
                tenantId,
                curriculumVersionId,
                classGradeId,
                subjectId,
                outcomeCode,
                outcomeTitle,
                sequenceNumber
        );
    }


    @GetMapping
    public List<LearningOutcome> list(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID tenantId
    ) {

        return service.findByCurriculumVersion(
                tenantId,
                curriculumVersionId
        );
    }


    @GetMapping("/{outcomeCode}")
    public LearningOutcome get(
            @PathVariable UUID curriculumVersionId,
            @PathVariable String outcomeCode,
            @RequestParam UUID tenantId
    ) {

        return service.findByCode(
                tenantId,
                curriculumVersionId,
                outcomeCode
        );
    }


    @GetMapping("/by-subject")
    public List<LearningOutcome> listBySubject(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID subjectId,
            @RequestParam UUID tenantId
    ) {

        return service.findBySubject(
                tenantId,
                curriculumVersionId,
                subjectId
        );
    }


    @PatchMapping("/{outcomeCode}/archive")
    public LearningOutcome archive(
            @PathVariable UUID curriculumVersionId,
            @PathVariable String outcomeCode,
            @RequestParam UUID tenantId
    ) {

        LearningOutcome outcome =
                service.findByCode(
                        tenantId,
                        curriculumVersionId,
                        outcomeCode
                );


        return service.archive(
                outcome
        );
    }

}
