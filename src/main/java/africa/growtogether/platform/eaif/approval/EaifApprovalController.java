package africa.growtogether.platform.eaif.approval;


import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/eaif/approvals")
public class EaifApprovalController {


    private final EaifApprovalService service;


    public EaifApprovalController(
            EaifApprovalService service
    ) {

        this.service = service;

    }


    @PostMapping("/{requestId}/approve")
    public EaifApprovalRecord approve(

            @PathVariable UUID requestId,

            @RequestParam UUID tenantId,

            @RequestParam UUID approvedBy,

            @RequestParam String reason

    ) {

        return service.approveAndRelease(
                tenantId,
                requestId,
                approvedBy,
                reason
        );

    }


    @PostMapping("/{requestId}/reject")
    public EaifApprovalRecord reject(

            @PathVariable UUID requestId,

            @RequestParam UUID tenantId,

            @RequestParam UUID rejectedBy,

            @RequestParam String reason

    ) {

        return service.rejectAndBlock(
                tenantId,
                requestId,
                rejectedBy,
                reason
        );

    }

}