package africa.growtogether.platform.school.assessment.examination.paper;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface AssessmentPaperRepository
        extends JpaRepository<AssessmentPaper, UUID> {


    Optional<AssessmentPaper> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


    Optional<AssessmentPaper> findByTenantIdAndPaperCode(
            UUID tenantId,
            String paperCode
    );


    boolean existsByTenantIdAndPaperCode(
            UUID tenantId,
            String paperCode
    );


    Optional<AssessmentPaper>
    findByTenantIdAndExaminationSessionIdAndPaperCode(
            UUID tenantId,
            UUID examinationSessionId,
            String paperCode
    );

}
