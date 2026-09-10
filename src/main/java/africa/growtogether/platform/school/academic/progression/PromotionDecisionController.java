package africa.growtogether.platform.school.academic.progression;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/school/promotion-decisions")
public class PromotionDecisionController {


    private final PromotionDecisionService service;


    public PromotionDecisionController(
            PromotionDecisionService service
    ) {

        this.service = service;

    }


    @PostMapping
    public ResponseEntity<PromotionDecision> create(
            @RequestParam UUID tenantId,
            @RequestParam UUID learnerId,
            @RequestParam UUID promotionRuleId
    ) {


        return ResponseEntity.ok(
                service.create(
                        tenantId,
                        learnerId,
                        promotionRuleId
                )
        );

    }


    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<PromotionDecision>> findByLearner(
            @RequestParam UUID tenantId,
            @PathVariable UUID learnerId
    ) {


        return ResponseEntity.ok(
                service.findByLearner(
                        tenantId,
                        learnerId
                )
        );

    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<PromotionDecision>> findByStatus(
            @RequestParam UUID tenantId,
            @PathVariable String status
    ) {


        return ResponseEntity.ok(
                service.findByStatus(
                        tenantId,
                        status
                )
        );

    }


    @PostMapping("/{id}/recommend")
    public ResponseEntity<PromotionDecision> recommend(
            @PathVariable UUID id
    ) {


        return ResponseEntity.ok(
                service.recommend(
                        find(id)
                )
        );

    }


    @PostMapping("/{id}/approve")
    public ResponseEntity<PromotionDecision> approve(
            @PathVariable UUID id
    ) {


        return ResponseEntity.ok(
                service.approve(
                        find(id)
                )
        );

    }


    @PostMapping("/{id}/promote")
    public ResponseEntity<PromotionDecision> promote(
            @PathVariable UUID id
    ) {


        return ResponseEntity.ok(
                service.promote(
                        find(id)
                )
        );

    }


    @PostMapping("/{id}/repeat")
    public ResponseEntity<PromotionDecision> repeat(
            @PathVariable UUID id
    ) {


        return ResponseEntity.ok(
                service.repeat(
                        find(id)
                )
        );

    }


    @PostMapping("/{id}/reject")
    public ResponseEntity<PromotionDecision> reject(
            @PathVariable UUID id
    ) {


        return ResponseEntity.ok(
                service.reject(
                        find(id)
                )
        );

    }


    private PromotionDecision find(
            UUID id
    ) {

        return service.findById(id);

    }

}
