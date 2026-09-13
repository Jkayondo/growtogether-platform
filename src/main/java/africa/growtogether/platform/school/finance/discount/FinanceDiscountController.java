package africa.growtogether.platform.school.finance.discount;

import static africa.growtogether.platform.school.finance.discount.FinanceDiscountDtos.*;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/finance/discount-schemes")
public class FinanceDiscountController {

    private final FinanceDiscountService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;

    public FinanceDiscountController(
            FinanceDiscountService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ApiResponse<FeeDiscountSchemeView> createFeeDiscountScheme(
            @RequestParam UUID tenantId,
            @RequestBody CreateFeeDiscountSchemeRequest request
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-026",
                "Fee discount scheme created.",
                service.createFeeDiscountScheme(
                        tenantId,
                        request,
                        identity.requireUserId().toString()
                )
        );
    }

    @GetMapping("/{discountSchemeId}")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<FeeDiscountSchemeView> getFeeDiscountScheme(
            @PathVariable UUID discountSchemeId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-027",
                "Fee discount scheme retrieved.",
                service.getFeeDiscountScheme(
                        tenantId,
                        discountSchemeId
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<List<FeeDiscountSchemeView>> listFeeDiscountSchemes(
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-028",
                "Fee discount schemes retrieved.",
                service.listFeeDiscountSchemes(
                        tenantId
                )
        );
    }
}
