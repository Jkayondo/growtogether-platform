package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.profile.SchoolProfileService;
import africa.growtogether.platform.school.timetable.bell.BellPeriod;

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

class TimetableCandidateAvailabilityEvaluatorTest {

    @Test
    void detectsTeacherConflictOnLaterWeeklyCandidateOccurrence() {

        Fixture f = new Fixture();

        TeacherUnavailability unavailable =
                mock(TeacherUnavailability.class);

        when(unavailable.isRecurring())
                .thenReturn(false);

        /*
         * P3 = 10:20–11:00 Africa/Kampala
         *    = 07:20–08:00 UTC.
         *
         * First Monday is available.
         * Teacher becomes unavailable on the second Monday.
         */
        when(unavailable.getUnavailableFrom())
                .thenReturn(
                        Instant.parse(
                                "2026-08-31T07:30:00Z"
                        )
                );

        when(unavailable.getUnavailableTo())
                .thenReturn(
                        Instant.parse(
                                "2026-08-31T08:30:00Z"
                        )
                );

        when(unavailable.getReasonType())
                .thenReturn("TRAINING");

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
                f.evaluator.evaluateCandidate(
                        f.tenantId,
                        f.bellPeriod,
                        "MONDAY",
                        f.teacherId,
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

        assertTrue(
                result.get()
                        .description()
                        .contains(
                                "TRAINING"
                        )
        );
    }

    @Test
    void detectsResourceConflictForCandidateSlot() {

        Fixture f = new Fixture();

        ResourceUnavailability unavailable =
                mock(ResourceUnavailability.class);

        when(unavailable.isRecurring())
                .thenReturn(false);

        when(unavailable.getUnavailableFrom())
                .thenReturn(
                        Instant.parse(
                                "2026-08-24T07:00:00Z"
                        )
                );

        when(unavailable.getUnavailableTo())
                .thenReturn(
                        Instant.parse(
                                "2026-08-24T08:00:00Z"
                        )
                );

        when(unavailable.getReasonType())
                .thenReturn("MAINTENANCE");

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
                f.evaluator.evaluateCandidate(
                        f.tenantId,
                        f.bellPeriod,
                        "MONDAY",
                        null,
                        f.resourceId,
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
    void allowsCandidateWhenUnavailabilityEndsExactlyAtLessonStart() {

        Fixture f = new Fixture();

        TeacherUnavailability unavailable =
                mock(TeacherUnavailability.class);

        when(unavailable.isRecurring())
                .thenReturn(false);

        /*
         * P3 begins at 07:20 UTC.
         * Availability restriction ends exactly at 07:20.
         *
         * Adjacent intervals are not conflicts.
         */
        when(unavailable.getUnavailableFrom())
                .thenReturn(
                        Instant.parse(
                                "2026-08-24T06:00:00Z"
                        )
                );

        when(unavailable.getUnavailableTo())
                .thenReturn(
                        Instant.parse(
                                "2026-08-24T07:20:00Z"
                        )
                );

        when(unavailable.getReasonType())
                .thenReturn("MEETING");

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
                f.evaluator.evaluateCandidate(
                        f.tenantId,
                        f.bellPeriod,
                        "MONDAY",
                        f.teacherId,
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
                );

        assertTrue(
                result.isEmpty()
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

        final BellPeriod bellPeriod =
                mock(BellPeriod.class);

        Fixture() {

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
                    bellPeriod.getPeriodCode()
            ).thenReturn(
                    "P3"
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
                    teacherUnavailability
                            .findByTenantIdAndTeacherProfileIdAndStatus(
                                    tenantId,
                                    teacherId,
                                    EntityStatus.ACTIVE
                            )
            ).thenReturn(
                    List.of()
            );

            when(
                    resourceUnavailability
                            .findByTenantIdAndSchedulingResourceIdAndStatus(
                                    tenantId,
                                    resourceId,
                                    EntityStatus.ACTIVE
                            )
            ).thenReturn(
                    List.of()
            );
        }
    }
}
