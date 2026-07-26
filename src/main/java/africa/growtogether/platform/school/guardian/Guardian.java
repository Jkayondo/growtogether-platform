package africa.growtogether.platform.school.guardian;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gts_guardian")
public class Guardian extends AuditedTenantEntity {

    @Column(name = "guardian_number", nullable = false, length = 80)
    private String guardianNumber;

    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @Column(name = "middle_name", length = 120)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Column(name = "preferred_name", length = 120)
    private String preferredName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 30)
    private String gender;

    @Column(name = "nationality_code", length = 3)
    private String nationalityCode;

    @Column(name = "national_id_number", length = 120)
    private String nationalIdNumber;

    @Column(name = "passport_number", length = 120)
    private String passportNumber;

    @Column(name = "primary_phone_number", nullable = false, length = 40)
    private String primaryPhoneNumber;

    @Column(name = "alternative_phone_number", length = 40)
    private String alternativePhoneNumber;

    @Column(length = 200)
    private String email;

    @Column(name = "physical_address", length = 500)
    private String physicalAddress;

    @Column(name = "postal_address", length = 500)
    private String postalAddress;

    @Column(length = 160)
    private String occupation;

    @Column(length = 200)
    private String employer;

    @Column(name = "eiam_user_id")
    private UUID eiamUserId;

    @Column(name = "source_admission_guardian_id")
    private UUID sourceAdmissionGuardianId;

    @Column(name = "preferred_language", length = 80)
    private String preferredLanguage;

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "UNVERIFIED";

    @Column(name = "guardian_status", nullable = false, length = 30)
    private String guardianStatus = "ACTIVE";


    protected Guardian() {
    }


    public Guardian(
            String guardianNumber,
            String firstName,
            String middleName,
            String lastName,
            String preferredName,
            LocalDate dateOfBirth,
            String gender,
            String nationalityCode,
            String nationalIdNumber,
            String passportNumber,
            String primaryPhoneNumber,
            String alternativePhoneNumber,
            String email,
            String physicalAddress,
            String postalAddress,
            String occupation,
            String employer,
            UUID eiamUserId,
            UUID sourceAdmissionGuardianId,
            String preferredLanguage
    ) {
        this.guardianNumber = requireText(guardianNumber, "guardianNumber");
        this.firstName = requireText(firstName, "firstName");
        this.middleName = middleName;
        this.lastName = requireText(lastName, "lastName");
        this.preferredName = preferredName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationalityCode = nationalityCode;
        this.nationalIdNumber = nationalIdNumber;
        this.passportNumber = passportNumber;
        this.primaryPhoneNumber =
                requireText(primaryPhoneNumber, "primaryPhoneNumber");
        this.alternativePhoneNumber = alternativePhoneNumber;
        this.email = email;
        this.physicalAddress = physicalAddress;
        this.postalAddress = postalAddress;
        this.occupation = occupation;
        this.employer = employer;
        this.eiamUserId = eiamUserId;
        this.sourceAdmissionGuardianId = sourceAdmissionGuardianId;
        this.preferredLanguage = preferredLanguage;
    }


    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value.trim();
    }


    public String getGuardianNumber() {
        return guardianNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public String getGuardianStatus() {
        return guardianStatus;
    }
}
