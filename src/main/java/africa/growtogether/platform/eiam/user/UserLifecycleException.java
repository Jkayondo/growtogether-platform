package africa.growtogether.platform.eiam.user;

public final class UserLifecycleException extends RuntimeException {
    private final UserAccountStatus currentStatus;
    private final UserLifecycleAction action;

    public UserLifecycleException(UserAccountStatus currentStatus, UserLifecycleAction action, String message) {
        super(message);
        this.currentStatus = currentStatus;
        this.action = action;
    }

    public UserAccountStatus currentStatus() {
        return currentStatus;
    }

    public UserLifecycleAction action() {
        return action;
    }
}
