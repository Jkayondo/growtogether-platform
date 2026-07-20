package africa.growtogether.platform.eiam.recovery;
import jakarta.validation.constraints.*;
public record PasswordResetConfirm(@NotBlank String token, @NotBlank @Size(min=12,max=200) String newPassword) {}
