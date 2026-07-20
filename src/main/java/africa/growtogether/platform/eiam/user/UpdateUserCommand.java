package africa.growtogether.platform.eiam.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserCommand(
    @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "^[A-Za-z0-9._-]+$") String username,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(max = 200) String displayName
) {}
