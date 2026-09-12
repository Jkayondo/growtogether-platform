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
}
