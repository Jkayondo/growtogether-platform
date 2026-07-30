package africa.growtogether.platform.school.parent.governance.api;


public class ParentEngagementGovernanceDashboardResponse {


    private final long totalCommunications;

    private final long allowedCommunications;

    private final long blockedCommunications;


    public ParentEngagementGovernanceDashboardResponse(
            long totalCommunications,
            long allowedCommunications,
            long blockedCommunications
    ) {

        this.totalCommunications = totalCommunications;
        this.allowedCommunications = allowedCommunications;
        this.blockedCommunications = blockedCommunications;
    }


    public long getTotalCommunications() {

        return totalCommunications;
    }


    public long getAllowedCommunications() {

        return allowedCommunications;
    }


    public long getBlockedCommunications() {

        return blockedCommunications;
    }
}
