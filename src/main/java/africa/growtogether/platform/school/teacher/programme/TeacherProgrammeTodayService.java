package africa.growtogether.platform.school.teacher.programme;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Builds the authenticated teacher's Today Programme from one authoritative
 * ProgrammeDay so lessons and calendar events always use exactly the same
 * tenant, teacher, school-local date, timezone and day interval.
 */
@Service
public class TeacherProgrammeTodayService {

    private final TeacherProgrammeDayService days;
    private final TeacherProgrammeLessonDetailService lessons;
    private final TeacherProgrammeCalendarService calendar;

    public TeacherProgrammeTodayService(
            TeacherProgrammeDayService days,
            TeacherProgrammeLessonDetailService lessons,
            TeacherProgrammeCalendarService calendar
    ) {
        this.days = days;
        this.lessons = lessons;
        this.calendar = calendar;
    }

    @Transactional(readOnly = true)
    public TeacherProgrammeToday currentProgramme() {

        TeacherProgrammeDayService.ProgrammeDay day =
                days.currentDay();

        List<TeacherProgrammeLessonDetail> lessonDetails =
                lessons.currentLessonDetails(
                        day
                );

        List<TeacherProgrammeCalendarEvent> calendarEvents =
                calendar.currentEvents(
                        day
                );

        return new TeacherProgrammeToday(
                day.date(),
                day.zone(),
                lessonDetails,
                calendarEvents
        );
    }
}
