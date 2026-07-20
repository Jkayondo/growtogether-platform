package africa.growtogether.platform.eiam.auth;
import jakarta.validation.constraints.NotBlank;
public record MfaLoginCommand(@NotBlank String challengeToken,String code,String recoveryCode,boolean trustDevice,String deviceName,String deviceFingerprint){}
