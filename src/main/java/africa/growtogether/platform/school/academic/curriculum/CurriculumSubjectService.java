package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.school.academic.subject.SubjectRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


@Service
public class CurriculumSubjectService {


    private static final Set<String> ALLOWED_REQUIREMENTS =
            Set.of(
                    "CORE",
                    "COMPULSORY",
                    "ELECTIVE",
                    "OPTIONAL",
                    "VOCATIONAL",
                    "CO_CURRICULAR"
            );


    private final CurriculumSubjectRepository repository;
    private final ClassGradeRepository classGradeRepository;
    private final SubjectRepository subjectRepository;


    public CurriculumSubjectService(
            CurriculumSubjectRepository repository,
            ClassGradeRepository classGradeRepository,
            SubjectRepository subjectRepository
    ) {
        this.repository = repository;
        this.classGradeRepository = classGradeRepository;
        this.subjectRepository = subjectRepository;
    }


    @Transactional
    public CurriculumSubject create(
            UUID tenantId,
            CurriculumVersion curriculumVersion,
            UUID classGradeId,
            UUID subjectId
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

        subjectRepository
                .findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Subject not found"
                        )
                );


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

        if (requirement == null
                || requirement.isBlank()) {

            throw new IllegalArgumentException(
                    "Subject requirement is required"
            );
        }


        String normalizedRequirement =
                requirement
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        if (!ALLOWED_REQUIREMENTS.contains(
                normalizedRequirement
        )) {

            throw new IllegalArgumentException(
                    "Unsupported subject requirement"
            );
        }


        subject.changeRequirement(
                normalizedRequirement
        );


        return repository.save(
                subject
        );
    }

}
