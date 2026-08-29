package africa.growtogether.platform.eip;

public record ExternalProviderDispatchResult(

        Status status,

        String providerRequestId,

        String providerReference,

        String providerCode,

        String providerMessage

) {

    public ExternalProviderDispatchResult {

        if (status == null) {
            throw new IllegalArgumentException(
                    "status must not be null"
            );
        }
    }

    public boolean successful() {
        return status == Status.SUBMITTED
                || status == Status.ACCEPTED;
    }

    public enum Status {
        SUBMITTED,
        ACCEPTED,
        FAILED,
        TIMED_OUT
    }
}
