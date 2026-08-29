package africa.growtogether.platform.connect;

/**
 * Canonical authority-source identifiers for GT Connect membership roles.
 *
 * The persistence model remains generic so additional authoritative
 * enterprise/product sources can be introduced without redesigning
 * Connect membership storage.
 */
public final class ConnectRoleAuthoritySources {

    public static final String EIAM_SCHOOL_ADMIN =
            "EIAM_SCHOOL_ADMIN";

    private ConnectRoleAuthoritySources() {
    }
}
