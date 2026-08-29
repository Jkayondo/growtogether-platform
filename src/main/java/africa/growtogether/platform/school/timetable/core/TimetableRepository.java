package africa.growtogether.platform.school.timetable.core;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimetableRepository
        extends JpaRepository<Timetable, UUID> {

    Optional<Timetable> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    boolean existsByTenantIdAndTimetableCode(
            UUID tenantId,
            String timetableCode
    );

    boolean existsByTenantIdAndAcademicYearIdAndAcademicTermIdAndCampusIdAndTimetableTypeAndVersionNumber(
            UUID tenantId,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            String timetableType,
            Integer versionNumber
    );

    List<Timetable> findByTenantIdAndAcademicYearId(
            UUID tenantId,
            UUID academicYearId
    );

    List<Timetable> findByTenantIdAndCampusId(
            UUID tenantId,
            UUID campusId
    );

    Optional<Timetable>
    findFirstByTenantIdAndAcademicYearIdAndAcademicTermIdAndCampusIdAndTimetableTypeAndTimetableStatusAndStatus(
            UUID tenantId,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            String timetableType,
            String timetableStatus,
            EntityStatus status
    );
}
