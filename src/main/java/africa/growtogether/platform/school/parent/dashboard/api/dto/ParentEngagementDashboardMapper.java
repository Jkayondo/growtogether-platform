package africa.growtogether.platform.school.parent.dashboard.api.dto;


import africa.growtogether.platform.school.parent.dashboard.ParentEngagementDashboard;

import org.springframework.stereotype.Component;


@Component
public class ParentEngagementDashboardMapper {


    public ParentEngagementDashboardResponse map(
            ParentEngagementDashboard dashboard
    ) {

        return new ParentEngagementDashboardResponse(
                dashboard.getTotalNotifications(),
                dashboard.getDeliveredNotifications(),
                dashboard.getViewedNotifications(),
                dashboard.getAcknowledgedNotifications()
        );
    }
}
