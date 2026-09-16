package africa.growtogether.platform.eiam.bootstrap;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FirstAdminBootstrapCommand(
        @NotBlank @Email String administratorEmail,
        @NotBlank String administratorUsername,
        @NotBlank @Size(min = 12, max = 128) String administratorPassword,
        @NotBlank String administratorDisplayName
) {}
