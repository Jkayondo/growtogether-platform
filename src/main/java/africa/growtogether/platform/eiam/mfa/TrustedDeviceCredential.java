package africa.growtogether.platform.eiam.mfa; import java.time.Instant; public record TrustedDeviceCredential(String token,Instant expiresAt){}
