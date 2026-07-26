package africa.growtogether.platform.school.relationship;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gts_student_guardian_relationship")
public class StudentGuardianRelationship extends AuditedTenantEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "guardian_id", nullable = false)
    private UUID guardianId;

    @Column(name = "relationship_type", nullable = false, length = 40)
    private String relationshipType;

    @Column(name = "relationship_description", length = 300)
    private String relationshipDescription;

    @Column(name = "legal_guardian", nullable = false)
    private boolean legalGuardian = false;

    @Column(name = "primary_guardian", nullable = false)
    private boolean primaryGuardian = false;

    @Column(name = "emergency_contact", nullable = false)
    private boolean emergencyContact = false;

    @Column(name = "has_custody", nullable = false)
    private boolean hasCustody = false;

    @Column(name = "custody_type", length = 30)
    private String custodyType;

    @Column(name = "custody_notes", length = 1000)
    private String custodyNotes;

    @Column(name = "lives_with_student", nullable = false)
    private boolean livesWithStudent = false;

    @Column(name = "authorized_to_collect", nullable = false)
    private boolean authorizedToCollect = false;

    @Column(name = "receives_communications", nullable = false)
    private boolean receivesCommunications = true;

    @Column(name = "receives_academic_information", nullable = false)
    private boolean receivesAcademicInformation = true;

    @Column(name = "receives_discipline_information", nullable = false)
    private boolean receivesDisciplineInformation = true;

    @Column(name = "receives_medical_information", nullable = false)
    private boolean receivesMedicalInformation = false;

    @Column(name = "may_approve_school_activities", nullable = false)
    private boolean mayApproveSchoolActivities = false;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom = LocalDate.now();

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "relationship_status", nullable = false, length = 30)
    private String relationshipStatus = "ACTIVE";


    protected StudentGuardianRelationship() {
    }


    public StudentGuardianRelationship(
            UUID studentId,
            UUID guardianId,
            String relationshipType
    ) {
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.relationshipType = requireText(
                relationshipType,
                "relationshipType"
        );
    }


    private String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value.trim();
    }


    public UUID getStudentId() {
        return studentId;
    }

    public UUID getGuardianId() {
        return guardianId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }
}
