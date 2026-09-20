package africa.growtogether.platform.school.guardian;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_guardian")
public class Guardian extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_GENDERS =
            Set.of(
                    "FEMALE",
                    "MALE",
                    "OTHER",
                    "NOT_DECLARED"
            );

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

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verified_by")
    private UUID verifiedBy;

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

        this.guardianNumber =
                requireText(
                        guardianNumber,
                        "guardianNumber"
                );

        this.firstName =
                requireText(
                        firstName,
                        "firstName"
                );

        this.middleName = middleName;

        this.lastName =
                requireText(
                        lastName,
                        "lastName"
                );

        this.preferredName = preferredName;

        if (
                dateOfBirth != null
                && dateOfBirth.isAfter(LocalDate.now())
        ) {
            throw new IllegalArgumentException(
                    "dateOfBirth must not be in the future"
            );
        }

        this.dateOfBirth = dateOfBirth;

        if (
                gender != null
                && !ALLOWED_GENDERS.contains(gender)
        ) {
            throw new IllegalArgumentException(
                    "Invalid gender: " + gender
            );
        }

        this.gender = gender;
        this.nationalityCode = nationalityCode;
        this.nationalIdNumber = nationalIdNumber;
        this.passportNumber = passportNumber;

        this.primaryPhoneNumber =
                requireText(
                        primaryPhoneNumber,
                        "primaryPhoneNumber"
                );

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

    public void linkEiamUser(
            UUID userId
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "eiamUserId must not be null"
            );
        }

        if (
                eiamUserId != null
                && !eiamUserId.equals(userId)
        ) {
            throw new IllegalStateException(
                    "Guardian is already linked to a different EIAM user"
            );
        }

        eiamUserId = userId;
    }

    public boolean hasEiamUserLink() {
        return eiamUserId != null;
    }

    public void markVerificationPending() {
        this.verificationStatus = "PENDING";
        this.verifiedAt = null;
        this.verifiedBy = null;
    }

    public void verify(
            UUID verifiedBy
    ) {

        if (verifiedBy == null) {
            throw new IllegalArgumentException(
                    "verifiedBy must not be null"
            );
        }

        this.verificationStatus = "VERIFIED";
        this.verifiedAt = Instant.now();
        this.verifiedBy = verifiedBy;
    }

    public void rejectVerification() {
        this.verificationStatus = "REJECTED";
        this.verifiedAt = null;
        this.verifiedBy = null;
    }

    public void expireVerification() {
        this.verificationStatus = "EXPIRED";
        this.verifiedAt = null;
        this.verifiedBy = null;
    }

    public void activate() {
        this.guardianStatus = "ACTIVE";
    }

    public void deactivate() {
        this.guardianStatus = "INACTIVE";
    }

    public void restrict() {
        this.guardianStatus = "RESTRICTED";
    }

    public void markDeceased() {
        this.guardianStatus = "DECEASED";
    }

    public void archive() {
        this.guardianStatus = "ARCHIVED";
    }

    private String requireText(
            String value,
            String field
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
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

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public UUID getSourceAdmissionGuardianId() {
        return sourceAdmissionGuardianId;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public UUID getVerifiedBy() {
        return verifiedBy;
    }

    public String getGuardianStatus() {
        return guardianStatus;
    }

    public UUID getEiamUserId() {
        return eiamUserId;
    }

}
