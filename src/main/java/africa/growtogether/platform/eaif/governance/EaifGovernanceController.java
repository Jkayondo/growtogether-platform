package africa.growtogether.platform.eaif.governance;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/governance")
public class EaifGovernanceController {

    private final EaifGovernanceReportingService service;
    private final EnterpriseIdentityContext identity;

    public EaifGovernanceController(
            EaifGovernanceReportingService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }


    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ai.governance.read')")
    public EaifGovernanceDtos.Summary summary() {

        return service.summary(
                identity.tenantId()
        );
    }
}
