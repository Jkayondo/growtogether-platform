package africa.growtogether.platform.eiam.recovery;
import jakarta.validation.constraints.NotBlank;
public record PasswordResetRequest(@NotBlank String usernameOrEmail) {}
