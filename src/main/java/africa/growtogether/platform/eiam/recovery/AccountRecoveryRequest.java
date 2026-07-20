package africa.growtogether.platform.eiam.recovery;
import jakarta.validation.constraints.NotBlank;
public record AccountRecoveryRequest(@NotBlank String usernameOrEmail) {}
