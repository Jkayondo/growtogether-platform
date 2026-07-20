package africa.growtogether.platform.eiam.user;

public final class DuplicateUserException extends RuntimeException {
    private final String field;
    public DuplicateUserException(String field, String message) { super(message); this.field = field; }
    public String field() { return field; }
}
