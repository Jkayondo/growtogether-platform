package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class SubjectCatalogueService {


    private final SubjectCatalogueRepository repository;


    public SubjectCatalogueService(
            SubjectCatalogueRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public SubjectCatalogue create(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID learningAreaId,
            String subjectCode,
            String subjectName,
            String subjectType,
            String description,
            Integer sequenceNumber
    ) {


        SubjectCatalogue subject =
                new SubjectCatalogue(
                        curriculumVersionId,
                        learningAreaId,
                        subjectCode,
                        subjectName,
                        subjectType,
                        description,
                        sequenceNumber
                );


        subject.setTenantId(
                tenantId
        );


        return repository.save(
                subject
        );
    }


    @Transactional(readOnly = true)
    public List<SubjectCatalogue> findByCurriculumVersion(
            UUID tenantId,
            UUID curriculumVersionId
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionId(
                        tenantId,
                        curriculumVersionId
                );
    }


    @Transactional(readOnly = true)
    public List<SubjectCatalogue> findByLearningArea(
            UUID tenantId,
            UUID learningAreaId
    ) {

        return repository
                .findByTenantIdAndLearningAreaId(
                        tenantId,
                        learningAreaId
                );
    }


    @Transactional(readOnly = true)
    public SubjectCatalogue findByCode(
            UUID tenantId,
            UUID curriculumVersionId,
            String subjectCode
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionIdAndSubjectCode(
                        tenantId,
                        curriculumVersionId,
                        subjectCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Subject catalogue entry not found"
                        )
                );
    }


    @Transactional
    public SubjectCatalogue activate(
            SubjectCatalogue subject
    ) {

        subject.activate();

        return repository.save(
                subject
        );
    }


    @Transactional
    public SubjectCatalogue deactivate(
            SubjectCatalogue subject
    ) {

        subject.deactivate();

        return repository.save(
                subject
        );
    }

}
