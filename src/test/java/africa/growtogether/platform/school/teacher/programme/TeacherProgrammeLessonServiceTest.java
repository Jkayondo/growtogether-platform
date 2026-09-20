package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.timetable.core.Timetable;
import africa.growtogether.platform.school.timetable.core.TimetableRepository;
import africa.growtogether.platform.school.timetable.entry.TimetableEntry;
import africa.growtogether.platform.school.timetable.occurrence.TimetableOccurrenceResolver;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeacherProgrammeLessonServiceTest {

    @Test
    void returnsRecurringLessonThatOccursToday() {

        Fixture f = new Fixture();

        LocalDate today =
                LocalDate.of(
                        2026,
                        8,
                        31
                );

        f.stubDay(today);

        UUID timetableId =
                UUID.randomUUID();

        TimetableEntry entry =
                f.entry(
                        timetableId,
                        "MONDAY",
                        true,
                        null,
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        )
                );

        Timetable timetable =
                f.timetable(
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        )
                );

        when(
                f.lessons.findLessonCandidates(
                        f.tenantId,
                        f.teacherId,
                        today,
                        "MONDAY",
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                List.of(entry)
        );

        when(
                f.timetables.findByTenantIdAndId(
                        f.tenantId,
                        timetableId
                )
        ).thenReturn(
                Optional.of(timetable)
        );

        List<TimetableEntry> result =
                f.service.currentLessons();

        assertEquals(
                1,
                result.size()
        );

        assertSame(
                entry,
                result.get(0)
        );
    }

    @Test
    void filtersNonRecurringCandidateAfterItsSingleOccurrence() {

        Fixture f = new Fixture();

        LocalDate today =
                LocalDate.of(
                        2026,
                        8,
                        31
                );

        f.stubDay(today);

        UUID timetableId =
                UUID.randomUUID();

        /*
         * With no entry-level effective dates, a non-recurring
         * entry falls back to the timetable start date: 24 August.
         *
         * The repository may still return it as a date-range
         * candidate on 31 August, but occurrence evaluation must
         * exclude it from Today's Programme.
         */
        TimetableEntry entry =
                f.entry(
                        timetableId,
                        "MONDAY",
                        false,
                        null,
                        null,
                        null
                );

        Timetable timetable =
                f.timetable(
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        )
                );

        when(
                f.lessons.findLessonCandidates(
                        f.tenantId,
                        f.teacherId,
                        today,
                        "MONDAY",
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                List.of(entry)
        );

        when(
                f.timetables.findByTenantIdAndId(
                        f.tenantId,
                        timetableId
                )
        ).thenReturn(
                Optional.of(timetable)
        );

        assertEquals(
                List.of(),
                f.service.currentLessons()
        );
    }

    @Test
    void keepsNonRecurringCandidateOnItsExplicitOccurrenceDate() {

        Fixture f = new Fixture();

        LocalDate today =
                LocalDate.of(
                        2026,
                        8,
                        31
                );

        f.stubDay(today);

        UUID timetableId =
                UUID.randomUUID();

        TimetableEntry entry =
                f.entry(
                        timetableId,
                        "MONDAY",
                        false,
                        null,
                        today,
                        today
                );

        Timetable timetable =
                f.timetable(
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        )
                );

        when(
                f.lessons.findLessonCandidates(
                        f.tenantId,
                        f.teacherId,
                        today,
                        "MONDAY",
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                List.of(entry)
        );

        when(
                f.timetables.findByTenantIdAndId(
                        f.tenantId,
                        timetableId
                )
        ).thenReturn(
                Optional.of(timetable)
        );

        List<TimetableEntry> result =
                f.service.currentLessons();

        assertEquals(
                1,
                result.size()
        );

        assertSame(
                entry,
                result.get(0)
        );
    }

    @Test
    void refusesToSilentlyIgnoreCustomRecurrenceRule() {

        Fixture f = new Fixture();

        LocalDate today =
                LocalDate.of(
                        2026,
                        8,
                        31
                );

        f.stubDay(today);

        UUID timetableId =
                UUID.randomUUID();

        TimetableEntry entry =
                f.entry(
                        timetableId,
                        "MONDAY",
                        true,
                        "FREQ=WEEKLY;BYDAY=MO",
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        )
                );

        when(
                f.lessons.findLessonCandidates(
                        f.tenantId,
                        f.teacherId,
                        today,
                        "MONDAY",
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                List.of(entry)
        );

        Timetable timetable =
                f.timetable(
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        )
                );

        when(
                f.timetables.findByTenantIdAndId(
                        f.tenantId,
                        timetableId
                )
        ).thenReturn(
                Optional.of(timetable)
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> f.service.currentLessons()
                );

        assertEquals(
                "Custom timetable recurrence rules are not yet supported for availability evaluation",
                error.getMessage()
        );
    }

    @Test
    void requiresTimetableWithinAuthenticatedTenant() {

        Fixture f = new Fixture();

        LocalDate today =
                LocalDate.of(
                        2026,
                        8,
                        31
                );

        f.stubDay(today);

        UUID timetableId =
                UUID.randomUUID();

        TimetableEntry entry =
                f.entry(
                        timetableId,
                        "MONDAY",
                        true,
                        null,
                        null,
                        null
                );

        when(
                f.lessons.findLessonCandidates(
                        f.tenantId,
                        f.teacherId,
                        today,
                        "MONDAY",
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                List.of(entry)
        );

        when(
                f.timetables.findByTenantIdAndId(
                        f.tenantId,
                        timetableId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> f.service.currentLessons()
                );

        assertEquals(
                "Teacher programme timetable not found for tenant",
                error.getMessage()
        );

        verify(
                f.timetables
        ).findByTenantIdAndId(
                f.tenantId,
                timetableId
        );
    }

    @Test
    void reusesTimetableLookupForMultipleCandidatesFromSameTimetable() {

        Fixture f = new Fixture();

        LocalDate today =
                LocalDate.of(
                        2026,
                        8,
                        31
                );

        f.stubDay(today);

        UUID timetableId =
                UUID.randomUUID();

        TimetableEntry first =
                f.entry(
                        timetableId,
                        "MONDAY",
                        true,
                        null,
                        null,
                        null
                );

        TimetableEntry second =
                f.entry(
                        timetableId,
                        "MONDAY",
                        true,
                        null,
                        null,
                        null
                );

        Timetable timetable =
                f.timetable(
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        )
                );

        when(
                f.lessons.findLessonCandidates(
                        f.tenantId,
                        f.teacherId,
                        today,
                        "MONDAY",
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                List.of(
                        first,
                        second
                )
        );

        when(
                f.timetables.findByTenantIdAndId(
                        f.tenantId,
                        timetableId
                )
        ).thenReturn(
                Optional.of(timetable)
        );

        List<TimetableEntry> result =
                f.service.currentLessons();

        assertEquals(
                2,
                result.size()
        );

        assertSame(
                first,
                result.get(0)
        );

        assertSame(
                second,
                result.get(1)
        );

        verify(
                f.timetables,
                times(1)
        ).findByTenantIdAndId(
                f.tenantId,
                timetableId
        );
    }

    @Test
    void avoidsTimetableLookupWhenCandidateListIsEmpty() {

        Fixture f = new Fixture();

        LocalDate today =
                LocalDate.of(
                        2026,
                        8,
                        31
                );

        f.stubDay(today);

        when(
                f.lessons.findLessonCandidates(
                        f.tenantId,
                        f.teacherId,
                        today,
                        "MONDAY",
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                List.of()
        );

        assertEquals(
                List.of(),
                f.service.currentLessons()
        );

        verify(
                f.timetables,
                never()
        ).findByTenantIdAndId(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    private static class Fixture {

        final UUID tenantId =
                UUID.randomUUID();

        final UUID teacherId =
                UUID.randomUUID();

        final TeacherProgrammeDayService days =
                mock(
                        TeacherProgrammeDayService.class
                );

        final TeacherProgrammeLessonRepository lessons =
                mock(
                        TeacherProgrammeLessonRepository.class
                );

        final TimetableRepository timetables =
                mock(
                        TimetableRepository.class
                );

        final TimetableOccurrenceResolver occurrences =
                new TimetableOccurrenceResolver();

        final TeacherProgrammeLessonService service =
                new TeacherProgrammeLessonService(
                        days,
                        lessons,
                        timetables,
                        occurrences
                );

        void stubDay(
                LocalDate date
        ) {

            ZoneId zone =
                    ZoneId.of(
                            "Africa/Kampala"
                    );

            when(
                    days.currentDay()
            ).thenReturn(
                    new TeacherProgrammeDayService.ProgrammeDay(
                            tenantId,
                            teacherId,
                            date,
                            zone,
                            date.atStartOfDay(zone)
                                    .toInstant(),
                            date.plusDays(1)
                                    .atStartOfDay(zone)
                                    .toInstant()
                    )
            );
        }

        TimetableEntry entry(
                UUID timetableId,
                String dayOfWeek,
                boolean recurring,
                String recurrenceRule,
                LocalDate effectiveFrom,
                LocalDate effectiveTo
        ) {

            TimetableEntry entry =
                    mock(
                            TimetableEntry.class
                    );

            when(
                    entry.getTimetableId()
            ).thenReturn(
                    timetableId
            );

            when(
                    entry.getDayOfWeek()
            ).thenReturn(
                    dayOfWeek
            );

            when(
                    entry.isRecurring()
            ).thenReturn(
                    recurring
            );

            when(
                    entry.getRecurrenceRule()
            ).thenReturn(
                    recurrenceRule
            );

            when(
                    entry.getEffectiveFrom()
            ).thenReturn(
                    effectiveFrom
            );

            when(
                    entry.getEffectiveTo()
            ).thenReturn(
                    effectiveTo
            );

            return entry;
        }

        Timetable timetable(
                LocalDate effectiveFrom,
                LocalDate effectiveTo
        ) {

            Timetable timetable =
                    mock(
                            Timetable.class
                    );

            when(
                    timetable.getEffectiveFrom()
            ).thenReturn(
                    effectiveFrom
            );

            when(
                    timetable.getEffectiveTo()
            ).thenReturn(
                    effectiveTo
            );

            return timetable;
        }
    }
}
