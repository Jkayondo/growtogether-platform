package africa.growtogether.platform.school.parent.dashboard.api.dto;


public record ParentEngagementDashboardResponse(

        long totalNotifications,

        long deliveredNotifications,

        long viewedNotifications,

        long acknowledgedNotifications

) {
}
