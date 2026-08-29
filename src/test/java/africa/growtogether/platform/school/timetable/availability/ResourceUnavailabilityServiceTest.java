package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.timetable.resource.SchedulingResource;
import africa.growtogether.platform.school.timetable.resource.SchedulingResourceRepository;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResourceUnavailabilityServiceTest {

    @Test
    void createsTenantScopedResourceUnavailability() {

        ResourceUnavailabilityRepository repository =
                mock(ResourceUnavailabilityRepository.class);

        SchedulingResourceRepository resources =
                mock(SchedulingResourceRepository.class);

        ResourceUnavailabilityService service =
                new ResourceUnavailabilityService(
                        repository,
                        resources
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID resourceId =
                UUID.randomUUID();

        Instant from =
                Instant.parse("2026-08-21T06:00:00Z");

        Instant to =
                Instant.parse("2026-08-21T10:00:00Z");

        when(
                resources.findByTenantIdAndId(
                        tenantId,
                        resourceId
                )
        ).thenReturn(
                Optional.of(
                        mock(SchedulingResource.class)
                )
        );

        when(
                repository
                        .findByTenantIdAndSchedulingResourceIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
                                tenantId,
                                resourceId,
                                to,
                                from,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of()
        );

        when(
                repository.save(
                        any(ResourceUnavailability.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ResourceUnavailability result =
                service.create(
                        tenantId,
                        new CreateResourceUnavailabilityCommand(
                                resourceId,
                                from,
                                to,
                                "maintenance",
                                "Laboratory equipment servicing",
                                false,
                                null,
                                null
                        )
                );

        assertEquals(
                resourceId,
                result.getSchedulingResourceId()
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
                "MAINTENANCE",
                result.getReasonType()
        );

        assertFalse(
                result.isRecurring()
        );

        verify(
                repository
        ).save(
                any(ResourceUnavailability.class)
        );
    }

    @Test
    void rejectsResourceOutsideTenantBoundary() {

        ResourceUnavailabilityRepository repository =
                mock(ResourceUnavailabilityRepository.class);

        SchedulingResourceRepository resources =
                mock(SchedulingResourceRepository.class);

        ResourceUnavailabilityService service =
                new ResourceUnavailabilityService(
                        repository,
                        resources
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID resourceId =
                UUID.randomUUID();

        when(
                resources.findByTenantIdAndId(
                        tenantId,
                        resourceId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command(resourceId)
                        )
                );

        assertEquals(
                "Scheduling resource not found for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(ResourceUnavailability.class)
        );
    }

    @Test
    void rejectsInvalidUnavailabilityDates() {

        UUID resourceId =
                UUID.randomUUID();

        Instant from =
                Instant.parse("2026-08-21T10:00:00Z");

        Instant to =
                Instant.parse("2026-08-21T09:00:00Z");

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new ResourceUnavailability(
                                resourceId,
                                from,
                                to,
                                "MAINTENANCE",
                                null,
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
    void rejectsUnsupportedReasonType() {

        ResourceUnavailabilityRepository repository =
                mock(ResourceUnavailabilityRepository.class);

        SchedulingResourceRepository resources =
                mock(SchedulingResourceRepository.class);

        ResourceUnavailabilityService service =
                new ResourceUnavailabilityService(
                        repository,
                        resources
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID resourceId =
                UUID.randomUUID();

        when(
                resources.findByTenantIdAndId(
                        tenantId,
                        resourceId
                )
        ).thenReturn(
                Optional.of(
                        mock(SchedulingResource.class)
                )
        );

        when(
                repository
                        .findByTenantIdAndSchedulingResourceIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
                                any(),
                                any(),
                                any(),
                                any(),
                                any()
                        )
        ).thenReturn(
                List.of()
        );

        CreateResourceUnavailabilityCommand command =
                new CreateResourceUnavailabilityCommand(
                        resourceId,
                        Instant.parse("2026-08-21T06:00:00Z"),
                        Instant.parse("2026-08-21T10:00:00Z"),
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
                "Invalid resource unavailability reason type: UNKNOWN_REASON",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(ResourceUnavailability.class)
        );
    }

    @Test
    void rejectsOverlappingResourceUnavailability() {

        ResourceUnavailabilityRepository repository =
                mock(ResourceUnavailabilityRepository.class);

        SchedulingResourceRepository resources =
                mock(SchedulingResourceRepository.class);

        ResourceUnavailabilityService service =
                new ResourceUnavailabilityService(
                        repository,
                        resources
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID resourceId =
                UUID.randomUUID();

        Instant from =
                Instant.parse("2026-08-21T06:00:00Z");

        Instant to =
                Instant.parse("2026-08-21T10:00:00Z");

        when(
                resources.findByTenantIdAndId(
                        tenantId,
                        resourceId
                )
        ).thenReturn(
                Optional.of(
                        mock(SchedulingResource.class)
                )
        );

        when(
                repository
                        .findByTenantIdAndSchedulingResourceIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
                                tenantId,
                                resourceId,
                                to,
                                from,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(
                        mock(ResourceUnavailability.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                new CreateResourceUnavailabilityCommand(
                                        resourceId,
                                        from,
                                        to,
                                        "REPAIR",
                                        "Repair work",
                                        false,
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "Scheduling resource already has overlapping unavailability",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(ResourceUnavailability.class)
        );
    }

    private static CreateResourceUnavailabilityCommand command(
            UUID resourceId
    ) {

        return new CreateResourceUnavailabilityCommand(
                resourceId,
                Instant.parse("2026-08-21T06:00:00Z"),
                Instant.parse("2026-08-21T10:00:00Z"),
                "MAINTENANCE",
                "Scheduled maintenance",
                false,
                null,
                null
        );
    }
}
