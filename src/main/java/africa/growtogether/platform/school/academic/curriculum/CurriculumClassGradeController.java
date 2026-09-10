package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/curriculum-version/{curriculumVersionId}/grades"
)
public class CurriculumClassGradeController {


    private final CurriculumClassGradeService service;
    private final CurriculumVersionRepository versionRepository;
    private final EnterpriseIdentityContext identity;


    public CurriculumClassGradeController(
            CurriculumClassGradeService service,
            CurriculumVersionRepository versionRepository,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.versionRepository = versionRepository;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.version.create')")
    public CurriculumClassGrade create(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID tenantId,
            @RequestParam UUID classGradeId,
            @RequestParam Integer sequenceNumber
    ) {

        identity.requireTenant(tenantId);

        CurriculumVersion version =
                versionRepository.findByTenantIdAndId(
                        tenantId,
                        curriculumVersionId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Curriculum version not found"
                        )
                );


        return service.create(
                tenantId,
                version,
                classGradeId,
                sequenceNumber
        );
    }


    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.version.read')")
    public List<CurriculumClassGrade> list(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return service.findByCurriculumVersion(
                tenantId,
                curriculumVersionId
        );
    }


    @GetMapping("/{classGradeId}")
    @PreAuthorize("hasAuthority('school.academic.curriculum.version.read')")
    public CurriculumClassGrade get(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return service.findMapping(
                tenantId,
                curriculumVersionId,
                classGradeId
        );
    }


    @PatchMapping("/{classGradeId}/archive")
    @PreAuthorize("hasAuthority('school.academic.curriculum.version.manage')")
    public CurriculumClassGrade archive(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        CurriculumClassGrade mapping =
                service.findMapping(
                        tenantId,
                        curriculumVersionId,
                        classGradeId
                );


        return service.archive(
                mapping
        );
    }

}
