package africa.growtogether.platform.school.parent.reporting;


public class ParentEngagementReport {


    private final ParentEngagementReportType reportType;

    private final long totalEvents;

    private final long delivered;

    private final long viewed;

    private final long acknowledged;


    public ParentEngagementReport(
            ParentEngagementReportType reportType,
            long totalEvents,
            long delivered,
            long viewed,
            long acknowledged
    ) {

        this.reportType = reportType;
        this.totalEvents = totalEvents;
        this.delivered = delivered;
        this.viewed = viewed;
        this.acknowledged = acknowledged;
    }


    public ParentEngagementReportType getReportType() {
        return reportType;
    }


    public long getTotalEvents() {
        return totalEvents;
    }


    public long getDelivered() {
        return delivered;
    }


    public long getViewed() {
        return viewed;
    }


    public long getAcknowledged() {
        return acknowledged;
    }
}
