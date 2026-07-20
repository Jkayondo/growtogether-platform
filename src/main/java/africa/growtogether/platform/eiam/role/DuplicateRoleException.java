package africa.growtogether.platform.eiam.role;
public class DuplicateRoleException extends RuntimeException {
    private final String field;
    public DuplicateRoleException(String field, String message) { super(message); this.field = field; }
    public String field() { return field; }
}
