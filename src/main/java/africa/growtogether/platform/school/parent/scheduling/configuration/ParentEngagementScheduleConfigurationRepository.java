package africa.growtogether.platform.school.parent.scheduling.configuration;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentEngagementScheduleConfigurationRepository
        extends JpaRepository<ParentEngagementScheduleConfiguration, UUID> {


    List<ParentEngagementScheduleConfiguration>
    findByTenantId(UUID tenantId);


    List<ParentEngagementScheduleConfiguration>
    findByEnabledTrue();
}
