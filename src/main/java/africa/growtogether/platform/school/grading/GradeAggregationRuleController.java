package africa.growtogether.platform.school.grading;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/grading-aggregation-rules")
public class GradeAggregationRuleController {


    private final GradeAggregationRuleService service;


    public GradeAggregationRuleController(
            GradeAggregationRuleService service
    ) {

        this.service = service;

    }



    @GetMapping("/scheme/{gradingSchemeId}")
    public List<GradeAggregationRule> byScheme(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID gradingSchemeId
    ) {

        return service.findByScheme(
                tenantId,
                gradingSchemeId
        );

    }



    @GetMapping("/{ruleCode}")
    public GradeAggregationRule find(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String ruleCode
    ) {

        return service.findByCode(
                tenantId,
                ruleCode
        );

    }



    @PatchMapping("/{ruleCode}/activate")
    public GradeAggregationRule activate(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String ruleCode
    ) {

        return service.activate(
                tenantId,
                ruleCode
        );

    }



    @PatchMapping("/{ruleCode}/archive")
    public GradeAggregationRule archive(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String ruleCode
    ) {

        return service.archive(
                tenantId,
                ruleCode
        );

    }

}
