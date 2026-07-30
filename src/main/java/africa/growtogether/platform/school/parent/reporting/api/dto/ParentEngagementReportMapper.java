package africa.growtogether.platform.school.parent.reporting.api.dto;


import africa.growtogether.platform.school.parent.reporting.ParentEngagementReport;

import org.springframework.stereotype.Component;


@Component
public class ParentEngagementReportMapper {


    public ParentEngagementReportResponse map(
            ParentEngagementReport report
    ) {

        return new ParentEngagementReportResponse(
                report.getReportType(),
                report.getTotalEvents(),
                report.getDelivered(),
                report.getViewed(),
                report.getAcknowledged()
        );
    }
}
