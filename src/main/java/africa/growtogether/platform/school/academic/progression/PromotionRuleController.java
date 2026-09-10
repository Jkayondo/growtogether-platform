package africa.growtogether.platform.school.academic.progression;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/school/promotion-rules")
public class PromotionRuleController {


    private final PromotionRuleService service;


    public PromotionRuleController(
            PromotionRuleService service
    ) {

        this.service = service;

    }


    @PostMapping
    public ResponseEntity<PromotionRule> create(
            @RequestParam UUID tenantId,
            @RequestParam String ruleCode,
            @RequestParam String ruleName
    ) {


        return ResponseEntity.ok(
                service.create(
                        tenantId,
                        ruleCode,
                        ruleName
                )
        );

    }


    @GetMapping("/{ruleCode}")
    public ResponseEntity<PromotionRule> find(
            @RequestParam UUID tenantId,
            @PathVariable String ruleCode
    ) {


        return ResponseEntity.ok(
                service.findByCode(
                        tenantId,
                        ruleCode
                )
        );

    }


    @GetMapping
    public ResponseEntity<List<PromotionRule>> activeRules(
            @RequestParam UUID tenantId
    ) {


        return ResponseEntity.ok(
                service.findActiveRules(
                        tenantId
                )
        );

    }


    @PostMapping("/{ruleCode}/activate")
    public ResponseEntity<PromotionRule> activate(
            @RequestParam UUID tenantId,
            @PathVariable String ruleCode
    ) {


        return ResponseEntity.ok(
                service.activate(
                        tenantId,
                        ruleCode
                )
        );

    }


    @PostMapping("/{ruleCode}/deactivate")
    public ResponseEntity<PromotionRule> deactivate(
            @RequestParam UUID tenantId,
            @PathVariable String ruleCode
    ) {


        return ResponseEntity.ok(
                service.deactivate(
                        tenantId,
                        ruleCode
                )
        );

    }

}
