package africa.growtogether.platform.school.student;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gts_student")
public class Student extends AuditedTenantEntity {

    @Column(name = "admission_application_id")
    private UUID admissionApplicationId;

    @Column(name = "student_number", nullable = false, length = 80)
    private String studentNumber;

    @Column(name = "permanent_learner_number", nullable = false, length = 120)
    private String permanentLearnerNumber;

    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @Column(name = "middle_name", length = 120)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Column(name = "preferred_name", length = 120)
    private String preferredName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(length = 30)
    private String gender;

    @Column(name = "nationality_code", length = 3)
    private String nationalityCode;

    @Column(name = "country_of_birth_code", length = 3)
    private String countryOfBirthCode;

    @Column(name = "primary_language", length = 80)
    private String primaryLanguage;

    @Column(length = 100)
    private String religion;

    @Column(length = 200)
    private String email;

    @Column(name = "phone_number", length = 40)
    private String phoneNumber;

    @Column(name = "physical_address", length = 500)
    private String physicalAddress;

    @Column(name = "eiam_user_id")
    private UUID eiamUserId;

    @Column(name = "eds_student_file_id")
    private UUID edsStudentFileId;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "first_enrollment_date")
    private LocalDate firstEnrollmentDate;

    @Column(name = "expected_completion_date")
    private LocalDate expectedCompletionDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "student_status", nullable = false, length = 30)
    private String studentStatus = "ACTIVE";

    protected Student() {
}

    public Student(
        UUID admissionApplicationId,
        String studentNumber,
        String permanentLearnerNumber,
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
        String email,
        String phoneNumber,
        String physicalAddress,
        UUID eiamUserId,
        UUID edsStudentFileId,
        LocalDate admissionDate,
        LocalDate firstEnrollmentDate,
        LocalDate expectedCompletionDate,
        LocalDate completionDate
) {
    this.admissionApplicationId = admissionApplicationId;
    this.studentNumber = requireText(studentNumber, "studentNumber");
    this.permanentLearnerNumber = requireText(
            permanentLearnerNumber,
            "permanentLearnerNumber"
    );
    this.firstName = requireText(firstName, "firstName");
    this.middleName = middleName;
    this.lastName = requireText(lastName, "lastName");
    this.preferredName = preferredName;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
    this.nationalityCode = nationalityCode;
    this.countryOfBirthCode = countryOfBirthCode;
    this.primaryLanguage = primaryLanguage;
    this.religion = religion;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.physicalAddress = physicalAddress;
    this.eiamUserId = eiamUserId;
    this.edsStudentFileId = edsStudentFileId;
    this.admissionDate = admissionDate;
    this.firstEnrollmentDate = firstEnrollmentDate;
    this.expectedCompletionDate = expectedCompletionDate;
    this.completionDate = completionDate;
}

private String requireText(String value, String field) {
    if (value == null || value.isBlank()) {
        throw new IllegalArgumentException(
                field + " must not be blank"
        );
    }
    return value.trim();
  }
public UUID getAdmissionApplicationId() {
    return admissionApplicationId;
}

public String getStudentNumber() {
    return studentNumber;
}

public String getPermanentLearnerNumber() {
    return permanentLearnerNumber;
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

public String getStudentStatus() {
    return studentStatus;
}

}
