package africa.growtogether.platform.school.academic.learner360.support;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learner360/support-reviews")
public class LearnerSupportReviewController {

    private final LearnerSupportReviewService service;


    public LearnerSupportReviewController(
            LearnerSupportReviewService service
    ) {
        this.service = service;
    }


    @PostMapping
    public LearnerSupportReview createReview(
            @RequestBody LearnerSupportReview review
    ) {
        return service.createReview(review);
    }


    @GetMapping("/{id}")
    public LearnerSupportReview getReview(
            @PathVariable UUID id
    ) {
        return service.getReview(id);
    }


    @GetMapping("/support-plan/{supportPlanId}")
    public List<LearnerSupportReview> getReviewsForPlan(
            @PathVariable UUID supportPlanId
    ) {
        return service.getReviewsForPlan(supportPlanId);
    }
}
