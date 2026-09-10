package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/class-grades")
public class ClassGradeController {


    private final ClassGradeService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;


    public ClassGradeController(
            ClassGradeService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.class-grade.create')")
    public ApiResponse<ClassGrade> create(
            @RequestParam UUID tenantId,
            @RequestParam UUID educationLevelId,
            @RequestParam String classCode,
            @RequestParam String className,
            @RequestParam Integer sequenceNumber,
            @RequestParam(required = false) Integer capacity
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CLASS-GRADE-001",
                "Class grade created.",
                service.create(
                        tenantId,
                        educationLevelId,
                        classCode,
                        className,
                        sequenceNumber,
                        capacity
                )
        );

    }



    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.class-grade.read')")
    public ApiResponse<List<ClassGrade>> findByEducationLevel(
            @RequestParam UUID tenantId,
            @RequestParam UUID educationLevelId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CLASS-GRADE-002",
                "Class grades retrieved.",
                service.findByEducationLevel(
                        tenantId,
                        educationLevelId
                )
        );

    }



    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('school.academic.class-grade.read')")
    public ApiResponse<ClassGrade> findByCode(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CLASS-GRADE-003",
                "Class grade retrieved.",
                service.findByCode(
                        tenantId,
                        code
                )
        );

    }



    @PatchMapping("/{code}/activate")
    @PreAuthorize("hasAuthority('school.academic.class-grade.manage')")
    public ApiResponse<ClassGrade> activate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        ClassGrade classGrade =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-CLASS-GRADE-004",
                "Class grade activated.",
                service.activate(classGrade)
        );

    }



    @PatchMapping("/{code}/deactivate")
    @PreAuthorize("hasAuthority('school.academic.class-grade.manage')")
    public ApiResponse<ClassGrade> deactivate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        ClassGrade classGrade =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-CLASS-GRADE-005",
                "Class grade deactivated.",
                service.deactivate(classGrade)
        );

    }

}
