package africa.growtogether.platform.eiam.recovery;
import java.time.Instant;
public record RecoveryDispatch(String token, Instant expiresAt) {}
