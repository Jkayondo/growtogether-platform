package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface CurriculumClassGradeRepository
        extends JpaRepository<CurriculumClassGrade, UUID> {


    List<CurriculumClassGrade> findByTenantIdAndCurriculumVersionIdOrderBySequenceNumberAsc(
            UUID tenantId,
            UUID curriculumVersionId
    );


    Optional<CurriculumClassGrade> findByTenantIdAndCurriculumVersionIdAndClassGradeId(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID classGradeId
    );


    List<CurriculumClassGrade> findByTenantIdAndClassGradeId(
            UUID tenantId,
            UUID classGradeId
    );

}
