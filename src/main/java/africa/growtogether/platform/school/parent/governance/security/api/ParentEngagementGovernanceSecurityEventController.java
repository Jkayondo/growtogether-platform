package africa.growtogether.platform.school.parent.governance.security.api;


import africa.growtogether.platform.school.parent.governance.security.event.ParentEngagementGovernanceSecurityEventService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/school/parent-engagement/governance/security")
public class ParentEngagementGovernanceSecurityEventController {


    private final ParentEngagementGovernanceSecurityEventService service;

    private final ParentEngagementGovernanceSecurityEventMapper mapper;


    public ParentEngagementGovernanceSecurityEventController(
            ParentEngagementGovernanceSecurityEventService service,
            ParentEngagementGovernanceSecurityEventMapper mapper
    ) {

        this.service = service;
        this.mapper = mapper;
    }


    @GetMapping("/{tenantId}/events")
    public List<ParentEngagementGovernanceSecurityEventResponse> events(
            @PathVariable UUID tenantId
    ) {

        return service.findByTenant(tenantId)
                .stream()
                .map(mapper::map)
                .toList();
    }
}
