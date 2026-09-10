package africa.growtogether.platform.school.grading;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/education-grading-profiles")
public class EducationGradingProfileController {


    private final EducationGradingProfileService service;


    public EducationGradingProfileController(
            EducationGradingProfileService service
    ) {

        this.service = service;

    }



    @GetMapping("/active")
    public List<EducationGradingProfile> active(
            @RequestHeader("X-Tenant-ID") UUID tenantId
    ) {

        return service.findActive(
                tenantId
        );

    }



    @GetMapping("/{profileCode}")
    public EducationGradingProfile find(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String profileCode
    ) {

        return service.findByCode(
                tenantId,
                profileCode
        );

    }



    @GetMapping("/country/{countryCode}")
    public List<EducationGradingProfile> byCountry(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String countryCode
    ) {

        return service.findByCountry(
                tenantId,
                countryCode
        );

    }



    @PatchMapping("/{profileCode}/activate")
    public EducationGradingProfile activate(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String profileCode
    ) {

        return service.activate(
                tenantId,
                profileCode
        );

    }



    @PatchMapping("/{profileCode}/archive")
    public EducationGradingProfile archive(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String profileCode
    ) {

        return service.archive(
                tenantId,
                profileCode
        );

    }

}
