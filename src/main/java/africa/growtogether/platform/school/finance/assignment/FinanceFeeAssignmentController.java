package africa.growtogether.platform.school.finance.assignment;

import static africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.*;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/finance/fee-assignments")
public class FinanceFeeAssignmentController {

    private final FinanceFeeAssignmentService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;


    public FinanceFeeAssignmentController(
            FinanceFeeAssignmentService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ApiResponse<StudentFeeAssignmentView> assignStudentFee(
            @RequestParam UUID tenantId,
            @RequestBody AssignStudentFeeRequest request
    ) {

        identity.requireTenant(
                tenantId
        );


        UUID actorId =
                identity.requireUserId();


        return responses.success(
                "GT-SCHOOL-FIN-013",
                "Student fee assignment created.",
                service.assignStudentFee(
                        tenantId,
                        request,
                        actorId,
                        actorId.toString()
                )
        );
    }


    @GetMapping("/{assignmentId}")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<Optional<StudentFeeAssignmentView>> findStudentFeeAssignment(
            @PathVariable UUID assignmentId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-014",
                "Student fee assignment retrieved.",
                service.findStudentFeeAssignment(
                        tenantId,
                        assignmentId
                )
        );
    }


    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<List<StudentFeeAssignmentView>> listStudentFeeAssignments(
            @PathVariable UUID studentId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-015",
                "Student fee assignments retrieved.",
                service.listStudentFeeAssignments(
                        tenantId,
                        studentId
                )
        );
    }

@org.springframework.web.bind.annotation.PatchMapping(
            "/{assignmentId}/suspend"
    )
    @org.springframework.security.access.prepost.PreAuthorize(
            "hasAuthority('school.finance.approve')"
    )
    public africa.growtogether.platform.common.api.ApiResponse<StudentFeeAssignmentView>
    suspendStudentFeeAssignment(
            @org.springframework.web.bind.annotation.PathVariable
            java.util.UUID assignmentId,
            @org.springframework.web.bind.annotation.RequestParam
            java.util.UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-016",
                "Learner fee assignment suspended.",
                service.suspendStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        identity.requireUserId()
                                .toString()
                )
        );
    }

    @org.springframework.web.bind.annotation.PatchMapping(
            "/{assignmentId}/activate"
    )
    @org.springframework.security.access.prepost.PreAuthorize(
            "hasAuthority('school.finance.approve')"
    )
    public africa.growtogether.platform.common.api.ApiResponse<StudentFeeAssignmentView>
    activateStudentFeeAssignment(
            @org.springframework.web.bind.annotation.PathVariable
            java.util.UUID assignmentId,
            @org.springframework.web.bind.annotation.RequestParam
            java.util.UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-017",
                "Learner fee assignment activated.",
                service.activateStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        identity.requireUserId()
                                .toString()
                )
        );
    }

    @org.springframework.web.bind.annotation.PatchMapping(
            "/{assignmentId}/complete"
    )
    @org.springframework.security.access.prepost.PreAuthorize(
            "hasAuthority('school.finance.approve')"
    )
    public africa.growtogether.platform.common.api.ApiResponse<StudentFeeAssignmentView>
    completeStudentFeeAssignment(
            @org.springframework.web.bind.annotation.PathVariable
            java.util.UUID assignmentId,
            @org.springframework.web.bind.annotation.RequestParam
            java.util.UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-018",
                "Learner fee assignment completed.",
                service.completeStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        identity.requireUserId()
                                .toString()
                )
        );
    }

    @org.springframework.web.bind.annotation.PatchMapping(
            "/{assignmentId}/cancel"
    )
    @org.springframework.security.access.prepost.PreAuthorize(
            "hasAuthority('school.finance.approve')"
    )
    public africa.growtogether.platform.common.api.ApiResponse<StudentFeeAssignmentView>
    cancelStudentFeeAssignment(
            @org.springframework.web.bind.annotation.PathVariable
            java.util.UUID assignmentId,
            @org.springframework.web.bind.annotation.RequestParam
            java.util.UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-019",
                "Learner fee assignment cancelled.",
                service.cancelStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        identity.requireUserId()
                                .toString()
                )
        );
    }

    @org.springframework.web.bind.annotation.PatchMapping(
            "/{assignmentId}/archive"
    )
    @org.springframework.security.access.prepost.PreAuthorize(
            "hasAuthority('school.finance.approve')"
    )
    public africa.growtogether.platform.common.api.ApiResponse<StudentFeeAssignmentView>
    archiveStudentFeeAssignment(
            @org.springframework.web.bind.annotation.PathVariable
            java.util.UUID assignmentId,
            @org.springframework.web.bind.annotation.RequestParam
            java.util.UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-020",
                "Learner fee assignment archived.",
                service.archiveStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        identity.requireUserId()
                                .toString()
                )
        );
    }
}
