package africa.growtogether.platform.school.parent.engagement;


import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentEngagementAnalyticsService {


    private final ParentEngagementEventRepository repository;


    public ParentEngagementAnalyticsService(
            ParentEngagementEventRepository repository
    ) {

        this.repository = repository;
    }


    public long countEvents(
            UUID tenantId
    ) {

        return repository
                .findByTenantId(tenantId)
                .size();
    }


    public long countParentEvents(
            UUID parentId
    ) {

        return repository
                .findByParentId(parentId)
                .size();
    }
}
