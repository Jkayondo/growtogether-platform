package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/curriculum/{curriculumVersionId}/learning-areas"
)
public class CurriculumLearningAreaController {


    private final CurriculumLearningAreaService service;
    private final ApiResponses responses;


    public CurriculumLearningAreaController(
            CurriculumLearningAreaService service,
            ApiResponses responses
    ) {
        this.service = service;
        this.responses = responses;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.learning-area.create')")
    public ApiResponse<CurriculumLearningArea> create(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID tenantId,
            @RequestParam String learningAreaCode,
            @RequestParam String learningAreaName,
            @RequestParam String learningAreaType,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "1") Integer sequenceNumber
    ) {


        return responses.success(
                "GT-SCHOOL-LEARNING-AREA-001",
                "Learning area created.",
                service.create(
                        tenantId,
                        curriculumVersionId,
                        learningAreaCode,
                        learningAreaName,
                        learningAreaType,
                        description,
                        sequenceNumber
                )
        );
    }



    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.learning-area.read')")
    public ApiResponse<List<CurriculumLearningArea>> list(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID tenantId
    ) {


        return responses.success(
                "GT-SCHOOL-LEARNING-AREA-002",
                "Learning areas retrieved.",
                service.findByVersion(
                        tenantId,
                        curriculumVersionId
                )
        );
    }



    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('school.academic.curriculum.learning-area.read')")
    public ApiResponse<CurriculumLearningArea> get(
            @PathVariable UUID curriculumVersionId,
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {


        return responses.success(
                "GT-SCHOOL-LEARNING-AREA-003",
                "Learning area retrieved.",
                service.findByCode(
                        tenantId,
                        curriculumVersionId,
                        code
                )
        );
    }



    @PatchMapping("/{code}/activate")
    @PreAuthorize("hasAuthority('school.academic.curriculum.learning-area.manage')")
    public ApiResponse<CurriculumLearningArea> activate(
            @PathVariable UUID curriculumVersionId,
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {


        CurriculumLearningArea learningArea =
                service.findByCode(
                        tenantId,
                        curriculumVersionId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-LEARNING-AREA-004",
                "Learning area activated.",
                service.activate(
                        learningArea
                )
        );
    }



    @PatchMapping("/{code}/deactivate")
    @PreAuthorize("hasAuthority('school.academic.curriculum.learning-area.manage')")
    public ApiResponse<CurriculumLearningArea> deactivate(
            @PathVariable UUID curriculumVersionId,
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {


        CurriculumLearningArea learningArea =
                service.findByCode(
                        tenantId,
                        curriculumVersionId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-LEARNING-AREA-005",
                "Learning area deactivated.",
                service.deactivate(
                        learningArea
                )
        );
    }

}
