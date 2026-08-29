package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/subject-offerings")
public class SubjectOfferingController {


    private final SubjectOfferingService service;
    private final ApiResponses responses;


    public SubjectOfferingController(
            SubjectOfferingService service,
            ApiResponses responses
    ) {
        this.service = service;
        this.responses = responses;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.subject.create')")
    public ApiResponse<SubjectOffering> create(
            @RequestParam UUID tenantId,
            @RequestParam String subjectOfferingCode,
            @RequestParam UUID classOfferingId,
            @RequestParam(required = false) UUID academicTermId,
            @RequestParam(required = false) UUID streamId,
            @RequestParam UUID subjectId,
            @RequestParam(required = false) UUID academicDepartmentId,
            @RequestParam(required = false) UUID gradingSchemeId,
            @RequestParam(required = false) Integer weeklyPeriods,
            @RequestParam(required = false) BigDecimal creditValue,
            @RequestParam(required = false) Integer minimumEnrollment,
            @RequestParam(required = false) Integer maximumEnrollment
    ) {


        return responses.success(
                "GT-SCHOOL-SUBJECT-OFFERING-001",
                "Subject offering created.",
                service.create(
                        tenantId,
                        subjectOfferingCode,
                        classOfferingId,
                        academicTermId,
                        streamId,
                        subjectId,
                        academicDepartmentId,
                        gradingSchemeId,
                        weeklyPeriods,
                        creditValue,
                        minimumEnrollment,
                        maximumEnrollment
                )
        );
    }


    @GetMapping("/class/{classOfferingId}")
    @PreAuthorize("hasAuthority('school.academic.subject.read')")
    public ApiResponse<List<SubjectOffering>> findByClassOffering(
            @PathVariable UUID classOfferingId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-SUBJECT-OFFERING-002",
                "Subject offerings retrieved by class.",
                service.findByClassOffering(
                        tenantId,
                        classOfferingId
                )
        );
    }


    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAuthority('school.academic.subject.read')")
    public ApiResponse<List<SubjectOffering>> findBySubject(
            @PathVariable UUID subjectId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-SUBJECT-OFFERING-003",
                "Subject offerings retrieved by subject.",
                service.findBySubject(
                        tenantId,
                        subjectId
                )
        );
    }


    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('school.academic.subject.read')")
    public ApiResponse<SubjectOffering> get(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-SUBJECT-OFFERING-004",
                "Subject offering retrieved.",
                service.findByCode(
                        tenantId,
                        code
                )
        );
    }


    @PatchMapping("/{code}/activate")
    @PreAuthorize("hasAuthority('school.academic.subject.manage')")
    public ApiResponse<SubjectOffering> activate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        SubjectOffering offering =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-SUBJECT-OFFERING-005",
                "Subject offering activated.",
                service.activate(
                        offering
                )
        );
    }


    @PatchMapping("/{code}/deactivate")
    @PreAuthorize("hasAuthority('school.academic.subject.manage')")
    public ApiResponse<SubjectOffering> deactivate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        SubjectOffering offering =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-SUBJECT-OFFERING-006",
                "Subject offering deactivated.",
                service.deactivate(
                        offering
                )
        );
    }

}
