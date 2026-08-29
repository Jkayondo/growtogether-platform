package africa.growtogether.platform.school.timetable.reliability;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimetableConflictRepository
        extends JpaRepository<TimetableConflict, UUID> {

    Optional<TimetableConflict> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    List<TimetableConflict> findByTenantIdAndTimetableIdAndResolvedFalse(
            UUID tenantId,
            UUID timetableId
    );

    List<TimetableConflict> findByTenantIdAndTimetableId(
            UUID tenantId,
            UUID timetableId
    );
}
