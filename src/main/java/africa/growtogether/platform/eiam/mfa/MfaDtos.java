package africa.growtogether.platform.eiam.mfa;
import jakarta.validation.constraints.*; import java.time.Instant; import java.util.*;
record VerifyEnrollmentCommand(@NotBlank @Pattern(regexp="\\d{6}") String code){}
record CompleteMfaChallengeCommand(@NotBlank String challengeToken,String code,String recoveryCode,boolean trustDevice,String deviceName,String deviceFingerprint){}
record DisableMfaCommand(@NotBlank String code){}
record EnrollmentResponse(String secret,String otpauthUri,List<String> recoveryCodes){}
record TrustedDeviceView(UUID id,String deviceName,Instant expiresAt,Instant lastUsedAt,Instant revokedAt){static TrustedDeviceView from(TrustedDevice d){return new TrustedDeviceView(d.getId(),d.getDeviceName(),d.getExpiresAt(),d.getLastUsedAt(),d.getRevokedAt());}}
