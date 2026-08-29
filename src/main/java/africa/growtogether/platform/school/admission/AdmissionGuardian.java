package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_admission_guardian")
public class AdmissionGuardian extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_RELATIONSHIPS =
            Set.of(
                    "FATHER",
                    "MOTHER",
                    "LEGAL_GUARDIAN",
                    "GRANDPARENT",
                    "SIBLING",
                    "RELATIVE",
                    "SPONSOR",
                    "OTHER"
            );

    @Column(
            name = "admission_application_id",
            nullable = false
    )
    private UUID admissionApplicationId;

    @Column(
            name = "relationship_type",
            nullable = false,
            length = 40
    )
    private String relationshipType;

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
            name = "phone_number",
            nullable = false,
            length = 40
    )
    private String phoneNumber;

    @Column(
            name = "alternative_phone_number",
            length = 40
    )
    private String alternativePhoneNumber;

    @Column(
            name = "email",
            length = 200
    )
    private String email;

    @Column(
            name = "occupation",
            length = 160
    )
    private String occupation;

    @Column(
            name = "employer",
            length = 200
    )
    private String employer;

    @Column(
            name = "physical_address",
            length = 500
    )
    private String physicalAddress;

    @Column(
            name = "national_id_number",
            length = 120
    )
    private String nationalIdNumber;

    @Column(name = "existing_eiam_user_id")
    private UUID existingEiamUserId;

    @Column(
            name = "primary_guardian",
            nullable = false
    )
    private boolean primaryGuardian = false;

    @Column(
            name = "emergency_contact",
            nullable = false
    )
    private boolean emergencyContact = false;

    @Column(
            name = "authorized_to_collect",
            nullable = false
    )
    private boolean authorizedToCollect = true;

    @Column(
            name = "receives_communications",
            nullable = false
    )
    private boolean receivesCommunications = true;

    @Column(
            name = "financial_responsibility",
            nullable = false
    )
    private boolean financialResponsibility = false;

    protected AdmissionGuardian() {
    }

    public AdmissionGuardian(
            UUID admissionApplicationId,
            String relationshipType,
            String firstName,
            String middleName,
            String lastName,
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

        if (admissionApplicationId == null) {
            throw new IllegalArgumentException(
                    "admissionApplicationId must not be null"
            );
        }

        this.admissionApplicationId =
                admissionApplicationId;

        this.relationshipType =
                normalizeRelationship(
                        relationshipType
                );

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

        this.phoneNumber =
                requireText(
                        phoneNumber,
                        "phoneNumber"
                );

        this.alternativePhoneNumber =
                trimToNull(
                        alternativePhoneNumber
                );

        this.email =
                trimToNull(
                        email
                );

        this.occupation =
                trimToNull(
                        occupation
                );

        this.employer =
                trimToNull(
                        employer
                );

        this.physicalAddress =
                trimToNull(
                        physicalAddress
                );

        this.nationalIdNumber =
                trimToNull(
                        nationalIdNumber
                );

        this.existingEiamUserId =
                existingEiamUserId;

        this.primaryGuardian =
                Boolean.TRUE.equals(
                        primaryGuardian
                );

        this.emergencyContact =
                Boolean.TRUE.equals(
                        emergencyContact
                );

        this.authorizedToCollect =
                authorizedToCollect == null
                        || authorizedToCollect;

        this.receivesCommunications =
                receivesCommunications == null
                        || receivesCommunications;

        this.financialResponsibility =
                Boolean.TRUE.equals(
                        financialResponsibility
                );
    }

    private String normalizeRelationship(
            String value
    ) {

        String normalized =
                requireText(
                        value,
                        "relationshipType"
                )
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                !ALLOWED_RELATIONSHIPS.contains(
                        normalized
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid relationship type: "
                            + normalized
            );
        }

        return normalized;
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

    public String getRelationshipType() {
        return relationshipType;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAlternativePhoneNumber() {
        return alternativePhoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getEmployer() {
        return employer;
    }

    public String getPhysicalAddress() {
        return physicalAddress;
    }

    public String getNationalIdNumber() {
        return nationalIdNumber;
    }

    public UUID getExistingEiamUserId() {
        return existingEiamUserId;
    }

    public boolean isPrimaryGuardian() {
        return primaryGuardian;
    }

    public boolean isEmergencyContact() {
        return emergencyContact;
    }

    public boolean isAuthorizedToCollect() {
        return authorizedToCollect;
    }

    public boolean isReceivesCommunications() {
        return receivesCommunications;
    }

    public boolean isFinancialResponsibility() {
        return financialResponsibility;
    }
}
