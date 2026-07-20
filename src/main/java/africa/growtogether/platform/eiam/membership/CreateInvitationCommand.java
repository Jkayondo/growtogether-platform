package africa.growtogether.platform.eiam.membership;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateInvitationCommand(
    @Email @NotBlank String email,
    @NotEmpty Set<UUID> roleIds,
    @Future Instant expiresAt
) {}
