package africa.growtogether.platform.school.parent.governance.security.api;


import africa.growtogether.platform.school.parent.governance.security.event.ParentEngagementGovernanceSecurityEvent;

import org.springframework.stereotype.Component;


@Component
public class ParentEngagementGovernanceSecurityEventMapper {


    public ParentEngagementGovernanceSecurityEventResponse map(
            ParentEngagementGovernanceSecurityEvent event
    ) {

        return new ParentEngagementGovernanceSecurityEventResponse(
                event.getUserId(),
                event.getEventType()
        );
    }
}
