package africa.growtogether.platform.school.timetable.entry;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimetableEntryRepository
        extends JpaRepository<TimetableEntry, UUID> {

    Optional<TimetableEntry> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    List<TimetableEntry> findByTenantIdAndTimetableId(
            UUID tenantId,
            UUID timetableId
    );

    List<TimetableEntry>
    findByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndClassGradeIdAndEntryStatusInAndStatus(
            UUID tenantId,
            UUID timetableId,
            String dayOfWeek,
            UUID bellPeriodId,
            UUID classGradeId,
            Collection<String> entryStatuses,
            EntityStatus status
    );

    Optional<TimetableEntry>
    findFirstByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndTeacherProfileIdAndEntryStatusInAndStatus(
            UUID tenantId,
            UUID timetableId,
            String dayOfWeek,
            UUID bellPeriodId,
            UUID teacherProfileId,
            Collection<String> entryStatuses,
            EntityStatus status
    );

    Optional<TimetableEntry>
    findFirstByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndSchedulingResourceIdAndEntryStatusInAndStatus(
            UUID tenantId,
            UUID timetableId,
            String dayOfWeek,
            UUID bellPeriodId,
            UUID schedulingResourceId,
            Collection<String> entryStatuses,
            EntityStatus status
    );
}
