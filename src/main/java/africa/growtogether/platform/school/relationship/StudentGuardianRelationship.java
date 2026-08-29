package africa.growtogether.platform.school.relationship;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_student_guardian_relationship")
public class StudentGuardianRelationship
        extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_RELATIONSHIP_TYPES =
            Set.of(
                    "FATHER",
                    "MOTHER",
                    "LEGAL_GUARDIAN",
                    "STEP_PARENT",
                    "GRANDPARENT",
                    "SIBLING",
                    "RELATIVE",
                    "FOSTER_PARENT",
                    "SPONSOR",
                    "OTHER"
            );

    private static final Set<String> ALLOWED_CUSTODY_TYPES =
            Set.of(
                    "SOLE",
                    "JOINT",
                    "TEMPORARY",
                    "COURT_APPOINTED",
                    "CUSTOMARY",
                    "OTHER"
            );

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

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }

        if (guardianId == null) {
            throw new IllegalArgumentException(
                    "guardianId must not be null"
            );
        }

        String normalizedRelationshipType =
                requireText(
                        relationshipType,
                        "relationshipType"
                );

        if (
                !ALLOWED_RELATIONSHIP_TYPES.contains(
                        normalizedRelationshipType
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid relationship type: "
                            + normalizedRelationshipType
            );
        }

        if (
                custodyType != null
                && !custodyType.isBlank()
                && !ALLOWED_CUSTODY_TYPES.contains(
                        custodyType.trim()
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid custody type: "
                            + custodyType.trim()
            );
        }

        this.studentId = studentId;
        this.guardianId = guardianId;
        this.relationshipType = normalizedRelationshipType;
        this.relationshipDescription = relationshipDescription;
        this.legalGuardian = legalGuardian;
        this.primaryGuardian = primaryGuardian;
        this.emergencyContact = emergencyContact;
        this.hasCustody = hasCustody;

        this.custodyType =
                custodyType == null
                        || custodyType.isBlank()
                        ? null
                        : custodyType.trim();

        this.custodyNotes = custodyNotes;
        this.livesWithStudent = livesWithStudent;
        this.authorizedToCollect = authorizedToCollect;
        this.receivesCommunications = receivesCommunications;
        this.receivesAcademicInformation = receivesAcademicInformation;
        this.receivesDisciplineInformation = receivesDisciplineInformation;
        this.receivesMedicalInformation = receivesMedicalInformation;
        this.mayApproveSchoolActivities = mayApproveSchoolActivities;
    }

    public void suspend() {
        this.relationshipStatus = "SUSPENDED";
    }

    public void restrict() {
        this.relationshipStatus = "RESTRICTED";
    }

    public void end(
            LocalDate effectiveTo
    ) {

        if (
                effectiveTo != null
                && effectiveTo.isBefore(this.effectiveFrom)
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        this.effectiveTo = effectiveTo;
        this.relationshipStatus = "ENDED";
    }

    public void reactivate() {
        this.effectiveTo = null;
        this.relationshipStatus = "ACTIVE";
    }

    public void archive() {
        this.relationshipStatus = "ARCHIVED";
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

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getGuardianId() {
        return guardianId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public String getRelationshipDescription() {
        return relationshipDescription;
    }

    public boolean isLegalGuardian() {
        return legalGuardian;
    }

    public boolean isPrimaryGuardian() {
        return primaryGuardian;
    }

    public boolean isEmergencyContact() {
        return emergencyContact;
    }

    public boolean isHasCustody() {
        return hasCustody;
    }

    public String getCustodyType() {
        return custodyType;
    }

    public String getCustodyNotes() {
        return custodyNotes;
    }

    public boolean isLivesWithStudent() {
        return livesWithStudent;
    }

    public boolean isAuthorizedToCollect() {
        return authorizedToCollect;
    }

    public boolean isReceivesCommunications() {
        return receivesCommunications;
    }

    public boolean isReceivesAcademicInformation() {
        return receivesAcademicInformation;
    }

    public boolean isReceivesDisciplineInformation() {
        return receivesDisciplineInformation;
    }

    public boolean isReceivesMedicalInformation() {
        return receivesMedicalInformation;
    }

    public boolean isMayApproveSchoolActivities() {
        return mayApproveSchoolActivities;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }
}
