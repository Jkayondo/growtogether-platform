package africa.growtogether.platform.school.admission;

import java.time.Instant;
import java.util.UUID;

public record AdmissionGuardianAccountProvisioningResult(
        UUID provisioningId,
        UUID admissionGuardianId,
        UUID invitationId,
        AdmissionGuardianContactIdentityType contactIdentityType,
        String contactIdentity,
        Instant expiresAt,
        String acceptanceToken
) {

    @Override
    public String toString() {

        return "AdmissionGuardianAccountProvisioningResult["
                + "provisioningId="
                + provisioningId
                + ", admissionGuardianId="
                + admissionGuardianId
                + ", invitationId="
                + invitationId
                + ", contactIdentityType="
                + contactIdentityType
                + ", contactIdentity="
                + contactIdentity
                + ", expiresAt="
                + expiresAt
                + ", acceptanceToken=<redacted>]";
    }
}
