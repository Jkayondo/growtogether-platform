package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/academic/teaching-assignments")
public class TeachingAssignmentController {

    private final TeachingAssignmentService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;

    public TeachingAssignmentController(
            TeachingAssignmentService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.create')"
    )
    public ApiResponse<TeachingAssignment> create(
            @RequestParam UUID tenantId,
            @RequestParam String assignmentReference,
            @RequestParam UUID teacherProfileId,
            @RequestParam UUID academicYearId,
            @RequestParam(required = false) UUID academicTermId,
            @RequestParam UUID campusId,
            @RequestParam UUID classGradeId,
            @RequestParam(required = false) UUID streamId,
            @RequestParam UUID subjectId,
            @RequestParam(required = false) String assignmentType,
            @RequestParam int weeklyPeriods,
            @RequestParam(required = false) BigDecimal workloadPercentage,
            @RequestParam LocalDate effectiveFrom,
            @RequestParam(required = false) LocalDate effectiveTo,
            @RequestParam(required = false) String roomReference
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-001",
                "Teaching assignment created.",
                service.create(
                        tenantId,
                        assignmentReference,
                        teacherProfileId,
                        academicYearId,
                        academicTermId,
                        campusId,
                        classGradeId,
                        streamId,
                        subjectId,
                        assignmentType,
                        weeklyPeriods,
                        workloadPercentage,
                        effectiveFrom,
                        effectiveTo,
                        roomReference
                )
        );
    }

    @GetMapping("/{assignmentId}")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.read')"
    )
    public ApiResponse<TeachingAssignment> getById(
            @PathVariable UUID assignmentId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-002",
                "Teaching assignment retrieved.",
                service.findById(
                        tenantId,
                        assignmentId
                )
        );
    }

    @GetMapping("/reference/{assignmentReference}")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.read')"
    )
    public ApiResponse<TeachingAssignment> getByReference(
            @PathVariable String assignmentReference,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-003",
                "Teaching assignment retrieved by reference.",
                service.findByReference(
                        tenantId,
                        assignmentReference
                )
        );
    }

    @GetMapping("/teacher/{teacherProfileId}")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.read')"
    )
    public ApiResponse<List<TeachingAssignment>> findByTeacher(
            @PathVariable UUID teacherProfileId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-004",
                "Teaching assignments retrieved by teacher.",
                service.findByTeacher(
                        tenantId,
                        teacherProfileId
                )
        );
    }

    @GetMapping("/academic-year/{academicYearId}")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.read')"
    )
    public ApiResponse<List<TeachingAssignment>> findByAcademicYear(
            @PathVariable UUID academicYearId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-005",
                "Teaching assignments retrieved by academic year.",
                service.findByAcademicYear(
                        tenantId,
                        academicYearId
                )
        );
    }

    @GetMapping("/campus/{campusId}")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.read')"
    )
    public ApiResponse<List<TeachingAssignment>> findByCampus(
            @PathVariable UUID campusId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-006",
                "Teaching assignments retrieved by campus.",
                service.findByCampus(
                        tenantId,
                        campusId
                )
        );
    }

    @GetMapping("/class-grade/{classGradeId}")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.read')"
    )
    public ApiResponse<List<TeachingAssignment>> findByClassGrade(
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-007",
                "Teaching assignments retrieved by class grade.",
                service.findByClassGrade(
                        tenantId,
                        classGradeId
                )
        );
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.read')"
    )
    public ApiResponse<List<TeachingAssignment>> findBySubject(
            @PathVariable UUID subjectId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-008",
                "Teaching assignments retrieved by subject.",
                service.findBySubject(
                        tenantId,
                        subjectId
                )
        );
    }

    @GetMapping("/status/{assignmentStatus}")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.read')"
    )
    public ApiResponse<List<TeachingAssignment>> findByStatus(
            @PathVariable String assignmentStatus,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-009",
                "Teaching assignments retrieved by status.",
                service.findByStatus(
                        tenantId,
                        assignmentStatus
                )
        );
    }

    @PatchMapping("/{assignmentId}/request-approval")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.manage')"
    )
    public ApiResponse<TeachingAssignment> requestApproval(
            @PathVariable UUID assignmentId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-010",
                "Teaching assignment submitted for approval.",
                service.requestApproval(
                        tenantId,
                        assignmentId
                )
        );
    }

    @PatchMapping("/{assignmentId}/activate")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.manage')"
    )
    public ApiResponse<TeachingAssignment> activate(
            @PathVariable UUID assignmentId,
            @RequestParam UUID tenantId,
            @RequestParam UUID approvedBy
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-011",
                "Teaching assignment activated.",
                service.activate(
                        tenantId,
                        assignmentId,
                        approvedBy
                )
        );
    }

    @PatchMapping("/{assignmentId}/suspend")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.manage')"
    )
    public ApiResponse<TeachingAssignment> suspend(
            @PathVariable UUID assignmentId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-012",
                "Teaching assignment suspended.",
                service.suspend(
                        tenantId,
                        assignmentId
                )
        );
    }

    @PatchMapping("/{assignmentId}/complete")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.manage')"
    )
    public ApiResponse<TeachingAssignment> complete(
            @PathVariable UUID assignmentId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-013",
                "Teaching assignment completed.",
                service.complete(
                        tenantId,
                        assignmentId
                )
        );
    }

    @PatchMapping("/{assignmentId}/cancel")
    @PreAuthorize(
            "hasAuthority('school.academic.teaching-assignment.manage')"
    )
    public ApiResponse<TeachingAssignment> cancel(
            @PathVariable UUID assignmentId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-014",
                "Teaching assignment cancelled.",
                service.cancel(
                        tenantId,
                        assignmentId
                )
        );
    }
}
