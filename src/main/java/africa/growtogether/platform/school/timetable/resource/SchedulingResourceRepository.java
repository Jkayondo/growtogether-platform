package africa.growtogether.platform.school.timetable.resource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchedulingResourceRepository
        extends JpaRepository<SchedulingResource, UUID> {

    Optional<SchedulingResource> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    boolean existsByTenantIdAndCampusIdAndResourceCode(
            UUID tenantId,
            UUID campusId,
            String resourceCode
    );

    List<SchedulingResource> findByTenantIdAndCampusId(
            UUID tenantId,
            UUID campusId
    );

    List<SchedulingResource> findByTenantIdAndCampusIdAndResourceType(
            UUID tenantId,
            UUID campusId,
            String resourceType
    );

    List<SchedulingResource> findByTenantIdAndResourceStatus(
            UUID tenantId,
            String resourceStatus
    );
}
