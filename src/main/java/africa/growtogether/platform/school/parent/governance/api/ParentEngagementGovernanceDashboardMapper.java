package africa.growtogether.platform.school.parent.governance.api;


import africa.growtogether.platform.school.parent.compliance.ParentEngagementComplianceSummary;

import org.springframework.stereotype.Component;


@Component
public class ParentEngagementGovernanceDashboardMapper {


    public ParentEngagementGovernanceDashboardResponse map(
            ParentEngagementComplianceSummary summary
    ) {

        return new ParentEngagementGovernanceDashboardResponse(
                summary.getTotalCommunications(),
                summary.getAllowedCommunications(),
                summary.getBlockedCommunications()
        );
    }
}
