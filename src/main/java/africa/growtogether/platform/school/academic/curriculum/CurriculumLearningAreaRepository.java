package africa.growtogether.platform.school.academic.curriculum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CurriculumLearningAreaRepository
        extends JpaRepository<CurriculumLearningArea, UUID> {


    List<CurriculumLearningArea> findByTenantIdAndCurriculumVersionIdOrderBySequenceNumberAsc(
            UUID tenantId,
            UUID curriculumVersionId
    );


    Optional<CurriculumLearningArea> findByTenantIdAndCurriculumVersionIdAndLearningAreaCode(
            UUID tenantId,
            UUID curriculumVersionId,
            String learningAreaCode
    );


    List<CurriculumLearningArea> findByTenantIdAndCurriculumVersionIdAndStatus(
            UUID tenantId,
            UUID curriculumVersionId,
            String status
    );

}
