package africa.growtogether.platform.school.teacher.programme;

import java.time.Instant;
import java.util.UUID;

/**
 * Display-ready academic calendar item for Teacher Today Programme.
 */
public record TeacherProgrammeCalendarEvent(
        UUID id,
        String eventCode,
        String eventName,
        String eventType,
        Instant startAt,
        Instant endAt,
        String eventStatus
) {
}
