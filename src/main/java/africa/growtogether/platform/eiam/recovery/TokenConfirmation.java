package africa.growtogether.platform.eiam.recovery;
import jakarta.validation.constraints.NotBlank;
public record TokenConfirmation(@NotBlank String token) {}
