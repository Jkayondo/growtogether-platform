package africa.growtogether.platform.eiam.auth;
import jakarta.validation.constraints.NotBlank;
public record LogoutCommand(@NotBlank String refreshToken) {}
