package africa.growtogether.platform.ewf;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ewf_workforce_member")
public class WorkforceMember extends AuditedTenantEntity {

    @Column(
            name = "workforce_number",
            nullable = false,
            length = 80
    )
    private String workforceNumber;

    @Column(
            name = "employee_number",
            length = 80
    )
    private String employeeNumber;

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

    @Column(name = "date_of_birth")
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
            name = "primary_phone_number",
            length = 40
    )
    private String primaryPhoneNumber;

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
            name = "physical_address",
            length = 500
    )
    private String physicalAddress;

    @Column(
            name = "postal_address",
            length = 500
    )
    private String postalAddress;

    @Column(name = "eiam_user_id")
    private UUID eiamUserId;

    @Column(name = "eds_personnel_file_id")
    private UUID edsPersonnelFileId;

    @Column(
            name = "workforce_category",
            nullable = false,
            length = 40
    )
    private String workforceCategory = "EMPLOYEE";

    @Column(
            name = "workforce_status",
            nullable = false,
            length = 30
    )
    private String workforceStatus = "ACTIVE";

    protected WorkforceMember() {
    }

    public WorkforceMember(
            String workforceNumber,
            String employeeNumber,
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
            UUID eiamUserId,
            UUID edsPersonnelFileId,
            String workforceCategory
    ) {
        this.workforceNumber = workforceNumber;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.preferredName = preferredName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationalityCode = nationalityCode;
        this.nationalIdNumber = nationalIdNumber;
        this.passportNumber = passportNumber;
        this.primaryPhoneNumber = primaryPhoneNumber;
        this.alternativePhoneNumber = alternativePhoneNumber;
        this.email = email;
        this.physicalAddress = physicalAddress;
        this.postalAddress = postalAddress;
        this.eiamUserId = eiamUserId;
        this.edsPersonnelFileId = edsPersonnelFileId;

        if (workforceCategory != null && !workforceCategory.isBlank()) {
            this.workforceCategory = workforceCategory;
        }
    }

    public String getWorkforceNumber() {
        return workforceNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
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

    public String getNationalIdNumber() {
        return nationalIdNumber;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public String getAlternativePhoneNumber() {
        return alternativePhoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhysicalAddress() {
        return physicalAddress;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public UUID getEiamUserId() {
        return eiamUserId;
    }

    public UUID getEdsPersonnelFileId() {
        return edsPersonnelFileId;
    }

    public String getWorkforceCategory() {
        return workforceCategory;
    }

    public String getWorkforceStatus() {
        return workforceStatus;
    }
}
