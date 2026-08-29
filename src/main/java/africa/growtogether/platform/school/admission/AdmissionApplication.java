package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_admission_application")
public class AdmissionApplication extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_SUBMISSION_CHANNELS =
            Set.of(
                    "ONLINE",
                    "OFFICE",
                    "MOBILE",
                    "IMPORT",
                    "AGENT"
            );

    @Column(
            name = "application_number",
            nullable = false,
            length = 80
    )
    private String applicationNumber;

    @Column(
            name = "academic_year_id",
            nullable = false
    )
    private UUID academicYearId;

    @Column(
            name = "campus_id",
            nullable = false
    )
    private UUID campusId;

    @Column(
            name = "desired_class_grade_id",
            nullable = false
    )
    private UUID desiredClassGradeId;

    @Column(name = "desired_stream_id")
    private UUID desiredStreamId;

    @Column(
            name = "application_date",
            nullable = false
    )
    private LocalDate applicationDate;

    @Column(
            name = "admission_status",
            nullable = false,
            length = 30
    )
    private String admissionStatus = "DRAFT";

    @Column(
            name = "submission_channel",
            nullable = false,
            length = 30
    )
    private String submissionChannel = "ONLINE";

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "submitted_by")
    private UUID submittedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "decision_at")
    private Instant decisionAt;

    @Column(name = "decision_by")
    private UUID decisionBy;

    @Column(
            name = "decision_notes",
            length = 2000
    )
    private String decisionNotes;

    @Column(name = "offered_class_grade_id")
    private UUID offeredClassGradeId;

    @Column(name = "offered_stream_id")
    private UUID offeredStreamId;

    @Column(name = "offer_expiry_date")
    private LocalDate offerExpiryDate;

    @Column(name = "admitted_at")
    private Instant admittedAt;

    protected AdmissionApplication() {
    }

    public AdmissionApplication(
            String applicationNumber,
            UUID academicYearId,
            UUID campusId,
            UUID desiredClassGradeId,
            UUID desiredStreamId,
            LocalDate applicationDate,
            String submissionChannel
    ) {

        this.applicationNumber =
                requireText(
                        applicationNumber,
                        "applicationNumber"
                );

        if (academicYearId == null) {
            throw new IllegalArgumentException(
                    "academicYearId must not be null"
            );
        }

        if (campusId == null) {
            throw new IllegalArgumentException(
                    "campusId must not be null"
            );
        }

        if (desiredClassGradeId == null) {
            throw new IllegalArgumentException(
                    "desiredClassGradeId must not be null"
            );
        }

        if (applicationDate == null) {
            throw new IllegalArgumentException(
                    "applicationDate must not be null"
            );
        }

        this.academicYearId = academicYearId;
        this.campusId = campusId;
        this.desiredClassGradeId = desiredClassGradeId;
        this.desiredStreamId = desiredStreamId;
        this.applicationDate = applicationDate;

        String normalizedChannel =
                submissionChannel == null
                        || submissionChannel.isBlank()
                        ? "ONLINE"
                        : submissionChannel
                                .trim()
                                .toUpperCase();

        if (
                !ALLOWED_SUBMISSION_CHANNELS.contains(
                        normalizedChannel
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid submission channel: "
                            + normalizedChannel
            );
        }

        this.submissionChannel = normalizedChannel;
        this.admissionStatus = "DRAFT";
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

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public UUID getCampusId() {
        return campusId;
    }

    public UUID getDesiredClassGradeId() {
        return desiredClassGradeId;
    }

    public UUID getDesiredStreamId() {
        return desiredStreamId;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public String getAdmissionStatus() {
        return admissionStatus;
    }

    public String getSubmissionChannel() {
        return submissionChannel;
    }

    public UUID getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public UUID getSubmittedBy() {
        return submittedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public Instant getDecisionAt() {
        return decisionAt;
    }

    public UUID getDecisionBy() {
        return decisionBy;
    }

    public String getDecisionNotes() {
        return decisionNotes;
    }

    public UUID getOfferedClassGradeId() {
        return offeredClassGradeId;
    }

    public UUID getOfferedStreamId() {
        return offeredStreamId;
    }

    public LocalDate getOfferExpiryDate() {
        return offerExpiryDate;
    }

    public Instant getAdmittedAt() {
        return admittedAt;
    }
}
