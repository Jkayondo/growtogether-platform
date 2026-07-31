package africa.growtogether.platform.school.onboarding;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface SchoolOnboardingWorkflowRepository
        extends JpaRepository<SchoolOnboardingWorkflow, UUID> {


    Optional<SchoolOnboardingWorkflow> findBySchoolConfigurationId(
            UUID schoolConfigurationId
    );


    Optional<SchoolOnboardingWorkflow> findByTenantId(
            UUID tenantId
    );

}
