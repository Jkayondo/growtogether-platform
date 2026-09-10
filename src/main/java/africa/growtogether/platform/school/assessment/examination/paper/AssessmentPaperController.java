package africa.growtogether.platform.school.assessment.examination.paper;


import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/school/assessment-papers")
public class AssessmentPaperController {


    private final AssessmentPaperService service;


    public AssessmentPaperController(
            AssessmentPaperService service
    ) {

        this.service = service;

    }


    @PostMapping
    public AssessmentPaper create(
            @RequestBody AssessmentPaper paper
    ) {

        return service.create(paper);

    }


    @GetMapping("/{id}")
    public AssessmentPaper get(
            @PathVariable UUID id
    ) {

        return service.get(id);

    }


    @GetMapping("/code/{paperCode}")
    public AssessmentPaper getByCode(
            @RequestParam UUID tenantId,
            @PathVariable String paperCode
    ) {

        return service.getByCode(
                tenantId,
                paperCode
        );

    }


    @PostMapping("/{id}/submit-review")
    public AssessmentPaper submitForReview(
            @PathVariable UUID id
    ) {

        return service.submitForReview(id);

    }


    @PostMapping("/{id}/moderate")
    public AssessmentPaper moderate(
            @PathVariable UUID id
    ) {

        return service.moderate(id);

    }


    @PostMapping("/{id}/approve")
    public AssessmentPaper approve(
            @PathVariable UUID id
    ) {

        return service.approve(id);

    }


    @PostMapping("/{id}/schedule")
    public AssessmentPaper schedule(
            @PathVariable UUID id
    ) {

        return service.schedule(id);

    }


    @PostMapping("/{id}/complete")
    public AssessmentPaper complete(
            @PathVariable UUID id
    ) {

        return service.complete(id);

    }

}
