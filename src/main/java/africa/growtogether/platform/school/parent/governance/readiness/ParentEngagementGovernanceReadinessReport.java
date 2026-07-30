package africa.growtogether.platform.school.parent.governance.readiness;


public class ParentEngagementGovernanceReadinessReport {


    private final ParentEngagementGovernanceReadinessStatus status;

    private final String message;


    public ParentEngagementGovernanceReadinessReport(
            ParentEngagementGovernanceReadinessStatus status,
            String message
    ) {

        this.status = status;
        this.message = message;
    }


    public ParentEngagementGovernanceReadinessStatus getStatus() {

        return status;
    }


    public String getMessage() {

        return message;
    }
}
