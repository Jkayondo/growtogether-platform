package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/curriculum")
public class CurriculumController {


    private final CurriculumService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;


    public CurriculumController(
            CurriculumService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.create')")
    public ApiResponse<Curriculum> create(
            @RequestParam UUID tenantId,
            @RequestParam String curriculumCode,
            @RequestParam String curriculumName,
            @RequestParam String curriculumType
    ) {

        identity.requireTenant(tenantId);

        Curriculum curriculum =
                service.create(
                        tenantId,
                        curriculumCode,
                        curriculumName,
                        curriculumType
                );


        return responses.success(
                "GT-SCHOOL-CURRICULUM-001",
                "Curriculum created.",
                curriculum
        );
    }


    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.curriculum.read')")
    public ApiResponse<List<Curriculum>> list(
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CURRICULUM-005",
                "Curricula retrieved.",
                service.findAllCurricula(
                        tenantId
                )
        );
    }


    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('school.academic.curriculum.read')")
    public ApiResponse<Curriculum> get(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CURRICULUM-002",
                "Curriculum retrieved.",
                service.findByCode(
                        tenantId,
                        code
                )
        );
    }


    @GetMapping("/active")
    @PreAuthorize("hasAuthority('school.academic.curriculum.read')")
    public ApiResponse<List<Curriculum>> active(
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CURRICULUM-003",
                "Active curricula retrieved.",
                service.findActiveCurricula(
                        tenantId
                )
        );
    }


    @PatchMapping("/{code}/activate")
    @PreAuthorize("hasAuthority('school.academic.curriculum.manage')")
    public ApiResponse<Curriculum> activate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        Curriculum curriculum =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-CURRICULUM-004",
                "Curriculum activated.",
                service.activate(
                        tenantId,
                        curriculum
                )
        );
    }

}
