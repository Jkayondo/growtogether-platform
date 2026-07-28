package africa.growtogether.platform.school.academic.curriculum;


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


    public CurriculumClassGradeController(
            CurriculumClassGradeService service,
            CurriculumVersionRepository versionRepository
    ) {
        this.service = service;
        this.versionRepository = versionRepository;
    }


    @PostMapping
    public CurriculumClassGrade create(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID tenantId,
            @RequestParam UUID classGradeId,
            @RequestParam Integer sequenceNumber
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
                sequenceNumber
        );
    }


    @GetMapping
    public List<CurriculumClassGrade> list(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID tenantId
    ) {

        return service.findByCurriculumVersion(
                tenantId,
                curriculumVersionId
        );
    }


    @GetMapping("/{classGradeId}")
    public CurriculumClassGrade get(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        return service.findMapping(
                tenantId,
                curriculumVersionId,
                classGradeId
        );
    }


    @PatchMapping("/{classGradeId}/archive")
    public CurriculumClassGrade archive(
            @PathVariable UUID curriculumVersionId,
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

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
