package africa.growtogether.platform.school.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface SchoolConfigurationRepository
        extends JpaRepository<SchoolConfiguration, UUID> {


    Optional<SchoolConfiguration> findByTenantId(
            UUID tenantId
    );


    boolean existsByTenantId(
            UUID tenantId
    );
}
