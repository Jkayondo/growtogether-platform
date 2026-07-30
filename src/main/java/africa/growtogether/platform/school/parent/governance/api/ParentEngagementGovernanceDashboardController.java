package africa.growtogether.platform.school.parent.governance.api;


import africa.growtogether.platform.school.parent.compliance.ParentEngagementComplianceSummaryService;
import africa.growtogether.platform.school.parent.governance.security.ParentEngagementGovernanceAccessDecision;
import africa.growtogether.platform.school.parent.governance.security.ParentEngagementGovernanceAccessService;
import africa.growtogether.platform.school.parent.governance.security.ParentEngagementGovernancePermission;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/school/parent-engagement/governance")
public class ParentEngagementGovernanceDashboardController {


    private final ParentEngagementComplianceSummaryService service;

    private final ParentEngagementGovernanceDashboardMapper mapper;

    private final ParentEngagementGovernanceAccessService accessService;


    public ParentEngagementGovernanceDashboardController(
            ParentEngagementComplianceSummaryService service,
            ParentEngagementGovernanceDashboardMapper mapper,
            ParentEngagementGovernanceAccessService accessService
    ) {

        this.service = service;
        this.mapper = mapper;
        this.accessService = accessService;
    }


    @GetMapping("/{tenantId}/dashboard")
    public ParentEngagementGovernanceDashboardResponse dashboard(
            @PathVariable UUID tenantId
    ) {


        var decision =
                accessService.validateAccess(
                        UUID.randomUUID(),
                        tenantId,
                        ParentEngagementGovernancePermission.VIEW_GOVERNANCE_DASHBOARD
                );


        if (decision ==
                ParentEngagementGovernanceAccessDecision.DENIED) {

            throw new SecurityException(
                    "Access denied"
            );
        }


        return mapper.map(
                service.generate(tenantId)
        );
    }
}
