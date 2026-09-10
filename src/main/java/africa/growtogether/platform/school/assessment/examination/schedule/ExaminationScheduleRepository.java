package africa.growtogether.platform.school.assessment.examination.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface ExaminationScheduleRepository
        extends JpaRepository<ExaminationSchedule, UUID> {


    Optional<ExaminationSchedule> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


    Optional<ExaminationSchedule> findByTenantIdAndScheduleReference(
            UUID tenantId,
            String scheduleReference
    );


    boolean existsByTenantIdAndScheduleReference(
            UUID tenantId,
            String scheduleReference
    );

}
