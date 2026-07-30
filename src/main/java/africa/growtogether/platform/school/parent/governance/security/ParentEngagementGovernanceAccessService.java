package africa.growtogether.platform.school.parent.governance.security;


import africa.growtogether.platform.school.parent.governance.security.event.ParentEngagementGovernanceSecurityEvent;
import africa.growtogether.platform.school.parent.governance.security.event.ParentEngagementGovernanceSecurityEventService;
import africa.growtogether.platform.school.parent.governance.security.event.ParentEngagementGovernanceSecurityEventType;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentEngagementGovernanceAccessService {


    private final ParentEngagementGovernanceSecurityEventService eventService;


    public ParentEngagementGovernanceAccessService(
            ParentEngagementGovernanceSecurityEventService eventService
    ) {

        this.eventService = eventService;
    }


    public ParentEngagementGovernanceAccessDecision validateAccess(
            UUID userId,
            UUID tenantId,
            ParentEngagementGovernancePermission permission
    ) {

        /*
         * Foundation layer.
         *
         * Future integration:
         * - GT IAM
         * - RBAC
         * - ABAC
         * - ReBAC
         * - Tenant security policies
         */


        ParentEngagementGovernanceAccessDecision decision =
                ParentEngagementGovernanceAccessDecision.ALLOWED;


        if (decision ==
                ParentEngagementGovernanceAccessDecision.ALLOWED) {


            eventService.record(
                    new ParentEngagementGovernanceSecurityEvent(
                            tenantId,
                            userId,
                            ParentEngagementGovernanceSecurityEventType
                                    .DASHBOARD_ACCESS_GRANTED
                    )
            );


        } else {


            eventService.record(
                    new ParentEngagementGovernanceSecurityEvent(
                            tenantId,
                            userId,
                            ParentEngagementGovernanceSecurityEventType
                                    .DASHBOARD_ACCESS_DENIED
                    )
            );
        }


        return decision;
    }
}
