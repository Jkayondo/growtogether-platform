package africa.growtogether.platform.school.academic.learner360.support;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learner360/support-plans")
public class LearnerSupportPlanController {

    private final LearnerSupportPlanService service;

    public LearnerSupportPlanController(
            LearnerSupportPlanService service
    ) {
        this.service = service;
    }


    @GetMapping("/{id}")
    public LearnerSupportPlan getPlan(
            @PathVariable UUID id
    ) {
        return service.getPlan(id);
    }


    @GetMapping("/learner/{learnerId}")
    public List<LearnerSupportPlan> getLearnerPlans(
            @PathVariable UUID learnerId
    ) {
        return service.getPlansForLearner(learnerId);
    }


    @PutMapping("/{id}/status")
    public LearnerSupportPlan changeStatus(
            @PathVariable UUID id,
            @RequestParam String status
    ) {
        return service.changeStatus(
                id,
                status
        );
    }
}
