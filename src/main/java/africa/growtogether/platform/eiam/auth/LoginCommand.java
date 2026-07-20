package africa.growtogether.platform.eiam.auth;
import jakarta.validation.constraints.NotBlank;
public record LoginCommand(@NotBlank String usernameOrEmail,@NotBlank String password,String trustedDeviceToken,String deviceFingerprint){public LoginCommand(String usernameOrEmail,String password){this(usernameOrEmail,password,null,null);}}
