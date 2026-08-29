package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gts_teacher_subject_qualification")
public class TeacherSubjectQualification extends AuditedTenantEntity {

    @Column(
            name = "teacher_profile_id",
            nullable = false
    )
    private UUID teacherProfileId;

    @Column(
            name = "subject_id",
            nullable = false
    )
    private UUID subjectId;

    @Column(
            name = "competency_level",
            nullable = false,
            length = 30
    )
    private String competencyLevel = "QUALIFIED";

    @Column(
            name = "primary_subject",
            nullable = false
    )
    private boolean primarySubject;

    @Column(name = "minimum_class_grade_id")
    private UUID minimumClassGradeId;

    @Column(name = "maximum_class_grade_id")
    private UUID maximumClassGradeId;

    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom = LocalDate.now();

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(
            name = "verification_status",
            nullable = false,
            length = 30
    )
    private String verificationStatus = "PENDING";

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "eds_evidence_document_id")
    private UUID edsEvidenceDocumentId;

    protected TeacherSubjectQualification() {
    }

    public TeacherSubjectQualification(
            UUID teacherProfileId,
            UUID subjectId,
            String competencyLevel,
            boolean primarySubject,
            UUID minimumClassGradeId,
            UUID maximumClassGradeId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            UUID edsEvidenceDocumentId
    ) {
        this.teacherProfileId = teacherProfileId;
        this.subjectId = subjectId;

        if (competencyLevel != null && !competencyLevel.isBlank()) {
            this.competencyLevel = competencyLevel;
        }

        this.primarySubject = primarySubject;
        this.minimumClassGradeId = minimumClassGradeId;
        this.maximumClassGradeId = maximumClassGradeId;

        if (effectiveFrom != null) {
            this.effectiveFrom = effectiveFrom;
        }

        this.effectiveTo = effectiveTo;
        this.edsEvidenceDocumentId = edsEvidenceDocumentId;
    }

    public UUID getTeacherProfileId() {
        return teacherProfileId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public String getCompetencyLevel() {
        return competencyLevel;
    }

    public boolean isPrimarySubject() {
        return primarySubject;
    }

    public UUID getMinimumClassGradeId() {
        return minimumClassGradeId;
    }

    public UUID getMaximumClassGradeId() {
        return maximumClassGradeId;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
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

    public UUID getEdsEvidenceDocumentId() {
        return edsEvidenceDocumentId;
    }

    public void verify(
            UUID verifiedBy
    ) {
        this.verificationStatus = "VERIFIED";
        this.verifiedBy = verifiedBy;
        this.verifiedAt = Instant.now();
    }

    public void reject() {
        this.verificationStatus = "REJECTED";
        this.verifiedBy = null;
        this.verifiedAt = null;
    }
}
