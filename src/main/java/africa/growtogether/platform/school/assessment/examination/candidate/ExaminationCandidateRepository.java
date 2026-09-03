package africa.growtogether.platform.school.assessment.examination.candidate;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface ExaminationCandidateRepository
        extends JpaRepository<ExaminationCandidate, UUID> {


    Optional<ExaminationCandidate> findByTenantIdAndCandidateNumber(
            UUID tenantId,
            String candidateNumber
    );


    boolean existsByTenantIdAndCandidateNumber(
            UUID tenantId,
            String candidateNumber
    );


    Optional<ExaminationCandidate> findByTenantIdAndExaminationSessionIdAndStudentId(
            UUID tenantId,
            UUID examinationSessionId,
            UUID studentId
    );

}
