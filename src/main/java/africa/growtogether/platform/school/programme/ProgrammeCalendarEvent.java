package africa.growtogether.platform.school.programme;

import java.time.Instant;
import java.util.UUID;

/**
 * Display-ready academic-calendar item shared by school Today
 * Programme experiences.
 */
public record ProgrammeCalendarEvent(
        UUID id,
        String eventCode,
        String eventName,
        String eventType,
        Instant startAt,
        Instant endAt,
        String eventStatus
) {
}
