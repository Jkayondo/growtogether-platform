package africa.growtogether.platform.school.assessment;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface AssessmentConfigurationRepository
        extends JpaRepository<AssessmentConfiguration, UUID> {


    List<AssessmentConfiguration>
    findBySubjectConfigurationIdOrderByAssessmentNameAsc(
            UUID subjectConfigurationId
    );


    boolean existsBySubjectConfigurationIdAndAssessmentName(
            UUID subjectConfigurationId,
            String assessmentName
    );


    List<AssessmentConfiguration>
    findByTenantId(UUID tenantId);
}
