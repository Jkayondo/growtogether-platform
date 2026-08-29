package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeacherUnavailabilityRepository
        extends JpaRepository<TeacherUnavailability, UUID> {

    Optional<TeacherUnavailability> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    List<TeacherUnavailability>
    findByTenantIdAndTeacherProfileIdAndStatus(
            UUID tenantId,
            UUID teacherProfileId,
            EntityStatus status
    );

    List<TeacherUnavailability>
    findByTenantIdAndTeacherProfileIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
            UUID tenantId,
            UUID teacherProfileId,
            Instant requestedTo,
            Instant requestedFrom,
            EntityStatus status
    );
}
