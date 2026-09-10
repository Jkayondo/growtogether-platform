package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/curriculum-version/{curriculumVersionId}/grades/{classGradeId}/subjects"
)
public class CurriculumSubjectController {


    private final CurriculumSubjectService service;
    private final CurriculumVersionRepository versionRepository;
    private final EnterpriseIdentityContext identity;


    public CurriculumSubjectController(
            CurriculumSubjectService service,
            CurriculumVersionRepository versionRepository,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.versionRepository = versionRepository;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.subject.create')")
    public CurriculumSubject create(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId,
            @RequestParam UUID subjectId
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
                subjectId
        );
    }


    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.subject.read')")
    public List<CurriculumSubject> list(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return service.findByGrade(
                tenantId,
                curriculumVersionId,
                classGradeId
        );
    }


    @GetMapping("/{subjectId}")
    @PreAuthorize("hasAuthority('school.academic.subject.read')")
    public CurriculumSubject get(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @PathVariable UUID subjectId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return service.findMapping(
                tenantId,
                curriculumVersionId,
                classGradeId,
                subjectId
        );
    }


    @PatchMapping("/{subjectId}/requirement")
    @PreAuthorize("hasAuthority('school.academic.subject.manage')")
    public CurriculumSubject changeRequirement(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @PathVariable UUID subjectId,
            @RequestParam UUID tenantId,
            @RequestParam String requirement
    ) {

        identity.requireTenant(tenantId);

        CurriculumSubject subject =
                service.findMapping(
                        tenantId,
                        curriculumVersionId,
                        classGradeId,
                        subjectId
                );


        return service.changeRequirement(
                subject,
                requirement
        );
    }

}
