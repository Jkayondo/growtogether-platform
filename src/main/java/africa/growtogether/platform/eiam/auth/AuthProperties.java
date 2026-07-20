package africa.growtogether.platform.eiam.auth;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="gt.security.auth")
public record AuthProperties(long refreshTokenSeconds, int maxFailedAttempts, long lockSeconds) {}
