package africa.growtogether.platform.eiam.recovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gt.eiam.recovery")
public record RecoveryProperties(long passwordResetSeconds, long emailVerificationSeconds) {
    public RecoveryProperties {
        if (passwordResetSeconds <= 0) passwordResetSeconds = 1800;
        if (emailVerificationSeconds <= 0) emailVerificationSeconds = 86400;
    }
}
