package africa.growtogether.platform.ewf;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enterprise/workforce/members")
public class WorkforceMemberController {

    private final WorkforceMemberService service;
    private final ApiResponses responses;

    public WorkforceMemberController(
            WorkforceMemberService service,
            ApiResponses responses
    ) {
        this.service = service;
        this.responses = responses;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('enterprise.workforce.member.create')")
    public ApiResponse<WorkforceMember> create(
            @RequestParam UUID tenantId,
            @RequestParam String workforceNumber,
            @RequestParam(required = false) String employeeNumber,
            @RequestParam String firstName,
            @RequestParam(required = false) String middleName,
            @RequestParam String lastName,
            @RequestParam(required = false) String preferredName,
            @RequestParam(required = false) LocalDate dateOfBirth,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String nationalityCode,
            @RequestParam(required = false) String nationalIdNumber,
            @RequestParam(required = false) String passportNumber,
            @RequestParam(required = false) String primaryPhoneNumber,
            @RequestParam(required = false) String alternativePhoneNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String physicalAddress,
            @RequestParam(required = false) String postalAddress,
            @RequestParam(required = false) UUID eiamUserId,
            @RequestParam(required = false) UUID edsPersonnelFileId,
            @RequestParam(required = false) String workforceCategory
    ) {

        return responses.success(
                "GT-EWF-WORKFORCE-MEMBER-001",
                "Workforce member created.",
                service.create(
                        tenantId,
                        workforceNumber,
                        employeeNumber,
                        firstName,
                        middleName,
                        lastName,
                        preferredName,
                        dateOfBirth,
                        gender,
                        nationalityCode,
                        nationalIdNumber,
                        passportNumber,
                        primaryPhoneNumber,
                        alternativePhoneNumber,
                        email,
                        physicalAddress,
                        postalAddress,
                        eiamUserId,
                        edsPersonnelFileId,
                        workforceCategory
                )
        );
    }

    @GetMapping("/{workforceNumber}")
    @PreAuthorize("hasAuthority('enterprise.workforce.member.read')")
    public ApiResponse<WorkforceMember> getByWorkforceNumber(
            @PathVariable String workforceNumber,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-EWF-WORKFORCE-MEMBER-002",
                "Workforce member retrieved.",
                service.findByWorkforceNumber(
                        tenantId,
                        workforceNumber
                )
        );
    }

    @GetMapping("/employee/{employeeNumber}")
    @PreAuthorize("hasAuthority('enterprise.workforce.member.read')")
    public ApiResponse<WorkforceMember> getByEmployeeNumber(
            @PathVariable String employeeNumber,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-EWF-WORKFORCE-MEMBER-003",
                "Workforce member retrieved by employee number.",
                service.findByEmployeeNumber(
                        tenantId,
                        employeeNumber
                )
        );
    }

    @GetMapping("/search/email")
    @PreAuthorize("hasAuthority('enterprise.workforce.member.read')")
    public ApiResponse<List<WorkforceMember>> findByEmail(
            @RequestParam UUID tenantId,
            @RequestParam String email
    ) {

        return responses.success(
                "GT-EWF-WORKFORCE-MEMBER-004",
                "Workforce members retrieved by email.",
                service.findByEmail(
                        tenantId,
                        email
                )
        );
    }

    @GetMapping("/category/{workforceCategory}")
    @PreAuthorize("hasAuthority('enterprise.workforce.member.read')")
    public ApiResponse<List<WorkforceMember>> findByCategory(
            @PathVariable String workforceCategory,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-EWF-WORKFORCE-MEMBER-005",
                "Workforce members retrieved by category.",
                service.findByCategory(
                        tenantId,
                        workforceCategory
                )
        );
    }

    @GetMapping("/status/{workforceStatus}")
    @PreAuthorize("hasAuthority('enterprise.workforce.member.read')")
    public ApiResponse<List<WorkforceMember>> findByStatus(
            @PathVariable String workforceStatus,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-EWF-WORKFORCE-MEMBER-006",
                "Workforce members retrieved by workforce status.",
                service.findByStatus(
                        tenantId,
                        workforceStatus
                )
        );
    }
}
