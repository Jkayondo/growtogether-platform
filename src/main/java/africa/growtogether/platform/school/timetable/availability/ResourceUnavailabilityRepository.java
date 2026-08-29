package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceUnavailabilityRepository
        extends JpaRepository<ResourceUnavailability, UUID> {

    Optional<ResourceUnavailability> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    List<ResourceUnavailability>
    findByTenantIdAndSchedulingResourceIdAndStatus(
            UUID tenantId,
            UUID schedulingResourceId,
            EntityStatus status
    );

    List<ResourceUnavailability>
    findByTenantIdAndSchedulingResourceIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
            UUID tenantId,
            UUID schedulingResourceId,
            Instant requestedTo,
            Instant requestedFrom,
            EntityStatus status
    );
}
