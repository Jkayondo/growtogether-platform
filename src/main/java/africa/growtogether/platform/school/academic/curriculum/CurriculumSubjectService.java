package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class CurriculumSubjectService {


    private final CurriculumSubjectRepository repository;


    public CurriculumSubjectService(
            CurriculumSubjectRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public CurriculumSubject create(
            UUID tenantId,
            CurriculumVersion curriculumVersion,
            UUID classGradeId,
            UUID subjectId
    ) {


        CurriculumSubject subject =
                new CurriculumSubject(
                        curriculumVersion,
                        classGradeId,
                        subjectId
                );


        subject.setTenantId(
                tenantId
        );


        return repository.save(
                subject
        );
    }


    @Transactional(readOnly = true)
    public List<CurriculumSubject> findByGrade(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID classGradeId
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionIdAndClassGradeId(
                        tenantId,
                        curriculumVersionId,
                        classGradeId
                );
    }


    @Transactional(readOnly = true)
    public CurriculumSubject findMapping(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID classGradeId,
            UUID subjectId
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionIdAndClassGradeIdAndSubjectId(
                        tenantId,
                        curriculumVersionId,
                        classGradeId,
                        subjectId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Curriculum subject mapping not found"
                        )
                );
    }


    @Transactional
    public CurriculumSubject changeRequirement(
            CurriculumSubject subject,
            String requirement
    ) {

        subject.changeRequirement(
                requirement
        );


        return repository.save(
                subject
        );
    }

}
