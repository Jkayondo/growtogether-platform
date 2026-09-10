package africa.growtogether.platform.school.grading;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/grading-schemes")
public class GradingSchemeController {


    private final GradingSchemeService service;


    public GradingSchemeController(
            GradingSchemeService service
    ) {

        this.service = service;

    }



    @GetMapping("/active")
    public List<GradingScheme> active(
            @RequestHeader("X-Tenant-ID") UUID tenantId
    ) {

        return service.findActive(
                tenantId
        );

    }



    @GetMapping("/{schemeCode}")
    public GradingScheme find(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String schemeCode
    ) {

        return service.findByCode(
                tenantId,
                schemeCode
        );

    }



    @PatchMapping("/{schemeCode}/activate")
    public GradingScheme activate(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String schemeCode
    ) {

        return service.activate(
                tenantId,
                schemeCode
        );

    }



    @PatchMapping("/{schemeCode}/archive")
    public GradingScheme archive(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String schemeCode
    ) {

        return service.archive(
                tenantId,
                schemeCode
        );

    }

}
