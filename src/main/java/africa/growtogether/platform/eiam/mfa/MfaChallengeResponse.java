package africa.growtogether.platform.eiam.mfa; import java.time.Instant; public record MfaChallengeResponse(boolean mfaRequired,String challengeToken,Instant expiresAt){}
