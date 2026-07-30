package africa.growtogether.platform.school.parent.compliance;


public class ParentEngagementComplianceSummary {


    private final long totalCommunications;

    private final long allowedCommunications;

    private final long blockedCommunications;

    private final long consentGranted;

    private final long consentRevoked;


    public ParentEngagementComplianceSummary(
            long totalCommunications,
            long allowedCommunications,
            long blockedCommunications,
            long consentGranted,
            long consentRevoked
    ) {

        this.totalCommunications = totalCommunications;
        this.allowedCommunications = allowedCommunications;
        this.blockedCommunications = blockedCommunications;
        this.consentGranted = consentGranted;
        this.consentRevoked = consentRevoked;
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


    public long getConsentGranted() {

        return consentGranted;
    }


    public long getConsentRevoked() {

        return consentRevoked;
    }
}
