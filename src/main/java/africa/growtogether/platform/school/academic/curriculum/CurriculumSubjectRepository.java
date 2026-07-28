package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface CurriculumSubjectRepository
        extends JpaRepository<CurriculumSubject, UUID> {


    List<CurriculumSubject> findByTenantIdAndCurriculumVersionIdAndClassGradeId(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID classGradeId
    );


    Optional<CurriculumSubject> findByTenantIdAndCurriculumVersionIdAndClassGradeIdAndSubjectId(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID classGradeId,
            UUID subjectId
    );


    List<CurriculumSubject> findByTenantIdAndSubjectId(
            UUID tenantId,
            UUID subjectId
    );

}
