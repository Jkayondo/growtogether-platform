package africa.growtogether.platform.school.dashboard;

import java.util.List;

public record SchoolDashboardResponse(

        SchoolSnapshot snapshot,

        AttendanceSummary attendance,

        PaymentSummary payment,

        List<NotificationItem> notifications,

        List<ActivityItem> activities

) {


    public record SchoolSnapshot(
            long totalLearners,
            long visitorsToday,
            long totalStaff
    ) {}


    public record AttendanceSummary(
            long present,
            long absent,
            long lateArrivals,
            long boys,
            long girls,
            double attendanceRate
    ) {}


    public record PaymentSummary(
            long amountPaid,
            long amountPending,
            double collectionRate
    ) {}


    public record NotificationItem(
            String title,
            String message,
            String type
    ) {}


    public record ActivityItem(
            String title,
            String description,
            String time
    ) {}

}
