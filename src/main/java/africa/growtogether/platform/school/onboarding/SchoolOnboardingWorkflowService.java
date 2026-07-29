package africa.growtogether.platform.school.onboarding;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class SchoolOnboardingWorkflowService {


    private final SchoolOnboardingWorkflowRepository repository;


    public SchoolOnboardingWorkflowService(
            SchoolOnboardingWorkflowRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public SchoolOnboardingWorkflow create(
            UUID tenantId,
            UUID schoolConfigurationId
    ) {

        if (repository.existsByTenantId(tenantId)) {
            throw new IllegalStateException(
                    "School onboarding workflow already exists."
            );
        }


        SchoolOnboardingWorkflow workflow =
                new SchoolOnboardingWorkflow(
                        tenantId,
                        schoolConfigurationId
                );


        return repository.save(workflow);
    }


    @Transactional(readOnly = true)
    public SchoolOnboardingWorkflow getByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "School onboarding workflow not found."
                        )
                );
    }


    @Transactional
    public SchoolOnboardingWorkflow advance(
            SchoolOnboardingWorkflow workflow,
            SchoolOnboardingStatus nextStatus
    ) {

        workflow.updateOnboardingStatus(nextStatus);

        return repository.save(workflow);
    }
}
