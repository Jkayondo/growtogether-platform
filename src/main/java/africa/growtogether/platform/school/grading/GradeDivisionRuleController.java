package africa.growtogether.platform.school.grading;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/grading-division-rules")
public class GradeDivisionRuleController {


    private final GradeDivisionRuleService service;


    public GradeDivisionRuleController(
            GradeDivisionRuleService service
    ) {

        this.service = service;

    }



    @GetMapping("/scheme/{gradingSchemeId}")
    public List<GradeDivisionRule> byScheme(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID gradingSchemeId
    ) {

        return service.findByScheme(
                tenantId,
                gradingSchemeId
        );

    }



    @GetMapping("/scheme/{gradingSchemeId}/aggregate/{aggregate}")
    public GradeDivisionRule findDivision(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID gradingSchemeId,
            @PathVariable Integer aggregate
    ) {

        return service.findDivisionForAggregate(
                tenantId,
                gradingSchemeId,
                aggregate
        );

    }



    @PatchMapping("/scheme/{gradingSchemeId}/{divisionCode}/archive")
    public GradeDivisionRule archive(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID gradingSchemeId,
            @PathVariable String divisionCode
    ) {

        return service.archive(
                tenantId,
                gradingSchemeId,
                divisionCode
        );

    }

}
