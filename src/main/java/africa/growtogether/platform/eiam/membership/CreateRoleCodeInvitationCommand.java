package africa.growtogether.platform.eiam.membership;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public record CreateRoleCodeInvitationCommand(
        String email,
        String phoneNumber,
        Set<String> roleCodes,
        Instant expiresAt
) {

    public CreateRoleCodeInvitationCommand {

        email =
                normalizeOptional(
                        email
                );

        phoneNumber =
                normalizeOptional(
                        phoneNumber
                );

        boolean hasEmail =
                email != null;

        boolean hasPhone =
                phoneNumber != null;

        if (hasEmail == hasPhone) {
            throw new IllegalArgumentException(
                    "Exactly one invitation contact identity is required."
            );
        }

        if (
                roleCodes == null
                || roleCodes.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "At least one role code is required."
            );
        }

        LinkedHashSet<String> normalizedRoleCodes =
                new LinkedHashSet<>();

        for (String roleCode : roleCodes) {

            if (
                    roleCode == null
                    || roleCode.isBlank()
            ) {
                throw new IllegalArgumentException(
                        "Role codes must not be blank."
                );
            }

            normalizedRoleCodes.add(
                    roleCode.trim()
            );
        }

        roleCodes =
                Set.copyOf(
                        normalizedRoleCodes
                );
    }

    private static String normalizeOptional(
            String value
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}
