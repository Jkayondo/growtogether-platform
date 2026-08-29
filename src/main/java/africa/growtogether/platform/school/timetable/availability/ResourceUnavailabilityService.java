package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.timetable.resource.SchedulingResourceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ResourceUnavailabilityService {

    private final ResourceUnavailabilityRepository repository;
    private final SchedulingResourceRepository resources;

    public ResourceUnavailabilityService(
            ResourceUnavailabilityRepository repository,
            SchedulingResourceRepository resources
    ) {
        this.repository = repository;
        this.resources = resources;
    }

    @Transactional
    public ResourceUnavailability create(
            UUID tenantId,
            CreateResourceUnavailabilityCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        resources
                .findByTenantIdAndId(
                        tenantId,
                        command.schedulingResourceId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Scheduling resource not found for tenant"
                        )
                );

        /*
         * IMPROVEMENT:
         * Prevent contradictory overlapping unavailability periods
         * for the same scheduling resource.
         */
        List<ResourceUnavailability> overlaps =
                repository
                        .findByTenantIdAndSchedulingResourceIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
                                tenantId,
                                command.schedulingResourceId(),
                                command.unavailableTo(),
                                command.unavailableFrom(),
                                EntityStatus.ACTIVE
                        );

        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException(
                    "Scheduling resource already has overlapping unavailability"
            );
        }

        ResourceUnavailability unavailability =
                new ResourceUnavailability(
                        command.schedulingResourceId(),
                        command.unavailableFrom(),
                        command.unavailableTo(),
                        command.reasonType(),
                        command.reason(),
                        command.recurring(),
                        command.recurrenceRule(),
                        command.workflowInstanceId()
                );

        unavailability.setTenantId(
                tenantId
        );

        return repository.save(
                unavailability
        );
    }

    @Transactional(readOnly = true)
    public ResourceUnavailability get(
            UUID tenantId,
            UUID unavailabilityId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        unavailabilityId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Resource unavailability not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<ResourceUnavailability> findByResource(
            UUID tenantId,
            UUID resourceId
    ) {

        return repository
                .findByTenantIdAndSchedulingResourceIdAndStatus(
                        tenantId,
                        resourceId,
                        EntityStatus.ACTIVE
                );
    }

    @Transactional
    public ResourceUnavailability cancel(
            UUID tenantId,
            UUID unavailabilityId
    ) {

        ResourceUnavailability unavailability =
                get(
                        tenantId,
                        unavailabilityId
                );

        unavailability.setStatus(
                EntityStatus.INACTIVE
        );

        return repository.save(
                unavailability
        );
    }
}
