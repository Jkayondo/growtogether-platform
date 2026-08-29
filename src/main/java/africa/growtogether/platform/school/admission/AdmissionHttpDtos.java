package africa.growtogether.platform.school.admission;

import java.time.LocalDate;
import java.util.UUID;

public final class AdmissionHttpDtos {

    private AdmissionHttpDtos() {
    }

    public record ApplicationView(
            UUID id,
            String applicationNumber,
            UUID academicYearId,
            UUID campusId,
            UUID desiredClassGradeId,
            UUID desiredStreamId,
            LocalDate applicationDate,
            String admissionStatus,
            String submissionChannel
    ) {
    }

    public record GuardianView(
            UUID id,
            UUID admissionApplicationId,
            String relationshipType,
            String firstName,
            String middleName,
            String lastName,
            String phoneNumber,
            String email,
            boolean primaryGuardian,
            boolean emergencyContact,
            boolean authorizedToCollect,
            boolean receivesCommunications,
            boolean financialResponsibility
    ) {
    }

    public static ApplicationView application(
            AdmissionApplication source
    ) {

        return new ApplicationView(
                source.getId(),
                source.getApplicationNumber(),
                source.getAcademicYearId(),
                source.getCampusId(),
                source.getDesiredClassGradeId(),
                source.getDesiredStreamId(),
                source.getApplicationDate(),
                source.getAdmissionStatus(),
                source.getSubmissionChannel()
        );
    }

    public static GuardianView guardian(
            AdmissionGuardian source
    ) {

        /*
         * Deliberately exclude national ID, employer and physical address
         * from this creation response. The caller only needs the
         * non-secret identifiers/contact evidence required to continue
         * the admission workflow.
         */
        return new GuardianView(
                source.getId(),
                source.getAdmissionApplicationId(),
                source.getRelationshipType(),
                source.getFirstName(),
                source.getMiddleName(),
                source.getLastName(),
                source.getPhoneNumber(),
                source.getEmail(),
                source.isPrimaryGuardian(),
                source.isEmergencyContact(),
                source.isAuthorizedToCollect(),
                source.isReceivesCommunications(),
                source.isFinancialResponsibility()
        );
    }
}
