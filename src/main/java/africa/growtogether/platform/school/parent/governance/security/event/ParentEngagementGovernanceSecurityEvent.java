package africa.growtogether.platform.school.parent.governance.security.event;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "parent_engagement_governance_security_events")
public class ParentEngagementGovernanceSecurityEvent
        extends AuditedTenantEntity {


    @Column(name = "user_id",
            nullable = false)
    private UUID userId;


    @Column(name = "tenant_id",
            nullable = false,
            insertable = false,
            updatable = false)
    private UUID tenantReference;


    @Enumerated(EnumType.STRING)
    @Column(name = "event_type",
            nullable = false,
            length = 50)
    private ParentEngagementGovernanceSecurityEventType eventType;


    protected ParentEngagementGovernanceSecurityEvent() {
    }


    public ParentEngagementGovernanceSecurityEvent(
            UUID tenantId,
            UUID userId,
            ParentEngagementGovernanceSecurityEventType eventType
    ) {

        setTenantId(tenantId);

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
