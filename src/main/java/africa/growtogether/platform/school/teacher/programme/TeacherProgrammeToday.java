package africa.growtogether.platform.school.teacher.programme;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

/**
 * Authenticated teacher's display-ready programme for one school-local day.
 */
public record TeacherProgrammeToday(
        LocalDate date,
        ZoneId zone,
        List<TeacherProgrammeLessonDetail> lessons,
        List<TeacherProgrammeCalendarEvent> calendarEvents
) {

    public TeacherProgrammeToday {
        Objects.requireNonNull(
                date,
                "date must not be null"
        );
        Objects.requireNonNull(
                zone,
                "zone must not be null"
        );
        lessons = List.copyOf(
                Objects.requireNonNull(
                        lessons,
                        "lessons must not be null"
                )
        );
        calendarEvents = List.copyOf(
                Objects.requireNonNull(
                        calendarEvents,
                        "calendarEvents must not be null"
                )
        );
    }
}
