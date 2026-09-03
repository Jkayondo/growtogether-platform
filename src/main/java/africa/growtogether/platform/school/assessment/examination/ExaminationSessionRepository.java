package africa.growtogether.platform.school.assessment.examination;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExaminationSessionRepository
        extends JpaRepository<ExaminationSession, UUID> {


    Optional<ExaminationSession> findByTenantIdAndSessionCode(
            UUID tenantId,
            String sessionCode
    );


    boolean existsByTenantIdAndSessionCode(
            UUID tenantId,
            String sessionCode
    );

}
