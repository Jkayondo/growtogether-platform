package africa.growtogether.platform.school.parent.governance.security.api;


import africa.growtogether.platform.school.parent.governance.security.event.ParentEngagementGovernanceSecurityEventType;


import java.util.UUID;


public class ParentEngagementGovernanceSecurityEventResponse {


    private final UUID userId;

    private final ParentEngagementGovernanceSecurityEventType eventType;


    public ParentEngagementGovernanceSecurityEventResponse(
            UUID userId,
            ParentEngagementGovernanceSecurityEventType eventType
    ) {

        this.userId = userId;
        this.eventType = eventType;
    }


    public UUID getUserId() {

        return userId;
    }


    public ParentEngagementGovernanceSecurityEventType getEventType() {

        return eventType;
    }
}
