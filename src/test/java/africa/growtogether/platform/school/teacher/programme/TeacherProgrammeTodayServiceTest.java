package africa.growtogether.platform.school.teacher.programme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

class TeacherProgrammeTodayServiceTest {

    @Test
    void currentProgrammeReusesExactlyOneAuthoritativeProgrammeDay() {

        TeacherProgrammeDayService days =
                mock(TeacherProgrammeDayService.class);

        TeacherProgrammeLessonDetailService lessons =
                mock(TeacherProgrammeLessonDetailService.class);

        TeacherProgrammeCalendarService calendar =
                mock(TeacherProgrammeCalendarService.class);

        TeacherProgrammeTodayService service =
                new TeacherProgrammeTodayService(
                        days,
                        lessons,
                        calendar
                );

        UUID tenantId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        LocalDate date =
                LocalDate.of(
                        2026,
                        9,
                        19
                );

        ZoneId zone =
                ZoneId.of(
                        "Africa/Kampala"
                );

        TeacherProgrammeDayService.ProgrammeDay day =
                new TeacherProgrammeDayService.ProgrammeDay(
                        tenantId,
                        teacherId,
                        date,
                        zone,
                        Instant.parse(
                                "2026-09-18T21:00:00Z"
                        ),
                        Instant.parse(
                                "2026-09-19T21:00:00Z"
                        )
                );

        TeacherProgrammeLessonDetail lesson =
                new TeacherProgrammeLessonDetail(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "P1",
                        "Period 1",
                        1,
                        LocalTime.of(
                                8,
                                0
                        ),
                        LocalTime.of(
                                8,
                                40
                        ),
                        UUID.randomUUID(),
                        "P7",
                        "Primary Seven",
                        null,
                        null,
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "MAT",
                        "Mathematics",
                        "Mathematics Lesson"
                );

        TeacherProgrammeCalendarEvent event =
                new TeacherProgrammeCalendarEvent(
                        UUID.randomUUID(),
                        "EVENT-001",
                        "Parents Meeting",
                        "MEETING",
                        Instant.parse(
                                "2026-09-19T07:00:00Z"
                        ),
                        Instant.parse(
                                "2026-09-19T09:00:00Z"
                        ),
                        "SCHEDULED"
                );

        when(
                days.currentDay()
        ).thenReturn(
                day
        );

        when(
                lessons.currentLessonDetails(
                        same(day)
                )
        ).thenReturn(
                List.of(
                        lesson
                )
        );

        when(
                calendar.currentEvents(
                        same(day)
                )
        ).thenReturn(
                List.of(
                        event
                )
        );

        TeacherProgrammeToday result =
                service.currentProgramme();

        assertThat(result.date())
                .isEqualTo(date);

        assertThat(result.zone())
                .isEqualTo(zone);

        assertThat(result.lessons())
                .containsExactly(
                        lesson
                );

        assertThat(result.calendarEvents())
                .containsExactly(
                        event
                );

        verify(
                days,
                times(1)
        ).currentDay();

        verify(
                lessons,
                times(1)
        ).currentLessonDetails(
                same(day)
        );

        verify(
                calendar,
                times(1)
        ).currentEvents(
                same(day)
        );
    }

    @Test
    void currentProgrammePreservesEmptyProgrammeSections() {

        TeacherProgrammeDayService days =
                mock(TeacherProgrammeDayService.class);

        TeacherProgrammeLessonDetailService lessons =
                mock(TeacherProgrammeLessonDetailService.class);

        TeacherProgrammeCalendarService calendar =
                mock(TeacherProgrammeCalendarService.class);

        TeacherProgrammeTodayService service =
                new TeacherProgrammeTodayService(
                        days,
                        lessons,
                        calendar
                );

        TeacherProgrammeDayService.ProgrammeDay day =
                new TeacherProgrammeDayService.ProgrammeDay(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LocalDate.of(
                                2026,
                                9,
                                19
                        ),
                        ZoneId.of(
                                "Africa/Kampala"
                        ),
                        Instant.parse(
                                "2026-09-18T21:00:00Z"
                        ),
                        Instant.parse(
                                "2026-09-19T21:00:00Z"
                        )
                );

        when(
                days.currentDay()
        ).thenReturn(
                day
        );

        when(
                lessons.currentLessonDetails(
                        same(day)
                )
        ).thenReturn(
                List.of()
        );

        when(
                calendar.currentEvents(
                        same(day)
                )
        ).thenReturn(
                List.of()
        );

        TeacherProgrammeToday result =
                service.currentProgramme();

        assertThat(result.lessons())
                .isEmpty();

        assertThat(result.calendarEvents())
                .isEmpty();

        verify(
                days,
                times(1)
        ).currentDay();

        verify(
                lessons,
                times(1)
        ).currentLessonDetails(
                same(day)
        );

        verify(
                calendar,
                times(1)
        ).currentEvents(
                same(day)
        );
    }
}
