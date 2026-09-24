package africa.growtogether.platform.school.programme;

import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEvent;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Shared display projection for already-authorized academic-calendar
 * events. Identity, authorization and day selection remain with the
 * calling Teacher or Learner service.
 */
@Service
public class ProgrammeCalendarEventProjector {

    public List<ProgrammeCalendarEvent> project(
            List<AcademicCalendarEvent> events
    ) {
        if (events == null) {
            throw new IllegalArgumentException(
                    "events must not be null"
            );
        }

        return events
                .stream()
                .map(
                        ProgrammeCalendarEventProjector::toDetail
                )
                .toList();
    }

    private static ProgrammeCalendarEvent toDetail(
            AcademicCalendarEvent event
    ) {
        if (event == null) {
            throw new IllegalStateException(
                    "Programme calendar query returned null event"
            );
        }

        return new ProgrammeCalendarEvent(
                event.getId(),
                event.getEventCode(),
                event.getEventName(),
                event.getEventType(),
                event.getStartAt(),
                event.getEndAt(),
                event.getEventStatus()
        );
    }
}
