package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.profile.SchoolProfileService;

import africa.growtogether.platform.school.timetable.bell.BellPeriod;
import africa.growtogether.platform.school.timetable.core.Timetable;
import africa.growtogether.platform.school.timetable.entry.CreateTimetableEntryCommand;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimetableAvailabilityEvaluatorTest {

    @Test
    void detectsTeacherUnavailableUsingSchoolLocalTimezone() {

        Fixture f = new Fixture();

        f.stubSchoolTimezone();

        /*
         * Africa/Kampala = UTC+3.
         *
         * P3 is 10:20–11:00 school time,
         * therefore 07:20–08:00 UTC.
         *
         * Teacher unavailable 07:30–09:00 UTC,
         * therefore the intervals overlap.
         */
        TeacherUnavailability unavailable =
                mock(TeacherUnavailability.class);

        when(
                unavailable.isRecurring()
        ).thenReturn(false);

        when(
                unavailable.getUnavailableFrom()
        ).thenReturn(
                Instant.parse(
                        "2026-08-24T07:30:00Z"
                )
        );

        when(
                unavailable.getUnavailableTo()
        ).thenReturn(
                Instant.parse(
                        "2026-08-24T09:00:00Z"
                )
        );

        when(
                unavailable.getReasonType()
        ).thenReturn("MEDICAL");

        when(
                f.teacherUnavailability
                        .findByTenantIdAndTeacherProfileIdAndStatus(
                                f.tenantId,
                                f.teacherId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(unavailable)
        );

        Optional<TimetableAvailabilityConflict> result =
                f.evaluator.evaluate(
                        f.tenantId,
                        f.timetable,
                        f.bellPeriod,
                        f.command(
                                f.teacherId,
                                null,
                                false,
                                null,
                                LocalDate.of(
                                        2026,
                                        8,
                                        24
                                ),
                                LocalDate.of(
                                        2026,
                                        8,
                                        24
                                )
                        )
                );

        assertTrue(
                result.isPresent()
        );

        assertEquals(
                "TEACHER_UNAVAILABLE",
                result.get().conflictType()
        );

        assertTrue(
                result.get()
                        .description()
                        .contains(
                                "2026-08-24"
                        )
        );

        assertTrue(
                result.get()
                        .description()
                        .contains(
                                "P3"
                        )
        );

        assertTrue(
                result.get()
                        .description()
                        .contains(
                                "MEDICAL"
                        )
        );
    }

    @Test
    void checksEveryWeeklyOccurrenceAcrossEffectiveRange() {

        Fixture f = new Fixture();

        f.stubSchoolTimezone();

        /*
         * Entry begins Monday 24 August.
         * Teacher is available that day,
         * but unavailable on the NEXT Monday.
         *
         * This proves GT checks the complete
         * recurring timetable range.
         */
        TeacherUnavailability unavailable =
                mock(TeacherUnavailability.class);

        when(
                unavailable.isRecurring()
        ).thenReturn(false);

        when(
                unavailable.getUnavailableFrom()
        ).thenReturn(
                Instant.parse(
                        "2026-08-31T07:30:00Z"
                )
        );

        when(
                unavailable.getUnavailableTo()
        ).thenReturn(
                Instant.parse(
                        "2026-08-31T08:30:00Z"
                )
        );

        when(
                unavailable.getReasonType()
        ).thenReturn("TRAINING");

        when(
                f.teacherUnavailability
                        .findByTenantIdAndTeacherProfileIdAndStatus(
                                f.tenantId,
                                f.teacherId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(unavailable)
        );

        Optional<TimetableAvailabilityConflict> result =
                f.evaluator.evaluate(
                        f.tenantId,
                        f.timetable,
                        f.bellPeriod,
                        f.command(
                                f.teacherId,
                                null,
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
                        )
                );

        assertTrue(
                result.isPresent()
        );

        assertEquals(
                "TEACHER_UNAVAILABLE",
                result.get().conflictType()
        );

        assertTrue(
                result.get()
                        .description()
                        .contains(
                                "2026-08-31"
                        )
        );
    }

    @Test
    void detectsSchedulingResourceUnavailable() {

        Fixture f = new Fixture();

        f.stubSchoolTimezone();

        ResourceUnavailability unavailable =
                mock(ResourceUnavailability.class);

        when(
                unavailable.isRecurring()
        ).thenReturn(false);

        when(
                unavailable.getUnavailableFrom()
        ).thenReturn(
                Instant.parse(
                        "2026-08-24T07:00:00Z"
                )
        );

        when(
                unavailable.getUnavailableTo()
        ).thenReturn(
                Instant.parse(
                        "2026-08-24T08:00:00Z"
                )
        );

        when(
                unavailable.getReasonType()
        ).thenReturn("MAINTENANCE");

        when(
                f.resourceUnavailability
                        .findByTenantIdAndSchedulingResourceIdAndStatus(
                                f.tenantId,
                                f.resourceId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(unavailable)
        );

        Optional<TimetableAvailabilityConflict> result =
                f.evaluator.evaluate(
                        f.tenantId,
                        f.timetable,
                        f.bellPeriod,
                        f.command(
                                null,
                                f.resourceId,
                                false,
                                null,
                                LocalDate.of(
                                        2026,
                                        8,
                                        24
                                ),
                                LocalDate.of(
                                        2026,
                                        8,
                                        24
                                )
                        )
                );

        assertTrue(
                result.isPresent()
        );

        assertEquals(
                "RESOURCE_UNAVAILABLE",
                result.get().conflictType()
        );

        assertTrue(
                result.get()
                        .description()
                        .contains(
                                "MAINTENANCE"
                        )
        );
    }

    @Test
    void allowsLessonWhenUnavailabilityEndsExactlyAtLessonStart() {

        Fixture f = new Fixture();

        f.stubSchoolTimezone();

        TeacherUnavailability unavailable =
                mock(TeacherUnavailability.class);

        when(
                unavailable.isRecurring()
        ).thenReturn(false);

        /*
         * Lesson starts 07:20 UTC.
         * Unavailability ends exactly 07:20 UTC.
         *
         * Adjacent periods must not be treated
         * as overlapping.
         */
        when(
                unavailable.getUnavailableFrom()
        ).thenReturn(
                Instant.parse(
                        "2026-08-24T06:00:00Z"
                )
        );

        when(
                unavailable.getUnavailableTo()
        ).thenReturn(
                Instant.parse(
                        "2026-08-24T07:20:00Z"
                )
        );

        when(
                unavailable.getReasonType()
        ).thenReturn("MEETING");

        when(
                f.teacherUnavailability
                        .findByTenantIdAndTeacherProfileIdAndStatus(
                                f.tenantId,
                                f.teacherId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(unavailable)
        );

        Optional<TimetableAvailabilityConflict> result =
                f.evaluator.evaluate(
                        f.tenantId,
                        f.timetable,
                        f.bellPeriod,
                        f.command(
                                f.teacherId,
                                null,
                                false,
                                null,
                                LocalDate.of(
                                        2026,
                                        8,
                                        24
                                ),
                                LocalDate.of(
                                        2026,
                                        8,
                                        24
                                )
                        )
                );

        assertTrue(
                result.isEmpty()
        );
    }

    @Test
    void refusesToSilentlyIgnoreRecurringTeacherUnavailability() {

        Fixture f = new Fixture();

        f.stubSchoolTimezone();

        TeacherUnavailability unavailable =
                mock(TeacherUnavailability.class);

        when(
                unavailable.isRecurring()
        ).thenReturn(true);

        when(
                f.teacherUnavailability
                        .findByTenantIdAndTeacherProfileIdAndStatus(
                                f.tenantId,
                                f.teacherId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(unavailable)
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> f.evaluator.evaluate(
                                f.tenantId,
                                f.timetable,
                                f.bellPeriod,
                                f.command(
                                        f.teacherId,
                                        null,
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
                                )
                        )
                );

        assertEquals(
                "Recurring teacher unavailability requires recurrence-rule evaluation",
                error.getMessage()
        );
    }

    private static class Fixture {

        final SchoolProfileService schoolProfiles =
                mock(SchoolProfileService.class);

        final TeacherUnavailabilityRepository teacherUnavailability =
                mock(TeacherUnavailabilityRepository.class);

        final ResourceUnavailabilityRepository resourceUnavailability =
                mock(ResourceUnavailabilityRepository.class);

        final TimetableAvailabilityEvaluator evaluator =
                new TimetableAvailabilityEvaluator(
                        schoolProfiles,
                        teacherUnavailability,
                        resourceUnavailability
                );

        final UUID tenantId =
                UUID.randomUUID();

        final UUID teacherId =
                UUID.randomUUID();

        final UUID resourceId =
                UUID.randomUUID();

        final UUID timetableId =
                UUID.randomUUID();

        final UUID bellPeriodId =
                UUID.randomUUID();

        final Timetable timetable =
                mock(Timetable.class);

        final BellPeriod bellPeriod =
                mock(BellPeriod.class);

        void stubSchoolTimezone() {

            when(
                    schoolProfiles.requireTimezone(
                            tenantId
                    )
            ).thenReturn(
                    ZoneId.of(
                            "Africa/Kampala"
                    )
            );

            when(
                    timetable.getEffectiveFrom()
            ).thenReturn(
                    LocalDate.of(
                            2026,
                            8,
                            24
                    )
            );

            when(
                    timetable.getEffectiveTo()
            ).thenReturn(
                    LocalDate.of(
                            2026,
                            9,
                            7
                    )
            );

            when(
                    bellPeriod.getStartTime()
            ).thenReturn(
                    LocalTime.of(
                            10,
                            20
                    )
            );

            when(
                    bellPeriod.getEndTime()
            ).thenReturn(
                    LocalTime.of(
                            11,
                            0
                    )
            );

            when(
                    bellPeriod.getPeriodCode()
            ).thenReturn("P3");
        }

        CreateTimetableEntryCommand command(
                UUID teacherProfileId,
                UUID schedulingResourceId,
                Boolean recurring,
                String recurrenceRule,
                LocalDate effectiveFrom,
                LocalDate effectiveTo
        ) {

            return new CreateTimetableEntryCommand(
                    timetableId,
                    bellPeriodId,
                    "MONDAY",
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    UUID.randomUUID(),
                    teacherProfileId,
                    schedulingResourceId,
                    "LESSON",
                    null,
                    "Availability evaluation test",
                    recurring,
                    recurrenceRule,
                    effectiveFrom,
                    effectiveTo
            );
        }
    }
}
