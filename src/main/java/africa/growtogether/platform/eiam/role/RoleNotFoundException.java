package africa.growtogether.platform.eiam.role;
public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException() { super("Role was not found."); }
}
