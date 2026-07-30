package africa.growtogether.platform.school.parent.engagement;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
        name = "parent_engagement_events"
)
public class ParentEngagementEvent
        extends AuditedTenantEntity {


    @Column(
            name = "parent_id",
            nullable = false
    )
    private UUID parentId;


    @Column(
            name = "learner_id"
    )
    private UUID learnerId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 50
    )
    private ParentEngagementEventType eventType;


    @Column(
            name = "event_time",
            nullable = false
    )
    private Instant eventTime;


    protected ParentEngagementEvent() {
    }


    public ParentEngagementEvent(
            UUID tenantId,
            UUID parentId,
            UUID learnerId,
            ParentEngagementEventType eventType
    ) {

        setTenantId(tenantId);

        this.parentId = parentId;
        this.learnerId = learnerId;
        this.eventType = eventType;
        this.eventTime = Instant.now();
    }


    public UUID getParentId() {
        return parentId;
    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public ParentEngagementEventType getEventType() {
        return eventType;
    }


    public Instant getEventTime() {
        return eventTime;
    }
}
