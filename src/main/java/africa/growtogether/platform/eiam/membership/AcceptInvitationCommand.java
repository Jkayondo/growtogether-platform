package africa.growtogether.platform.eiam.membership;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationCommand(
    @NotBlank String token,
    @NotBlank @Size(min = 3, max = 100) String username,
    @NotBlank @Size(min = 2, max = 200) String displayName,
    @NotBlank @Size(min = 12, max = 128) String password
) {}
