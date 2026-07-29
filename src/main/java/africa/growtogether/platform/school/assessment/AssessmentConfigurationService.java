package africa.growtogether.platform.school.assessment;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class AssessmentConfigurationService {


    private final AssessmentConfigurationRepository repository;


    public AssessmentConfigurationService(
            AssessmentConfigurationRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public AssessmentConfiguration create(
            UUID tenantId,
            UUID subjectConfigurationId,
            AssessmentType assessmentType,
            String assessmentName,
            Integer weightPercentage
    ) {


        if (repository
                .existsBySubjectConfigurationIdAndAssessmentName(
                        subjectConfigurationId,
                        assessmentName
                )) {

            throw new IllegalStateException(
                    "Assessment configuration already exists."
            );
        }


        AssessmentConfiguration assessment =
                new AssessmentConfiguration(
                        tenantId,
                        subjectConfigurationId,
                        assessmentType,
                        assessmentName,
                        weightPercentage
                );


        return repository.save(assessment);
    }


    @Transactional(readOnly = true)
    public List<AssessmentConfiguration> getBySubject(
            UUID subjectConfigurationId
    ) {

        return repository
                .findBySubjectConfigurationIdOrderByAssessmentNameAsc(
                        subjectConfigurationId
                );
    }


    @Transactional(readOnly = true)
    public List<AssessmentConfiguration> getByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId);
    }
}
