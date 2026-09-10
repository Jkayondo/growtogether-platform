package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class CurriculumClassGradeService {


    private final CurriculumClassGradeRepository repository;
    private final ClassGradeRepository classGradeRepository;


    public CurriculumClassGradeService(
            CurriculumClassGradeRepository repository,
            ClassGradeRepository classGradeRepository
    ) {
        this.repository = repository;
        this.classGradeRepository = classGradeRepository;
    }


    @Transactional
    public CurriculumClassGrade create(
            UUID tenantId,
            CurriculumVersion curriculumVersion,
            UUID classGradeId,
            Integer sequenceNumber
    ) {

        if (!tenantId.equals(curriculumVersion.getTenantId())) {
            throw new IllegalArgumentException(
                    "Curriculum version does not belong to tenant"
            );
        }

        classGradeRepository
                .findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Class grade not found"
                        )
                );

        CurriculumClassGrade mapping =
                new CurriculumClassGrade(
                        curriculumVersion,
                        classGradeId,
                        sequenceNumber
                );


        mapping.setTenantId(
                tenantId
        );


        return repository.save(
                mapping
        );
    }


    @Transactional(readOnly = true)
    public List<CurriculumClassGrade> findByCurriculumVersion(
            UUID tenantId,
            UUID curriculumVersionId
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionIdOrderBySequenceNumberAsc(
                        tenantId,
                        curriculumVersionId
                );
    }


    @Transactional(readOnly = true)
    public CurriculumClassGrade findMapping(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID classGradeId
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionIdAndClassGradeId(
                        tenantId,
                        curriculumVersionId,
                        classGradeId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Curriculum grade mapping not found"
                        )
                );
    }


    @Transactional
    public CurriculumClassGrade archive(
            CurriculumClassGrade mapping
    ) {

        mapping.archive();


        return repository.save(
                mapping
        );
    }

}
