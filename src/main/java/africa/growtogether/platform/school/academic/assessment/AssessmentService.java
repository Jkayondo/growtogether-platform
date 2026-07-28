package africa.growtogether.platform.school.academic.assessment;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class AssessmentService {


    private final AssessmentRepository repository;


    public AssessmentService(
            AssessmentRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public Assessment create(
            UUID tenantId,
            UUID learningOutcomeId,
            String assessmentCode,
            String assessmentTitle
    ) {


        repository
                .findByTenantIdAndLearningOutcomeIdAndAssessmentCode(
                        tenantId,
                        learningOutcomeId,
                        assessmentCode
                )
                .ifPresent(
                        existing -> {
                            throw new IllegalArgumentException(
                                    "Assessment code already exists."
                            );
                        }
                );


        Assessment assessment =
                new Assessment(
                        learningOutcomeId,
                        assessmentCode,
                        assessmentTitle
                );


        assessment.setTenantId(
                tenantId
        );


        return repository.save(
                assessment
        );
    }


    @Transactional(readOnly = true)
    public List<Assessment> findByLearningOutcome(
            UUID tenantId,
            UUID learningOutcomeId
    ) {

        return repository
                .findByTenantIdAndLearningOutcomeIdOrderByAssessmentDateAsc(
                        tenantId,
                        learningOutcomeId
                );
    }


    @Transactional(readOnly = true)
    public List<Assessment> findByType(
            UUID tenantId,
            String assessmentType
    ) {

        return repository
                .findByTenantIdAndAssessmentTypeOrderByAssessmentDateAsc(
                        tenantId,
                        assessmentType
                );
    }


    @Transactional(readOnly = true)
    public Assessment findByCode(
            UUID tenantId,
            UUID learningOutcomeId,
            String assessmentCode
    ) {

        return repository
                .findByTenantIdAndLearningOutcomeIdAndAssessmentCode(
                        tenantId,
                        learningOutcomeId,
                        assessmentCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Assessment not found."
                        )
                );
    }


    @Transactional
    public Assessment archive(
            Assessment assessment
    ) {

        assessment.archive();

        return repository.save(
                assessment
        );
    }

}
