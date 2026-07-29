package africa.growtogether.platform.school.curriculum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface CurriculumConfigurationRepository
        extends JpaRepository<CurriculumConfiguration, UUID> {


    Optional<CurriculumConfiguration>
    findByTenantId(UUID tenantId);


    Optional<CurriculumConfiguration>
    findBySchoolConfigurationId(UUID schoolConfigurationId);


    boolean existsBySchoolConfigurationId(
            UUID schoolConfigurationId
    );
}
