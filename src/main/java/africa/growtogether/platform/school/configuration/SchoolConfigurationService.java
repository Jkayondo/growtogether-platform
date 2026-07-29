package africa.growtogether.platform.school.configuration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class SchoolConfigurationService {


    private final SchoolConfigurationRepository repository;


    public SchoolConfigurationService(
            SchoolConfigurationRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public SchoolConfiguration create(
            UUID tenantId,
            String schoolName,
            String countryCode,
            String schoolType
    ) {

        if (repository.existsByTenantId(tenantId)) {
            throw new IllegalStateException(
                    "School configuration already exists."
            );
        }


        SchoolConfiguration configuration =
                new SchoolConfiguration(
                        tenantId,
                        schoolName,
                        countryCode,
                        schoolType
                );


        return repository.save(configuration);
    }


    @Transactional(readOnly = true)
    public SchoolConfiguration getByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "School configuration not found."
                        )
                );
    }
}
