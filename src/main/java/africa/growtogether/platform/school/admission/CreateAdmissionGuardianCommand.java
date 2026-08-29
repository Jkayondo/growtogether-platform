package africa.growtogether.platform.school.admission;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateAdmissionGuardianCommand(

        @NotBlank
        String relationshipType,

        @NotBlank
        String firstName,

        String middleName,

        @NotBlank
        String lastName,

        @NotBlank
        String phoneNumber,

        String alternativePhoneNumber,

        String email,

        String occupation,

        String employer,

        String physicalAddress,

        String nationalIdNumber,

        UUID existingEiamUserId,

        Boolean primaryGuardian,

        Boolean emergencyContact,

        Boolean authorizedToCollect,

        Boolean receivesCommunications,

        Boolean financialResponsibility

) {
}
