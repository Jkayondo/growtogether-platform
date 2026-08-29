package africa.growtogether.platform.school.timetable.bell;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BellScheduleRepository
        extends JpaRepository<BellSchedule, UUID> {

    Optional<BellSchedule> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    boolean existsByTenantIdAndCampusIdAndScheduleCode(
            UUID tenantId,
            UUID campusId,
            String scheduleCode
    );

    List<BellSchedule> findByTenantIdAndCampusId(
            UUID tenantId,
            UUID campusId
    );

    Optional<BellSchedule>
    findFirstByTenantIdAndCampusIdAndScheduleTypeAndScheduleStatusAndStatus(
            UUID tenantId,
            UUID campusId,
            String scheduleType,
            String scheduleStatus,
            EntityStatus status
    );
}
