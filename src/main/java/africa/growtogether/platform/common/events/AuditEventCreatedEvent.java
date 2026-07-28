package africa.growtogether.platform.common.events;

import africa.growtogether.platform.eiam.audit.AuditEventCategory;
import africa.growtogether.platform.eiam.audit.AuditOutcome;
import africa.growtogether.platform.eiam.audit.SecuritySeverity;

import java.time.Instant;
import java.util.UUID;

public record AuditEventCreatedEvent(
        UUID eventId,
        UUID auditEventId,
        UUID tenantId,
        String eventType,
        AuditEventCategory category,
        AuditOutcome outcome,
        SecuritySeverity severity,
        String resourceType,
        String resourceId,
        Instant occurredAt
) implements DomainEvent {

    @Override
    public UUID eventId() {
        return eventId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
