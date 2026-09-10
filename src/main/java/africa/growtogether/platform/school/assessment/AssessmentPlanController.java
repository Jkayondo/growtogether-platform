package africa.growtogether.platform.school.assessment;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/assessment-plans")
public class AssessmentPlanController {


    private final AssessmentPlanService service;
    private final EnterpriseIdentityContext identity;


    public AssessmentPlanController(
            AssessmentPlanService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.create')"
    )
    public AssessmentPlan create(
            @Valid @RequestBody CreateAssessmentPlanCommand command
    ) {

        UUID tenantId =
                identity.requireTenantId();

        return service.create(
                tenantId,
                command
        );
    }

    @org.springframework.web.bind.annotation.GetMapping(
            "/{assessmentPlanId}"
    )
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public AssessmentPlan get(
            @org.springframework.web.bind.annotation.PathVariable
            UUID assessmentPlanId
    ) {

        return service.get(
                identity.requireTenantId(),
                assessmentPlanId
        );
    }



    @org.springframework.web.bind.annotation.GetMapping(
            "/academic-year/{academicYearId}"
    )
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public java.util.List<AssessmentPlan> findByAcademicYear(
            @org.springframework.web.bind.annotation.PathVariable
            UUID academicYearId
    ) {

        return service.findByAcademicYear(
                identity.requireTenantId(),
                academicYearId
        );
    }



    @org.springframework.web.bind.annotation.GetMapping(
            "/campus/{campusId}"
    )
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public java.util.List<AssessmentPlan> findByCampus(
            @org.springframework.web.bind.annotation.PathVariable
            UUID campusId
    ) {

        return service.findByCampus(
                identity.requireTenantId(),
                campusId
        );
    }



    @org.springframework.web.bind.annotation.GetMapping(
            "/grade/{classGradeId}"
    )
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public java.util.List<AssessmentPlan> findByClassGrade(
            @org.springframework.web.bind.annotation.PathVariable
            UUID classGradeId
    ) {

        return service.findByClassGrade(
                identity.requireTenantId(),
                classGradeId
        );
    }

}
