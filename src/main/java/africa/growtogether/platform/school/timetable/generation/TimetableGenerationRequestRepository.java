package africa.growtogether.platform.school.timetable.generation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimetableGenerationRequestRepository
        extends JpaRepository<
                TimetableGenerationRequest,
                UUID
        > {

    Optional<TimetableGenerationRequest>
    findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<TimetableGenerationRequest>
    findByTenantIdAndGenerationCode(
            UUID tenantId,
            String generationCode
    );

    boolean existsByTenantIdAndGenerationCode(
            UUID tenantId,
            String generationCode
    );

    List<TimetableGenerationRequest>
    findByTenantIdAndAcademicYearIdAndCampusId(
            UUID tenantId,
            UUID academicYearId,
            UUID campusId
    );
}
