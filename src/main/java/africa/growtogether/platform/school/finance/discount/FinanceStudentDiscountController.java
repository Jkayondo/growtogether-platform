package africa.growtogether.platform.school.finance.discount;

import static africa.growtogether.platform.school.finance.discount.FinanceStudentDiscountDtos.*;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/finance/student-discounts")
public class FinanceStudentDiscountController {

    private final FinanceStudentDiscountService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;

    public FinanceStudentDiscountController(
            FinanceStudentDiscountService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ApiResponse<StudentDiscountRequestView> createStudentDiscountRequest(
            @RequestParam UUID tenantId,
            @RequestBody CreateStudentDiscountRequest request
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-029",
                "Student discount request created.",
                service.createStudentDiscountRequest(
                        tenantId,
                        request,
                        identity.requireUserId()
                )
        );
    }

    @GetMapping("/{studentDiscountId}")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<StudentDiscountRequestView> getStudentDiscountRequest(
            @PathVariable UUID studentDiscountId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-030",
                "Student discount request retrieved.",
                service.getStudentDiscountRequest(
                        tenantId,
                        studentDiscountId
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<List<StudentDiscountRequestView>> listStudentDiscountRequests(
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-031",
                "Student discount requests retrieved.",
                service.listStudentDiscountRequests(
                        tenantId
                )
        );
    }
}
