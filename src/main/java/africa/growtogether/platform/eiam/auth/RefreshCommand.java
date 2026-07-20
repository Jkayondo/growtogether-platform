package africa.growtogether.platform.eiam.auth;
import jakarta.validation.constraints.NotBlank;
public record RefreshCommand(@NotBlank String refreshToken) {}
