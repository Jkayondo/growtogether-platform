package africa.growtogether.platform.school.academic.calendar.events;


import africa.growtogether.platform.common.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;


public record AcademicCalendarEventCreatedEvent(

        UUID eventId,

        UUID tenantId,

        String eventType,

        String eventName,

        Instant startAt,

        boolean notificationRequired,

        Instant occurredAt

) implements DomainEvent {
}
