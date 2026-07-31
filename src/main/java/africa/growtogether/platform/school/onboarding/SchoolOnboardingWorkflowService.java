package africa.growtogether.platform.school.onboarding;


import africa.growtogether.platform.school.onboarding.dto.SchoolOnboardingWorkflowResponse;
import africa.growtogether.platform.school.onboarding.dto.StartSchoolOnboardingRequest;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class SchoolOnboardingWorkflowService {


    private final SchoolOnboardingWorkflowRepository repository;


    public SchoolOnboardingWorkflowService(
            SchoolOnboardingWorkflowRepository repository
    ) {

        this.repository = repository;
    }


    public SchoolOnboardingWorkflowResponse start(
            UUID tenantId,
            StartSchoolOnboardingRequest request
    ) {


        SchoolOnboardingWorkflow workflow =
                new SchoolOnboardingWorkflow(
                        tenantId,
                        request.schoolConfigurationId()
                );


        SchoolOnboardingWorkflow saved =
                repository.save(workflow);


        return map(saved);
    }


    public SchoolOnboardingWorkflowResponse activate(
            UUID workflowId
    ) {

        SchoolOnboardingWorkflow workflow =
                repository.findById(workflowId)
                        .orElseThrow();


        workflow.updateOnboardingStatus(
                SchoolOnboardingStatus.ACTIVE
        );


        return map(
                repository.save(workflow)
        );
    }


    private SchoolOnboardingWorkflowResponse map(
            SchoolOnboardingWorkflow workflow
    ) {

        return new SchoolOnboardingWorkflowResponse(
                workflow.getId(),
                workflow.getSchoolConfigurationId(),
                workflow.getOnboardingStatus(),
                workflow.getStartedAt(),
                workflow.getCompletedAt()
        );
    }
}
