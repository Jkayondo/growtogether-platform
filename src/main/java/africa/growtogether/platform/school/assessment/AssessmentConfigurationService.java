package africa.growtogether.platform.school.assessment;

import africa.growtogether.platform.school.subject.SubjectConfigurationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class AssessmentConfigurationService {


    private final AssessmentConfigurationRepository repository;
    private final SubjectConfigurationRepository subjectConfigurations;


    public AssessmentConfigurationService(
            AssessmentConfigurationRepository repository,
            SubjectConfigurationRepository subjectConfigurations
    ) {
        this.repository = repository;
        this.subjectConfigurations = subjectConfigurations;
    }


    @Transactional
    public AssessmentConfiguration create(
            UUID tenantId,
            UUID subjectConfigurationId,
            AssessmentType assessmentType,
            String assessmentName,
            Integer weightPercentage
    ) {


        subjectConfigurations
                .findByIdAndTenantId(
                        subjectConfigurationId,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Subject configuration not found."
                        )
                );


        if (repository
                .existsByTenantIdAndSubjectConfigurationIdAndAssessmentName(
                        tenantId,
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
            UUID tenantId,
            UUID subjectConfigurationId
    ) {

        return repository
                .findByTenantIdAndSubjectConfigurationIdOrderByAssessmentNameAsc(
                        tenantId,
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
