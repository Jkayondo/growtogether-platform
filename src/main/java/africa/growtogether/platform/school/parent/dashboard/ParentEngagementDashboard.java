package africa.growtogether.platform.school.parent.dashboard;


public class ParentEngagementDashboard {


    private final long totalNotifications;

    private final long deliveredNotifications;

    private final long viewedNotifications;

    private final long acknowledgedNotifications;


    public ParentEngagementDashboard(
            long totalNotifications,
            long deliveredNotifications,
            long viewedNotifications,
            long acknowledgedNotifications
    ) {

        this.totalNotifications = totalNotifications;
        this.deliveredNotifications = deliveredNotifications;
        this.viewedNotifications = viewedNotifications;
        this.acknowledgedNotifications = acknowledgedNotifications;
    }


    public long getTotalNotifications() {
        return totalNotifications;
    }


    public long getDeliveredNotifications() {
        return deliveredNotifications;
    }


    public long getViewedNotifications() {
        return viewedNotifications;
    }


    public long getAcknowledgedNotifications() {
        return acknowledgedNotifications;
    }
}
