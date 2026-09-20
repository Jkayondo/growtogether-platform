package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeacherProgrammeCalendarServiceTest {

    @Test
    void resolvesCurrentSchoolDayOnceAndUsesExactTenantInterval() {

        Fixture f = new Fixture();

        TeacherProgrammeDayService.ProgrammeDay day =
                f.day();

        AcademicCalendarEvent event =
                f.event(
                        "EVT-001",
                        "Staff Meeting",
                        "STAFF_MEETING",
                        Instant.parse("2026-09-18T07:00:00Z"),
                        Instant.parse("2026-09-18T08:00:00Z"),
                        "SCHEDULED"
                );

        when(
                f.days.currentDay()
        ).thenReturn(
                day
        );

        when(
                f.calendar.findCurrentDayEvents(
                        f.tenantId,
                        f.startInclusive,
                        f.endExclusive
                )
        ).thenReturn(
                List.of(event)
        );

        List<TeacherProgrammeCalendarEvent> result =
                f.service.currentEvents();

        assertEquals(
                1,
                result.size()
        );

        TeacherProgrammeCalendarEvent detail =
                result.get(0);

        assertEquals(
                "EVT-001",
                detail.eventCode()
        );

        assertEquals(
                "Staff Meeting",
                detail.eventName()
        );

        assertEquals(
                "STAFF_MEETING",
                detail.eventType()
        );

        assertEquals(
                Instant.parse("2026-09-18T07:00:00Z"),
                detail.startAt()
        );

        assertEquals(
                Instant.parse("2026-09-18T08:00:00Z"),
                detail.endAt()
        );

        assertEquals(
                "SCHEDULED",
                detail.eventStatus()
        );

        verify(
                f.days,
                times(1)
        ).currentDay();

        verify(
                f.calendar,
                times(1)
        ).findCurrentDayEvents(
                f.tenantId,
                f.startInclusive,
                f.endExclusive
        );
    }

    @Test
    void packagePrivateDayOverloadReusesSuppliedProgrammeDay() {

        Fixture f = new Fixture();

        TeacherProgrammeDayService.ProgrammeDay day =
                f.day();

        when(
                f.calendar.findCurrentDayEvents(
                        f.tenantId,
                        f.startInclusive,
                        f.endExclusive
                )
        ).thenReturn(
                List.of()
        );

        List<TeacherProgrammeCalendarEvent> result =
                f.service.currentEvents(day);

        assertEquals(
                List.of(),
                result
        );

        verify(
                f.days,
                never()
        ).currentDay();

        verify(
                f.calendar,
                times(1)
        ).findCurrentDayEvents(
                f.tenantId,
                f.startInclusive,
                f.endExclusive
        );
    }

    @Test
    void rejectsNullProgrammeDayBeforeRepositoryAccess() {

        Fixture f = new Fixture();

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.currentEvents(null)
                );

        assertEquals(
                "programme day must not be null",
                error.getMessage()
        );

        verify(
                f.days,
                never()
        ).currentDay();

        verify(
                f.calendar,
                never()
        ).findCurrentDayEvents(
                any(),
                any(),
                any()
        );
    }

    @Test
    void failsClosedWhenRepositoryReturnsNullEvent() {

        Fixture f = new Fixture();

        TeacherProgrammeDayService.ProgrammeDay day =
                f.day();

        when(
                f.calendar.findCurrentDayEvents(
                        f.tenantId,
                        f.startInclusive,
                        f.endExclusive
                )
        ).thenReturn(
                java.util.Arrays.asList(
                        (AcademicCalendarEvent) null
                )
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> f.service.currentEvents(day)
                );

        assertEquals(
                "Teacher programme calendar query returned null event",
                error.getMessage()
        );
    }

    @Test
    void preservesRepositoryOrderingInMappedResponse() {

        Fixture f = new Fixture();

        TeacherProgrammeDayService.ProgrammeDay day =
                f.day();

        Instant firstStart =
                Instant.parse(
                        "2026-09-18T05:30:00Z"
                );

        Instant secondStart =
                Instant.parse(
                        "2026-09-18T12:00:00Z"
                );

        AcademicCalendarEvent first =
                f.event(
                        "EVT-001",
                        "Assembly",
                        "ASSEMBLY",
                        firstStart,
                        Instant.parse("2026-09-18T06:00:00Z"),
                        "COMPLETED"
                );

        AcademicCalendarEvent second =
                f.event(
                        "EVT-002",
                        "Parents Meeting",
                        "PARENTS_MEETING",
                        secondStart,
                        null,
                        "CONFIRMED"
                );

        when(
                f.calendar.findCurrentDayEvents(
                        f.tenantId,
                        f.startInclusive,
                        f.endExclusive
                )
        ).thenReturn(
                List.of(
                        first,
                        second
                )
        );

        List<TeacherProgrammeCalendarEvent> result =
                f.service.currentEvents(day);

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                "EVT-001",
                result.get(0).eventCode()
        );

        assertEquals(
                "EVT-002",
                result.get(1).eventCode()
        );

        assertSame(
                firstStart,
                result.get(0).startAt()
        );

        assertSame(
                secondStart,
                result.get(1).startAt()
        );
    }

    private static class Fixture {

        final UUID tenantId =
                UUID.randomUUID();

        final UUID teacherId =
                UUID.randomUUID();

        final ZoneId zone =
                ZoneId.of(
                        "Africa/Kampala"
                );

        final LocalDate date =
                LocalDate.of(
                        2026,
                        9,
                        18
                );

        final Instant startInclusive =
                date.atStartOfDay(zone)
                        .toInstant();

        final Instant endExclusive =
                date.plusDays(1)
                        .atStartOfDay(zone)
                        .toInstant();

        final TeacherProgrammeDayService days =
                mock(
                        TeacherProgrammeDayService.class
                );

        final TeacherProgrammeCalendarRepository calendar =
                mock(
                        TeacherProgrammeCalendarRepository.class
                );

        final TeacherProgrammeCalendarService service =
                new TeacherProgrammeCalendarService(
                        days,
                        calendar
                );

        TeacherProgrammeDayService.ProgrammeDay day() {

            return new TeacherProgrammeDayService.ProgrammeDay(
                    tenantId,
                    teacherId,
                    date,
                    zone,
                    startInclusive,
                    endExclusive
            );
        }

        AcademicCalendarEvent event(
                String eventCode,
                String eventName,
                String eventType,
                Instant startAt,
                Instant endAt,
                String eventStatus
        ) {

            AcademicCalendarEvent event =
                    mock(
                            AcademicCalendarEvent.class
                    );

            UUID id =
                    UUID.randomUUID();

            when(
                    event.getId()
            ).thenReturn(
                    id
            );

            when(
                    event.getEventCode()
            ).thenReturn(
                    eventCode
            );

            when(
                    event.getEventName()
            ).thenReturn(
                    eventName
            );

            when(
                    event.getEventType()
            ).thenReturn(
                    eventType
            );

            when(
                    event.getStartAt()
            ).thenReturn(
                    startAt
            );

            when(
                    event.getEndAt()
            ).thenReturn(
                    endAt
            );

            when(
                    event.getEventStatus()
            ).thenReturn(
                    eventStatus
            );

            return event;
        }
    }
}
