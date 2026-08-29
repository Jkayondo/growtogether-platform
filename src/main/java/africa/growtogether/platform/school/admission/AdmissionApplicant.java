package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_admission_applicant")
public class AdmissionApplicant extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_GENDERS =
            Set.of(
                    "FEMALE",
                    "MALE",
                    "OTHER",
                    "NOT_DECLARED"
            );

    @Column(
            name = "admission_application_id",
            nullable = false
    )
    private UUID admissionApplicationId;

    @Column(
            name = "first_name",
            nullable = false,
            length = 120
    )
    private String firstName;

    @Column(
            name = "middle_name",
            length = 120
    )
    private String middleName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 120
    )
    private String lastName;

    @Column(
            name = "preferred_name",
            length = 120
    )
    private String preferredName;

    @Column(
            name = "date_of_birth",
            nullable = false
    )
    private LocalDate dateOfBirth;

    @Column(
            name = "gender",
            length = 30
    )
    private String gender;

    @Column(
            name = "nationality_code",
            length = 3
    )
    private String nationalityCode;

    @Column(
            name = "country_of_birth_code",
            length = 3
    )
    private String countryOfBirthCode;

    @Column(
            name = "primary_language",
            length = 80
    )
    private String primaryLanguage;

    @Column(
            name = "religion",
            length = 100
    )
    private String religion;

    @Column(
            name = "national_id_number",
            length = 120
    )
    private String nationalIdNumber;

    @Column(
            name = "passport_number",
            length = 120
    )
    private String passportNumber;

    @Column(
            name = "birth_certificate_number",
            length = 120
    )
    private String birthCertificateNumber;

    @Column(
            name = "email",
            length = 200
    )
    private String email;

    @Column(
            name = "phone_number",
            length = 40
    )
    private String phoneNumber;

    @Column(
            name = "physical_address",
            length = 500
    )
    private String physicalAddress;

    @Column(name = "existing_eiam_user_id")
    private UUID existingEiamUserId;

    @Column(name = "existing_learner_reference")
    private UUID existingLearnerReference;

    protected AdmissionApplicant() {
    }

    public AdmissionApplicant(
            UUID admissionApplicationId,
            String firstName,
            String middleName,
            String lastName,
            String preferredName,
            LocalDate dateOfBirth,
            String gender,
            String nationalityCode,
            String countryOfBirthCode,
            String primaryLanguage,
            String religion,
            String nationalIdNumber,
            String passportNumber,
            String birthCertificateNumber,
            String email,
            String phoneNumber,
            String physicalAddress,
            UUID existingEiamUserId,
            UUID existingLearnerReference
    ) {

        if (admissionApplicationId == null) {
            throw new IllegalArgumentException(
                    "admissionApplicationId must not be null"
            );
        }

        if (dateOfBirth == null) {
            throw new IllegalArgumentException(
                    "dateOfBirth must not be null"
            );
        }

        this.admissionApplicationId =
                admissionApplicationId;

        this.firstName =
                requireText(
                        firstName,
                        "firstName"
                );

        this.middleName =
                trimToNull(
                        middleName
                );

        this.lastName =
                requireText(
                        lastName,
                        "lastName"
                );

        this.preferredName =
                trimToNull(
                        preferredName
                );

        this.dateOfBirth =
                dateOfBirth;

        this.gender =
                normalizeGender(
                        gender
                );

        this.nationalityCode =
                normalizeCode(
                        nationalityCode
                );

        this.countryOfBirthCode =
                normalizeCode(
                        countryOfBirthCode
                );

        this.primaryLanguage =
                trimToNull(
                        primaryLanguage
                );

        this.religion =
                trimToNull(
                        religion
                );

        this.nationalIdNumber =
                trimToNull(
                        nationalIdNumber
                );

        this.passportNumber =
                trimToNull(
                        passportNumber
                );

        this.birthCertificateNumber =
                trimToNull(
                        birthCertificateNumber
                );

        this.email =
                trimToNull(
                        email
                );

        this.phoneNumber =
                trimToNull(
                        phoneNumber
                );

        this.physicalAddress =
                trimToNull(
                        physicalAddress
                );

        this.existingEiamUserId =
                existingEiamUserId;

        this.existingLearnerReference =
                existingLearnerReference;
    }

    private String normalizeGender(
            String value
    ) {

        String normalized =
                trimToNull(
                        value
                );

        if (normalized == null) {
            return null;
        }

        normalized =
                normalized.toUpperCase(
                        Locale.ROOT
                );

        if (
                !ALLOWED_GENDERS.contains(
                        normalized
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid gender: "
                            + normalized
            );
        }

        return normalized;
    }

    private String normalizeCode(
            String value
    ) {

        String normalized =
                trimToNull(
                        value
                );

        if (normalized == null) {
            return null;
        }

        return normalized.toUpperCase(
                Locale.ROOT
        );
    }

    private String requireText(
            String value,
            String field
    ) {

        String normalized =
                trimToNull(
                        value
                );

        if (normalized == null) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return normalized;
    }

    private String trimToNull(
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

    public UUID getAdmissionApplicationId() {
        return admissionApplicationId;
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

    public String getNationalityCode() {
        return nationalityCode;
    }

    public String getCountryOfBirthCode() {
        return countryOfBirthCode;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public String getReligion() {
        return religion;
    }

    public String getNationalIdNumber() {
        return nationalIdNumber;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public String getBirthCertificateNumber() {
        return birthCertificateNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhysicalAddress() {
        return physicalAddress;
    }

    public UUID getExistingEiamUserId() {
        return existingEiamUserId;
    }

    public UUID getExistingLearnerReference() {
        return existingLearnerReference;
    }
}
