package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/class-offerings")
public class ClassOfferingController {


    private final ClassOfferingService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;


    public ClassOfferingController(
            ClassOfferingService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.class-offering.create')")
    public ApiResponse<ClassOffering> create(
            @RequestParam UUID tenantId,
            @RequestParam String offeringCode,
            @RequestParam UUID academicYearId,
            @RequestParam UUID campusId,
            @RequestParam(required = false) UUID academicProgrammeId,
            @RequestParam(required = false) UUID studyTrackId,
            @RequestParam(required = false) UUID curriculumVersionId,
            @RequestParam UUID classGradeId,
            @RequestParam(required = false) Integer plannedCapacity,
            @RequestParam(required = false) Integer minimumEnrollment,
            @RequestParam(required = false) Integer maximumEnrollment,
            @RequestParam(required = false) LocalDate enrollmentOpenDate,
            @RequestParam(required = false) LocalDate enrollmentCloseDate
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CLASS-OFFERING-001",
                "Class offering created.",
                service.create(
                        tenantId,
                        offeringCode,
                        academicYearId,
                        campusId,
                        academicProgrammeId,
                        studyTrackId,
                        curriculumVersionId,
                        classGradeId,
                        plannedCapacity,
                        minimumEnrollment,
                        maximumEnrollment,
                        enrollmentOpenDate,
                        enrollmentCloseDate
                )
        );
    }


    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('school.academic.class-offering.read')")
    public ApiResponse<ClassOffering> get(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CLASS-OFFERING-002",
                "Class offering retrieved.",
                service.findByCode(
                        tenantId,
                        code
                )
        );
    }


    @GetMapping("/year/{academicYearId}")
    @PreAuthorize("hasAuthority('school.academic.class-offering.read')")
    public ApiResponse<List<ClassOffering>> findByAcademicYear(
            @PathVariable UUID academicYearId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CLASS-OFFERING-003",
                "Class offerings retrieved by academic year.",
                service.findByAcademicYear(
                        tenantId,
                        academicYearId
                )
        );
    }


    @GetMapping("/campus/{campusId}")
    @PreAuthorize("hasAuthority('school.academic.class-offering.read')")
    public ApiResponse<List<ClassOffering>> findByCampus(
            @PathVariable UUID campusId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CLASS-OFFERING-004",
                "Class offerings retrieved by campus.",
                service.findByCampus(
                        tenantId,
                        campusId
                )
        );
    }


    @GetMapping("/grade/{classGradeId}")
    @PreAuthorize("hasAuthority('school.academic.class-offering.read')")
    public ApiResponse<List<ClassOffering>> findByClassGrade(
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-CLASS-OFFERING-005",
                "Class offerings retrieved by class grade.",
                service.findByClassGrade(
                        tenantId,
                        classGradeId
                )
        );
    }


    @PatchMapping("/{code}/activate")
    @PreAuthorize("hasAuthority('school.academic.class-offering.manage')")
    public ApiResponse<ClassOffering> activate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        ClassOffering offering =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-CLASS-OFFERING-006",
                "Class offering activated.",
                service.activate(
                        offering
                )
        );
    }


    @PatchMapping("/{code}/deactivate")
    @PreAuthorize("hasAuthority('school.academic.class-offering.manage')")
    public ApiResponse<ClassOffering> deactivate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        ClassOffering offering =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-CLASS-OFFERING-007",
                "Class offering deactivated.",
                service.deactivate(
                        offering
                )
        );
    }

}
