package africa.growtogether.platform.school.finance.foundation;

import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/finance")
public class FinanceFoundationController {

    private final FinanceFoundationService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;


    public FinanceFoundationController(
            FinanceFoundationService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }


    @PostMapping("/fee-categories")
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ApiResponse<FeeCategoryView> createFeeCategory(
            @RequestParam UUID tenantId,
            @RequestBody CreateFeeCategoryRequest request
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-001",
                "Fee category created.",
                service.createFeeCategory(
                        tenantId,
                        request,
                        identity.requireUserId().toString()
                )
        );
    }


    @GetMapping("/fee-categories")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<List<FeeCategoryView>> listFeeCategories(
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-002",
                "Fee categories retrieved.",
                service.listFeeCategories(
                        tenantId
                )
        );
    }


    @PostMapping("/fee-items")
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ApiResponse<FeeItemView> createFeeItem(
            @RequestParam UUID tenantId,
            @RequestBody CreateFeeItemRequest request
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-003",
                "Fee item created.",
                service.createFeeItem(
                        tenantId,
                        request,
                        identity.requireUserId().toString()
                )
        );
    }


    @GetMapping("/fee-items")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<List<FeeItemView>> listFeeItems(
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-004",
                "Fee items retrieved.",
                service.listFeeItems(
                        tenantId
                )
        );
    }


    @PostMapping("/fee-structures")
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ApiResponse<FeeStructureView> createFeeStructure(
            @RequestParam UUID tenantId,
            @RequestBody CreateFeeStructureRequest request
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-005",
                "Fee structure created.",
                service.createFeeStructure(
                        tenantId,
                        request,
                        identity.requireUserId().toString()
                )
        );
    }


    @GetMapping("/fee-structures")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<List<FeeStructureView>> listFeeStructures(
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-006",
                "Fee structures retrieved.",
                service.listFeeStructures(
                        tenantId
                )
        );
    }


    @PostMapping("/fee-structures/{feeStructureId}/items")
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ApiResponse<FeeStructureItemView> addFeeStructureItem(
            @PathVariable UUID feeStructureId,
            @RequestParam UUID tenantId,
            @RequestBody AddFeeStructureItemRequest request
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-007",
                "Fee structure item added.",
                service.addFeeStructureItem(
                        tenantId,
                        feeStructureId,
                        request,
                        identity.requireUserId().toString()
                )
        );
    }


    @GetMapping("/fee-structures/{feeStructureId}/items")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<List<FeeStructureItemView>> listFeeStructureItems(
            @PathVariable UUID feeStructureId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-008",
                "Fee structure items retrieved.",
                service.listFeeStructureItems(
                        tenantId,
                        feeStructureId
                )
        );
    }


    @PatchMapping("/fee-structures/{feeStructureId}/approve")
    @PreAuthorize("hasAuthority('school.finance.approve')")
    public ApiResponse<FeeStructureView> approveFeeStructure(
            @PathVariable UUID feeStructureId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        UUID actorId =
                identity.requireUserId();


        return responses.success(
                "GT-SCHOOL-FIN-009",
                "Fee structure approved.",
                service.approveFeeStructure(
                        tenantId,
                        feeStructureId,
                        actorId,
                        actorId.toString()
                )
        );
    }


    @PatchMapping("/fee-structures/{feeStructureId}/activate")
    @PreAuthorize("hasAuthority('school.finance.approve')")
    public ApiResponse<FeeStructureView> activateFeeStructure(
            @PathVariable UUID feeStructureId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-010",
                "Fee structure activated.",
                service.activateFeeStructure(
                        tenantId,
                        feeStructureId,
                        identity.requireUserId().toString()
                )
        );
    }


    @PostMapping("/student-accounts")
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ApiResponse<StudentFinancialAccountView> openStudentAccount(
            @RequestParam UUID tenantId,
            @RequestBody OpenStudentFinancialAccountRequest request
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-011",
                "Student financial account opened.",
                service.openStudentAccount(
                        tenantId,
                        request,
                        identity.requireUserId().toString()
                )
        );
    }


    @GetMapping("/student-accounts/student/{studentId}")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<Optional<StudentFinancialAccountView>> findStudentAccount(
            @PathVariable UUID studentId,
            @RequestParam UUID tenantId,
            @RequestParam String currencyCode
    ) {

        identity.requireTenant(
                tenantId
        );


        return responses.success(
                "GT-SCHOOL-FIN-012",
                "Student financial account retrieved.",
                service.findStudentAccount(
                        tenantId,
                        studentId,
                        currencyCode
                )
        );
    }
}
