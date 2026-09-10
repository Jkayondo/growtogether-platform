package africa.growtogether.platform.school.grading;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/competency-descriptors")
public class CompetencyDescriptorController {


    private final CompetencyDescriptorService service;


    public CompetencyDescriptorController(
            CompetencyDescriptorService service
    ) {

        this.service = service;

    }



    @GetMapping("/scheme/{gradingSchemeId}")
    public List<CompetencyDescriptor> byScheme(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID gradingSchemeId
    ) {

        return service.findByScheme(
                tenantId,
                gradingSchemeId
        );

    }



    @GetMapping("/scheme/{gradingSchemeId}/{descriptorCode}")
    public CompetencyDescriptor find(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID gradingSchemeId,
            @PathVariable String descriptorCode
    ) {

        return service.findByCode(
                tenantId,
                gradingSchemeId,
                descriptorCode
        );

    }



    @PatchMapping("/scheme/{gradingSchemeId}/{descriptorCode}/archive")
    public CompetencyDescriptor archive(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID gradingSchemeId,
            @PathVariable String descriptorCode
    ) {

        return service.archive(
                tenantId,
                gradingSchemeId,
                descriptorCode
        );

    }

}
