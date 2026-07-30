package africa.growtogether.platform.school.parent.reporting;


import africa.growtogether.platform.school.parent.engagement.ParentEngagementEventRepository;
import africa.growtogether.platform.school.parent.engagement.ParentEngagementEventType;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentEngagementReportService {


    private final ParentEngagementEventRepository repository;


    public ParentEngagementReportService(
            ParentEngagementEventRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementReport generate(
            UUID tenantId,
            ParentEngagementReportType reportType
    ) {


        var events =
                repository.findByTenantId(tenantId);


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


        return new ParentEngagementReport(
                reportType,
                events.size(),
                delivered,
                viewed,
                acknowledged
        );
    }
}
