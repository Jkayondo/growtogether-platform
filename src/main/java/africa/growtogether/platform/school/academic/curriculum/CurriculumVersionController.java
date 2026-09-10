package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/curriculum/{curriculumId}/versions")
public class CurriculumVersionController {


    private final CurriculumVersionService service;
    private final CurriculumRepository curriculumRepository;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;


    public CurriculumVersionController(
            CurriculumVersionService service,
            CurriculumRepository curriculumRepository,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.curriculumRepository = curriculumRepository;
        this.responses = responses;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.version.create')")
    public ApiResponse<CurriculumVersion> create(
            @PathVariable UUID curriculumId,
            @RequestParam UUID tenantId,
            @RequestParam String versionCode,
            @RequestParam String versionName,
            @RequestParam LocalDate effectiveFrom
    ) {

        identity.requireTenant(tenantId);

        Curriculum curriculum =
                curriculumRepository.findByTenantIdAndId(
                        tenantId,
                        curriculumId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Curriculum not found"
                        )
                );


        CurriculumVersion version =
                service.create(
                        tenantId,
                        curriculum,
                        versionCode,
                        versionName,
                        effectiveFrom
                );


        return responses.success(
                "GT-SCHOOL-CURRICULUM-VERSION-001",
                "Curriculum version created.",
                version
        );
    }


    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.version.read')")
    public ApiResponse<List<CurriculumVersion>> list(
            @PathVariable UUID curriculumId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CURRICULUM-VERSION-002",
                "Curriculum versions retrieved.",
                service.findByCurriculum(
                        tenantId,
                        curriculumId
                )
        );
    }


    @GetMapping("/{versionCode}")
    @PreAuthorize("hasAuthority('school.academic.curriculum.version.read')")
    public ApiResponse<CurriculumVersion> get(
            @PathVariable UUID curriculumId,
            @PathVariable String versionCode,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CURRICULUM-VERSION-003",
                "Curriculum version retrieved.",
                service.findByCode(
                        tenantId,
                        curriculumId,
                        versionCode
                )
        );
    }


    @PatchMapping("/{versionCode}/approve")
    @PreAuthorize("hasAuthority('school.academic.curriculum.version.manage')")
    public ApiResponse<CurriculumVersion> approve(
            @PathVariable UUID curriculumId,
            @PathVariable String versionCode,
            @RequestParam UUID tenantId,
            @RequestParam String approvalReference
    ) {

        identity.requireTenant(tenantId);

        CurriculumVersion version =
                service.findByCode(
                        tenantId,
                        curriculumId,
                        versionCode
                );


        return responses.success(
                "GT-SCHOOL-CURRICULUM-VERSION-004",
                "Curriculum version approved.",
                service.approve(
                        version,
                        identity.requireUserId(),
                        approvalReference
                )
        );
    }


    @PatchMapping("/{versionCode}/activate")
    @PreAuthorize("hasAuthority('school.academic.curriculum.version.manage')")
    public ApiResponse<CurriculumVersion> activate(
            @PathVariable UUID curriculumId,
            @PathVariable String versionCode,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        CurriculumVersion version =
                service.findByCode(
                        tenantId,
                        curriculumId,
                        versionCode
                );


        return responses.success(
                "GT-SCHOOL-CURRICULUM-VERSION-005",
                "Curriculum version activated.",
                service.activate(
                        version
                )
        );
    }

}
