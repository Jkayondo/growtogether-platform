package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEvent;
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
 */
@Service
public class TeacherProgrammeCalendarService {

    private final TeacherProgrammeDayService days;

    private final TeacherProgrammeCalendarRepository calendar;

    public TeacherProgrammeCalendarService(
            TeacherProgrammeDayService days,
            TeacherProgrammeCalendarRepository calendar
    ) {

        this.days = days;
        this.calendar = calendar;
    }

    @Transactional(readOnly = true)
    public List<TeacherProgrammeCalendarEvent> currentEvents() {

        TeacherProgrammeDayService.ProgrammeDay day =
                days.currentDay();

        return currentEvents(day);
    }

    /*
     * Package-private so the later Today Programme aggregate can resolve
     * ProgrammeDay once and reuse exactly the same tenant/date interval
     * for lessons and calendar events.
     */
    List<TeacherProgrammeCalendarEvent> currentEvents(
            TeacherProgrammeDayService.ProgrammeDay day
    ) {

        if (day == null) {
            throw new IllegalArgumentException(
                    "programme day must not be null"
            );
        }

        return calendar
                .findCurrentDayEvents(
                        day.tenantId(),
                        day.startInclusive(),
                        day.endExclusive()
                )
                .stream()
                .map(
                        TeacherProgrammeCalendarService::toDetail
                )
                .toList();
    }

    private static TeacherProgrammeCalendarEvent toDetail(
            AcademicCalendarEvent event
    ) {

        if (event == null) {
            throw new IllegalStateException(
                    "Teacher programme calendar query returned null event"
            );
        }

        return new TeacherProgrammeCalendarEvent(
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
