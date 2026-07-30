package africa.growtogether.platform.school.parent.dashboard;


import africa.growtogether.platform.school.parent.engagement.ParentEngagementEventRepository;
import africa.growtogether.platform.school.parent.engagement.ParentEngagementEventType;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentEngagementDashboardService {


    private final ParentEngagementEventRepository repository;


    public ParentEngagementDashboardService(
            ParentEngagementEventRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementDashboard loadDashboard(
            UUID tenantId
    ) {


        var events =
                repository.findByTenantId(
                        tenantId
                );


        long total =
                events.size();


        long delivered =
                events.stream()
                        .filter(event ->
                                event.getEventType()
                                ==
                                ParentEngagementEventType.NOTIFICATION_DELIVERED
                        )
                        .count();


        long viewed =
                events.stream()
                        .filter(event ->
                                event.getEventType()
                                ==
                                ParentEngagementEventType.NOTIFICATION_VIEWED
                        )
                        .count();


        long acknowledged =
                events.stream()
                        .filter(event ->
                                event.getEventType()
                                ==
                                ParentEngagementEventType.ACKNOWLEDGED
                        )
                        .count();


        return new ParentEngagementDashboard(
                total,
                delivered,
                viewed,
                acknowledged
        );
    }
}
