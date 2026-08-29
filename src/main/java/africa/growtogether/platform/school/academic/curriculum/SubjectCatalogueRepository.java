package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SubjectCatalogueRepository
        extends JpaRepository<SubjectCatalogue, UUID> {


    List<SubjectCatalogue> findByTenantIdAndCurriculumVersionId(
            UUID tenantId,
            UUID curriculumVersionId
    );


    List<SubjectCatalogue> findByTenantIdAndLearningAreaId(
            UUID tenantId,
            UUID learningAreaId
    );


    Optional<SubjectCatalogue> findByTenantIdAndCurriculumVersionIdAndSubjectCode(
            UUID tenantId,
            UUID curriculumVersionId,
            String subjectCode
    );


    List<SubjectCatalogue> findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

}
