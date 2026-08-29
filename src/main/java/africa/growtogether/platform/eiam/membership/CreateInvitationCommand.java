package africa.growtogether.platform.eiam.membership;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateInvitationCommand(
    @Email @Size(max = 255) String email,
    @Pattern(regexp = "^\\+[1-9][0-9]{5,14}$") String phoneNumber,
    @NotEmpty Set<UUID> roleIds,
    @Future Instant expiresAt
) {
    public CreateInvitationCommand(
            String email,
            Set<UUID> roleIds,
            Instant expiresAt
    ) {
        this(
                email,
                null,
                roleIds,
                expiresAt
        );
    }

    public CreateInvitationCommand {
        email = normalizeOptional(email);
        phoneNumber = normalizeOptional(phoneNumber);

        boolean hasEmail = email != null;
        boolean hasPhone = phoneNumber != null;

        if (hasEmail == hasPhone) {
            throw new IllegalArgumentException(
                    "Exactly one invitation contact identity is required."
            );
        }
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
