package africa.growtogether.platform.eiam.role.events;

import africa.growtogether.platform.common.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published after the authoritative EIAM role assignments for a user
 * have changed.
 *
 * Consumers must resolve any product-specific consequences from their
 * own authoritative rules. EIAM does not depend on GT School or
 * GT Connect.
 */
public record UserRolesChangedEvent(

        UUID eventId,

        UUID tenantId,

        UUID userId,

        Instant occurredAt

) implements DomainEvent {
}
