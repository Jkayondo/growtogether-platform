package africa.growtogether.platform.school.academic.outcome;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class LearningOutcomeService {


    private final LearningOutcomeRepository repository;


    public LearningOutcomeService(
            LearningOutcomeRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public LearningOutcome create(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID classGradeId,
            UUID subjectId,
            String outcomeCode,
            String outcomeTitle,
            Integer sequenceNumber
    ) {


        repository
                .findByTenantIdAndCurriculumVersionIdAndOutcomeCode(
                        tenantId,
                        curriculumVersionId,
                        outcomeCode
                )
                .ifPresent(
                        existing -> {
                            throw new IllegalArgumentException(
                                    "Learning outcome code already exists."
                            );
                        }
                );


        LearningOutcome outcome =
                new LearningOutcome(
                        curriculumVersionId,
                        classGradeId,
                        subjectId,
                        outcomeCode,
                        outcomeTitle,
                        sequenceNumber
                );


        outcome.setTenantId(
                tenantId
        );


        return repository.save(
                outcome
        );
    }


    @Transactional(readOnly = true)
    public List<LearningOutcome> findByCurriculumVersion(
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
    public List<LearningOutcome> findBySubject(
            UUID tenantId,
            UUID curriculumVersionId,
            UUID subjectId
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionIdAndSubjectIdOrderBySequenceNumberAsc(
                        tenantId,
                        curriculumVersionId,
                        subjectId
                );
    }


    @Transactional(readOnly = true)
    public LearningOutcome findByCode(
            UUID tenantId,
            UUID curriculumVersionId,
            String outcomeCode
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionIdAndOutcomeCode(
                        tenantId,
                        curriculumVersionId,
                        outcomeCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Learning outcome not found."
                        )
                );
    }


    @Transactional
    public LearningOutcome archive(
            LearningOutcome outcome
    ) {

        outcome.archive();

        return repository.save(
                outcome
        );
    }

}
