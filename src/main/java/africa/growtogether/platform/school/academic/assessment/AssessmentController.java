package africa.growtogether.platform.school.academic.assessment;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments"
)
public class AssessmentController {


    private final AssessmentService service;


    public AssessmentController(
            AssessmentService service
    ) {
        this.service = service;
    }


    @PostMapping
    public Assessment create(
            @PathVariable UUID learningOutcomeId,
            @RequestParam UUID tenantId,
            @RequestParam String assessmentCode,
            @RequestParam String assessmentTitle
    ) {

        return service.create(
                tenantId,
                learningOutcomeId,
                assessmentCode,
                assessmentTitle
        );
    }


    @GetMapping
    public List<Assessment> list(
            @PathVariable UUID learningOutcomeId,
            @RequestParam UUID tenantId
    ) {

        return service.findByLearningOutcome(
                tenantId,
                learningOutcomeId
        );
    }


    @GetMapping("/{assessmentCode}")
    public Assessment get(
            @PathVariable UUID learningOutcomeId,
            @PathVariable String assessmentCode,
            @RequestParam UUID tenantId
    ) {

        return service.findByCode(
                tenantId,
                learningOutcomeId,
                assessmentCode
        );
    }


    @PatchMapping("/{assessmentCode}/archive")
    public Assessment archive(
            @PathVariable UUID learningOutcomeId,
            @PathVariable String assessmentCode,
            @RequestParam UUID tenantId
    ) {

        Assessment assessment =
                service.findByCode(
                        tenantId,
                        learningOutcomeId,
                        assessmentCode
                );


        return service.archive(
                assessment
        );
    }

}
