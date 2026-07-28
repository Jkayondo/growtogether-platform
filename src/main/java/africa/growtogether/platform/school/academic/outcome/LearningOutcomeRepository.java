package africa.growtogether.platform.school.academic.outcome;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface LearningOutcomeRepository
        extends JpaRepository<LearningOutcome, UUID> {


    List<LearningOutcome> findByTenantIdAndCurriculumVersionIdOrderBySequenceNumberAsc(
            UUID tenantId,
            UUID curriculumVersionId
    );


    List<LearningOutcome> findByTenantIdAndCurriculumVersionIdAndSubjectIdOrderBySequenceNumberAsc(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID subjectId
    );


    List<LearningOutcome> findByTenantIdAndClassGradeIdOrderBySequenceNumberAsc(
            UUID tenantId,
            UUID classGradeId
    );


    Optional<LearningOutcome> findByTenantIdAndCurriculumVersionIdAndOutcomeCode(
            UUID tenantId,
            UUID curriculumVersionId,
            String outcomeCode
    );

}
