package africa.growtogether.platform.school.parent.scheduling.configuration;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ParentEngagementScheduleConfigurationService {


    private final ParentEngagementScheduleConfigurationRepository repository;


    public ParentEngagementScheduleConfigurationService(
            ParentEngagementScheduleConfigurationRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementScheduleConfiguration create(
            ParentEngagementScheduleConfiguration configuration
    ) {

        return repository.save(configuration);
    }


    public List<ParentEngagementScheduleConfiguration> findByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId);
    }


    public void enable(
            ParentEngagementScheduleConfiguration configuration
    ) {

        configuration.enable();

        repository.save(configuration);
    }


    public void disable(
            ParentEngagementScheduleConfiguration configuration
    ) {

        configuration.disable();

        repository.save(configuration);
    }
}
