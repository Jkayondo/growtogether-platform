package africa.growtogether.platform.school.timetable.reliability;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimetableChangeHistoryRepository
        extends JpaRepository<TimetableChangeHistory, UUID> {

    List<TimetableChangeHistory>
    findByTenantIdAndTimetableIdOrderByEffectiveAtDesc(
            UUID tenantId,
            UUID timetableId
    );
}
