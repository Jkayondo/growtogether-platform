package africa.growtogether.platform.school.parent.reporting.api.dto;


import africa.growtogether.platform.school.parent.reporting.ParentEngagementReportType;


public record ParentEngagementReportResponse(

        ParentEngagementReportType reportType,

        long totalEvents,

        long delivered,

        long viewed,

        long acknowledged

) {
}
