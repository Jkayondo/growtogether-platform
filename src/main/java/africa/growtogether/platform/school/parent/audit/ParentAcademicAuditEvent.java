package africa.growtogether.platform.school.parent.audit;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
        name = "parent_academic_audit_events",
        indexes = {
                @Index(
                        name = "ix_parent_academic_audit_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class ParentAcademicAuditEvent
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
            length = 40
    )
    private ParentAcademicAuditEventType eventType;


    @Column(
            name = "event_time",
            nullable = false
    )
    private Instant eventTime;


    protected ParentAcademicAuditEvent() {
    }


    public ParentAcademicAuditEvent(
            UUID tenantId,
            UUID parentId,
            UUID learnerId,
            ParentAcademicAuditEventType eventType
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


    public ParentAcademicAuditEventType getEventType() {
        return eventType;
    }


    public Instant getEventTime() {
        return eventTime;
    }
}
