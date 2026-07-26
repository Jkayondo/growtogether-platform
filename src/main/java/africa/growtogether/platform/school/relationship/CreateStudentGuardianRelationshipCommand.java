package africa.growtogether.platform.school.relationship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateStudentGuardianRelationshipCommand(

        @NotNull
        UUID studentId,

        @NotNull
        UUID guardianId,

        @NotBlank
        String relationshipType,

        String relationshipDescription,

        boolean legalGuardian,

        boolean primaryGuardian,

        boolean emergencyContact,

        boolean hasCustody,

        String custodyType,

        String custodyNotes,

        boolean livesWithStudent,

        boolean authorizedToCollect,

        boolean receivesCommunications,

        boolean receivesAcademicInformation,

        boolean receivesDisciplineInformation,

        boolean receivesMedicalInformation,

        boolean mayApproveSchoolActivities

) {
}
