package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEvent;
import africa.growtogether.platform.school.programme.ProgrammeCalendarEvent;
import africa.growtogether.platform.school.programme.ProgrammeCalendarEventProjector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Resolves academic-calendar events belonging to the same authenticated
 * tenant and school-local day used by Teacher Today Programme lessons.
 *
 * No teacher-campus filter is applied because the current TeacherProfile
 * model does not establish one authoritative campus for a teacher.
 * Campus association exists at individual assignment level, while the
 * academic calendar itself permits tenant-wide events with null campus.
 *
 * Calendar query/projection semantics are shared with other school
 * programme experiences. This adapter preserves the established
 * Teacher-facing DTO and failure contract.
 */
@Service
public class TeacherProgrammeCalendarService {

    private final TeacherProgrammeDayService days;
    private final TeacherProgrammeCalendarRepository calendar;
    private final ProgrammeCalendarEventProjector projector;

    @Autowired
    public TeacherProgrammeCalendarService(
            TeacherProgrammeDayService days,
            TeacherProgrammeCalendarRepository calendar,
            ProgrammeCalendarEventProjector projector
    ) {
        this.days = days;
        this.calendar = calendar;
        this.projector = projector;
    }

    /**
     * Compatibility constructor retained for existing focused tests and
     * callers while projection ownership resides in the shared
     * programme capability.
     */
    public TeacherProgrammeCalendarService(
            TeacherProgrammeDayService days,
            TeacherProgrammeCalendarRepository calendar
    ) {
        this(
                days,
                calendar,
                new ProgrammeCalendarEventProjector()
        );
    }

    @Transactional(readOnly = true)
    public List<TeacherProgrammeCalendarEvent> currentEvents() {

        TeacherProgrammeDayService.ProgrammeDay day =
                days.currentDay();

        return currentEvents(
                day
        );
    }

    List<TeacherProgrammeCalendarEvent> currentEvents(
            TeacherProgrammeDayService.ProgrammeDay day
    ) {
        if (day == null) {
            throw new IllegalArgumentException(
                    "programme day must not be null"
            );
        }

        List<AcademicCalendarEvent> events =
                calendar.findCurrentDayEvents(
                        day.tenantId(),
                        day.startInclusive(),
                        day.endExclusive()
                );

        try {
            return projector
                    .project(
                            events
                    )
                    .stream()
                    .map(
                            TeacherProgrammeCalendarService::toTeacherEvent
                    )
                    .toList();
        } catch (IllegalStateException ex) {
            if (
                    "Programme calendar query returned null event"
                            .equals(
                                    ex.getMessage()
                            )
            ) {
                throw new IllegalStateException(
                        "Teacher programme calendar query returned null event",
                        ex
                );
            }

            throw ex;
        }
    }

    private static TeacherProgrammeCalendarEvent toTeacherEvent(
            ProgrammeCalendarEvent event
    ) {
        return new TeacherProgrammeCalendarEvent(
                event.id(),
                event.eventCode(),
                event.eventName(),
                event.eventType(),
                event.startAt(),
                event.endAt(),
                event.eventStatus()
        );
    }
}
