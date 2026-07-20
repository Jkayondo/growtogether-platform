package africa.growtogether.platform.common.persistence;

public final class TenantScopeViolationException extends RuntimeException {
    public TenantScopeViolationException(String message) {
        super(message);
    }
}
