package africa.growtogether.platform.school.assessment.examination.registration;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface CandidatePaperRegistrationRepository
        extends JpaRepository<CandidatePaperRegistration, UUID> {


    Optional<CandidatePaperRegistration> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


    Optional<CandidatePaperRegistration>
    findByTenantIdAndExaminationCandidateIdAndAssessmentPaperId(
            UUID tenantId,
            UUID examinationCandidateId,
            UUID assessmentPaperId
    );


    boolean existsByTenantIdAndExaminationCandidateIdAndAssessmentPaperId(
            UUID tenantId,
            UUID examinationCandidateId,
            UUID assessmentPaperId
    );
}
