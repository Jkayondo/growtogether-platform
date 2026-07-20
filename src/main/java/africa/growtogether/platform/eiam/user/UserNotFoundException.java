package africa.growtogether.platform.eiam.user;

public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() { super("User account was not found."); }
}
