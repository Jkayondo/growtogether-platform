package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/education-levels"
)
public class EducationLevelController {


    private final EducationLevelService service;
    private final ApiResponses responses;


    public EducationLevelController(
            EducationLevelService service,
            ApiResponses responses
    ) {

        this.service = service;
        this.responses = responses;

    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.create')")
    public ApiResponse<EducationLevel> create(
            @RequestParam UUID tenantId,
            @RequestParam String levelCode,
            @RequestParam String levelName,
            @RequestParam(required = false) String description,
            @RequestParam Integer sequenceNumber
    ) {

        return responses.success(
                "GT-SCHOOL-EDUCATION-LEVEL-001",
                "Education level created.",
                service.create(
                        tenantId,
                        levelCode,
                        levelName,
                        description,
                        sequenceNumber
                )
        );

    }


    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.read')")
    public ApiResponse<List<EducationLevel>> list(
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-EDUCATION-LEVEL-002",
                "Education levels retrieved.",
                service.findAll(
                        tenantId
                )
        );

    }


    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('school.academic.curriculum.read')")
    public ApiResponse<EducationLevel> get(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-EDUCATION-LEVEL-003",
                "Education level retrieved.",
                service.findByCode(
                        tenantId,
                        code
                )
        );

    }


    @PatchMapping("/{code}/activate")
    @PreAuthorize("hasAuthority('school.academic.curriculum.manage')")
    public ApiResponse<EducationLevel> activate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        EducationLevel level =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-EDUCATION-LEVEL-004",
                "Education level activated.",
                service.activate(level)
        );

    }


    @PatchMapping("/{code}/deactivate")
    @PreAuthorize("hasAuthority('school.academic.curriculum.manage')")
    public ApiResponse<EducationLevel> deactivate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        EducationLevel level =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-EDUCATION-LEVEL-005",
                "Education level deactivated.",
                service.deactivate(level)
        );

    }

}
