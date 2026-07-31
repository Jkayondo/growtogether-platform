package africa.growtogether.platform.school.subject;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface SubjectConfigurationRepository
        extends JpaRepository<SubjectConfiguration, UUID> {


    List<SubjectConfiguration> findByTenantId(
            UUID tenantId
    );


    List<SubjectConfiguration> findByAcademicGradeId(
            UUID academicGradeId
    );

}
