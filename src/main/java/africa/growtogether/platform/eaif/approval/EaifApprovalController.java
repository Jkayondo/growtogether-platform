package africa.growtogether.platform.eaif.approval;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eaif/approvals")
public class EaifApprovalController {

    private final EaifApprovalService service;
    private final EnterpriseIdentityContext identity;

    public EaifApprovalController(
            EaifApprovalService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAuthority('ai.request.approval')")
    public EaifApprovalRecord approve(
            @PathVariable UUID requestId,
            @RequestParam String reason
    ) {
        UUID tenantId = identity.requireTenantId();
        UUID approvedBy = identity.requireUserId();

        return service.approveAndRelease(
                tenantId,
                requestId,
                approvedBy,
                reason
        );
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAuthority('ai.request.approval')")
    public EaifApprovalRecord reject(
            @PathVariable UUID requestId,
            @RequestParam String reason
    ) {
        UUID tenantId = identity.requireTenantId();
        UUID rejectedBy = identity.requireUserId();

        return service.rejectAndBlock(
                tenantId,
                requestId,
                rejectedBy,
                reason
        );
    }
}
