package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.academic.teaching.TeacherProfileRepository;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeacherUnavailabilityServiceTest {

    @Test
    void createsTenantScopedTeacherUnavailability() {

        TeacherUnavailabilityRepository repository =
                mock(TeacherUnavailabilityRepository.class);

        TeacherProfileRepository teachers =
                mock(TeacherProfileRepository.class);

        TeacherUnavailabilityService service =
                new TeacherUnavailabilityService(
                        repository,
                        teachers
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID teacherId =
                UUID.randomUUID();

        Instant from =
                Instant.parse(
                        "2026-08-21T06:00:00Z"
                );

        Instant to =
                Instant.parse(
                        "2026-08-21T10:00:00Z"
                );

        when(
                teachers.findByTenantIdAndId(
                        tenantId,
                        teacherId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                repository
                        .findByTenantIdAndTeacherProfileIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
                                tenantId,
                                teacherId,
                                to,
                                from,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of()
        );

        when(
                repository.save(
                        any(TeacherUnavailability.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        TeacherUnavailability result =
                service.create(
                        tenantId,
                        new CreateTeacherUnavailabilityCommand(
                                teacherId,
                                from,
                                to,
                                "training",
                                "Curriculum training",
                                false,
                                null,
                                null
                        )
                );

        assertEquals(
                teacherId,
                result.getTeacherProfileId()
        );

        assertEquals(
                from,
                result.getUnavailableFrom()
        );

        assertEquals(
                to,
                result.getUnavailableTo()
        );

        assertEquals(
                "TRAINING",
                result.getReasonType()
        );

        assertFalse(
                result.isRecurring()
        );

        verify(
                repository
        ).save(
                any(TeacherUnavailability.class)
        );
    }

    @Test
    void rejectsTeacherOutsideTenantBoundary() {

        TeacherUnavailabilityRepository repository =
                mock(TeacherUnavailabilityRepository.class);

        TeacherProfileRepository teachers =
                mock(TeacherProfileRepository.class);

        TeacherUnavailabilityService service =
                new TeacherUnavailabilityService(
                        repository,
                        teachers
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID teacherId =
                UUID.randomUUID();

        when(
                teachers.findByTenantIdAndId(
                        tenantId,
                        teacherId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command(
                                        teacherId
                                )
                        )
                );

        assertEquals(
                "Teacher profile not found for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherUnavailability.class)
        );
    }

    @Test
    void rejectsInvalidTeacherUnavailabilityDates() {

        UUID teacherId =
                UUID.randomUUID();

        Instant from =
                Instant.parse(
                        "2026-08-21T10:00:00Z"
                );

        Instant to =
                Instant.parse(
                        "2026-08-21T09:00:00Z"
                );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new TeacherUnavailability(
                                teacherId,
                                from,
                                to,
                                "MEDICAL",
                                "Medical appointment",
                                false,
                                null,
                                null
                        )
                );

        assertEquals(
                "unavailableTo must be after unavailableFrom",
                error.getMessage()
        );
    }

    @Test
    void rejectsUnsupportedTeacherUnavailabilityReason() {

        TeacherUnavailabilityRepository repository =
                mock(TeacherUnavailabilityRepository.class);

        TeacherProfileRepository teachers =
                mock(TeacherProfileRepository.class);

        TeacherUnavailabilityService service =
                new TeacherUnavailabilityService(
                        repository,
                        teachers
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID teacherId =
                UUID.randomUUID();

        when(
                teachers.findByTenantIdAndId(
                        tenantId,
                        teacherId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                repository
                        .findByTenantIdAndTeacherProfileIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
                                any(),
                                any(),
                                any(),
                                any(),
                                any()
                        )
        ).thenReturn(
                List.of()
        );

        CreateTeacherUnavailabilityCommand command =
                new CreateTeacherUnavailabilityCommand(
                        teacherId,
                        Instant.parse(
                                "2026-08-21T06:00:00Z"
                        ),
                        Instant.parse(
                                "2026-08-21T10:00:00Z"
                        ),
                        "UNKNOWN_REASON",
                        null,
                        false,
                        null,
                        null
                );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command
                        )
                );

        assertEquals(
                "Invalid teacher unavailability reason type: UNKNOWN_REASON",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherUnavailability.class)
        );
    }

    @Test
    void rejectsOverlappingTeacherUnavailability() {

        TeacherUnavailabilityRepository repository =
                mock(TeacherUnavailabilityRepository.class);

        TeacherProfileRepository teachers =
                mock(TeacherProfileRepository.class);

        TeacherUnavailabilityService service =
                new TeacherUnavailabilityService(
                        repository,
                        teachers
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID teacherId =
                UUID.randomUUID();

        Instant from =
                Instant.parse(
                        "2026-08-21T06:00:00Z"
                );

        Instant to =
                Instant.parse(
                        "2026-08-21T10:00:00Z"
                );

        when(
                teachers.findByTenantIdAndId(
                        tenantId,
                        teacherId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                repository
                        .findByTenantIdAndTeacherProfileIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
                                tenantId,
                                teacherId,
                                to,
                                from,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(
                        mock(TeacherUnavailability.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                new CreateTeacherUnavailabilityCommand(
                                        teacherId,
                                        from,
                                        to,
                                        "OFFICIAL_DUTY",
                                        "District education meeting",
                                        false,
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "Teacher already has overlapping unavailability",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherUnavailability.class)
        );
    }

    private static CreateTeacherUnavailabilityCommand command(
            UUID teacherId
    ) {

        return new CreateTeacherUnavailabilityCommand(
                teacherId,
                Instant.parse(
                        "2026-08-21T06:00:00Z"
                ),
                Instant.parse(
                        "2026-08-21T10:00:00Z"
                ),
                "LEAVE",
                "Approved leave",
                false,
                null,
                null
        );
    }
}
