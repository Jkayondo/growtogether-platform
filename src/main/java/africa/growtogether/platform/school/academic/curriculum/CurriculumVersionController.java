package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;

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


    public CurriculumVersionController(
            CurriculumVersionService service,
            CurriculumRepository curriculumRepository,
            ApiResponses responses
    ) {
        this.service = service;
        this.curriculumRepository = curriculumRepository;
        this.responses = responses;
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


        Curriculum curriculum =
                curriculumRepository.findById(
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
            @RequestParam UUID approvedBy,
            @RequestParam String approvalReference
    ) {


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
                        approvedBy,
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
