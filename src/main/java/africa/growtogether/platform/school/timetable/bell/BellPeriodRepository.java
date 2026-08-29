package africa.growtogether.platform.school.timetable.bell;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BellPeriodRepository
        extends JpaRepository<BellPeriod, UUID> {

    Optional<BellPeriod> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    boolean existsByTenantIdAndBellScheduleIdAndPeriodCode(
            UUID tenantId,
            UUID bellScheduleId,
            String periodCode
    );

    boolean existsByTenantIdAndBellScheduleIdAndSequenceNumber(
            UUID tenantId,
            UUID bellScheduleId,
            Integer sequenceNumber
    );

    List<BellPeriod> findByTenantIdAndBellScheduleIdAndStatusOrderBySequenceNumber(
            UUID tenantId,
            UUID bellScheduleId,
            EntityStatus status
    );
}
