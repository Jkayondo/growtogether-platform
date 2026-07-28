package africa.growtogether.platform.school.academic.curriculum;


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


    public CurriculumSubjectController(
            CurriculumSubjectService service,
            CurriculumVersionRepository versionRepository
    ) {
        this.service = service;
        this.versionRepository = versionRepository;
    }


    @PostMapping
    public CurriculumSubject create(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId,
            @RequestParam UUID subjectId
    ) {

        CurriculumVersion version =
                versionRepository.findById(
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
    public List<CurriculumSubject> list(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        return service.findByGrade(
                tenantId,
                curriculumVersionId,
                classGradeId
        );
    }


    @GetMapping("/{subjectId}")
    public CurriculumSubject get(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @PathVariable UUID subjectId,
            @RequestParam UUID tenantId
    ) {

        return service.findMapping(
                tenantId,
                curriculumVersionId,
                classGradeId,
                subjectId
        );
    }


    @PatchMapping("/{subjectId}/requirement")
    public CurriculumSubject changeRequirement(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @PathVariable UUID subjectId,
            @RequestParam UUID tenantId,
            @RequestParam String requirement
    ) {

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
