
package africa.growtogether.platform.school.finance.allocation;

import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.AllocationResponse;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.CreateRequest;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.CorrectionResponse;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.ReallocateRequest;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.ReverseRequest;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school/finance/payments")
public class FinancePaymentAllocationController {

    private final FinancePaymentAllocationService service;
    private final EnterpriseIdentityContext identity;

    public FinancePaymentAllocationController(
            FinancePaymentAllocationService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }

    @PostMapping("/{paymentId}/allocations")
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public AllocationResponse create(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID paymentId,
            @Valid @RequestBody CreateRequest request
    ) {
        identity.requireTenant(tenantId);

        UUID actorId = UUID.fromString(
                identity.requireUserId().toString()
        );

        return service.create(
                tenantId,
                paymentId,
                request,
                actorId
        );
    }

    @PostMapping("/{paymentId}/allocations/{allocationId}/reverse")
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public CorrectionResponse reverse(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID paymentId,
            @PathVariable UUID allocationId,
            @Valid @RequestBody ReverseRequest request
    ) {
        identity.requireTenant(tenantId);

        UUID actorId = UUID.fromString(
                identity.requireUserId().toString()
        );

        return service.reverse(
                tenantId,
                paymentId,
                allocationId,
                request,
                actorId
        );
    }

    @PostMapping("/{paymentId}/allocations/{allocationId}/reallocate")
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public CorrectionResponse reallocate(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID paymentId,
            @PathVariable UUID allocationId,
            @Valid @RequestBody ReallocateRequest request
    ) {
        identity.requireTenant(tenantId);

        UUID actorId = UUID.fromString(
                identity.requireUserId().toString()
        );

        return service.reallocate(
                tenantId,
                paymentId,
                allocationId,
                request,
                actorId
        );
    }

    @GetMapping("/{paymentId}/allocations/{allocationId}/correction")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public CorrectionResponse getCorrection(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID paymentId,
            @PathVariable UUID allocationId
    ) {
        identity.requireTenant(tenantId);

        return service.getCorrection(
                tenantId,
                paymentId,
                allocationId
        );
    }

    @GetMapping("/{paymentId}/allocations/{allocationId}")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public AllocationResponse get(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID paymentId,
            @PathVariable UUID allocationId
    ) {
        identity.requireTenant(tenantId);

        return service.get(
                tenantId,
                paymentId,
                allocationId
        );
    }

    @GetMapping("/{paymentId}/allocations")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public List<AllocationResponse> list(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID paymentId
    ) {
        identity.requireTenant(tenantId);

        return service.list(
                tenantId,
                paymentId
        );
    }
}
