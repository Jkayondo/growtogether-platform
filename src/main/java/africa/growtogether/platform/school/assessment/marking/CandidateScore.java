package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "gts_candidate_score")
public class CandidateScore
        extends AuditedTenantEntity {


    @Column(
            name = "mark_sheet_id",
            nullable = false
    )
    private UUID markSheetId;


    @Column(name = "mark_entry_batch_id")
    private UUID markEntryBatchId;


    @Column(name = "examination_candidate_id")
    private UUID examinationCandidateId;


    @Column(name = "candidate_paper_registration_id")
    private UUID candidatePaperRegistrationId;


    @Column(
            name = "student_id",
            nullable = false
    )
    private UUID studentId;


    @Column(
            name = "student_enrollment_id",
            nullable = false
    )
    private UUID studentEnrollmentId;


    @Column(
            name = "raw_score",
            precision = 10,
            scale = 2
    )
    private BigDecimal rawScore;


    @Column(
            name = "adjusted_score",
            precision = 10,
            scale = 2
    )
    private BigDecimal adjustedScore;


    @Column(
            name = "final_score",
            precision = 10,
            scale = 2
    )
    private BigDecimal finalScore;


    @Column(
            name = "score_status",
            nullable = false,
            length = 30
    )
    private String scoreStatus;


    @Column(
            name = "absent",
            nullable = false
    )
    private boolean absent;


    @Column(
            name = "exempted",
            nullable = false
    )
    private boolean exempted;


    @Column(
            name = "missing_mark",
            nullable = false
    )
    private boolean missingMark;


    @Column(
            name = "withheld",
            nullable = false
    )
    private boolean withheld;


    @Column(
            name = "absence_reason",
            length = 1000
    )
    private String absenceReason;


    @Column(
            name = "withholding_reason",
            length = 1000
    )
    private String withholdingReason;


    @Column(name = "entered_at")
    private Instant enteredAt;


    @Column(name = "entered_by")
    private UUID enteredBy;


    @Column(name = "validated_at")
    private Instant validatedAt;


    @Column(name = "validated_by")
    private UUID validatedBy;


    @Column(name = "approved_at")
    private Instant approvedAt;


    @Column(name = "approved_by")
    private UUID approvedBy;


    @Column(
            name = "source_type",
            nullable = false,
            length = 30
    )
    private String sourceType;


    @Column(
            name = "source_reference",
            length = 255
    )
    private String sourceReference;


    protected CandidateScore() {
    }


    public CandidateScore(
            UUID markSheetId,
            UUID studentId,
            UUID studentEnrollmentId
    ) {

        this(
                markSheetId,
                null,
                null,
                studentId,
                studentEnrollmentId
        );
    }


    public CandidateScore(
            UUID markSheetId,
            UUID examinationCandidateId,
            UUID candidatePaperRegistrationId,
            UUID studentId,
            UUID studentEnrollmentId
    ) {

        this.markSheetId =
                Objects.requireNonNull(
                        markSheetId,
                        "Mark sheet is required"
                );

        this.examinationCandidateId =
                examinationCandidateId;

        this.candidatePaperRegistrationId =
                candidatePaperRegistrationId;

        this.studentId =
                Objects.requireNonNull(
                        studentId,
                        "Student is required"
                );

        this.studentEnrollmentId =
                Objects.requireNonNull(
                        studentEnrollmentId,
                        "Student enrollment is required"
                );

        this.scoreStatus = "DRAFT";

        this.absent = false;
        this.exempted = false;
        this.missingMark = false;
        this.withheld = false;

        this.sourceType = "MANUAL";
    }


    public void enterScore(
            BigDecimal score,
            UUID actorId
    ) {

        requireActor(actorId);

        requireStatus(
                "DRAFT",
                "ENTERED"
        );

        if (
                score == null
                || score.compareTo(BigDecimal.ZERO) < 0
        ) {

            throw new IllegalArgumentException(
                    "Score must be zero or greater"
            );
        }

        this.rawScore = score;
        this.adjustedScore = null;
        this.finalScore = score;

        this.absent = false;
        this.exempted = false;
        this.missingMark = false;
        this.withheld = false;

        this.absenceReason = null;
        this.withholdingReason = null;

        this.enteredAt = Instant.now();
        this.enteredBy = actorId;

        this.scoreStatus = "ENTERED";
    }


    public void markAbsent(
            UUID actorId
    ) {

        requireActor(actorId);

        requireStatus(
                "DRAFT",
                "ENTERED"
        );

        this.rawScore = null;
        this.adjustedScore = null;
        this.finalScore = null;

        this.absent = true;
        this.exempted = false;
        this.missingMark = false;
        this.withheld = false;

        this.enteredAt = Instant.now();
        this.enteredBy = actorId;

        /*
         * Absence is a special-state flag, not a Candidate Score
         * lifecycle state.  The database lifecycle contract does not
         * contain ABSENT; an entered absence therefore remains ENTERED.
         */
        this.scoreStatus = "ENTERED";
    }


    public void validate(
            UUID actorId
    ) {

        requireActor(actorId);

        requireStatus(
                "ENTERED"
        );

        this.validatedAt = Instant.now();
        this.validatedBy = actorId;

        this.scoreStatus = "VALIDATED";
    }


    public void approve(
            UUID actorId
    ) {

        requireActor(actorId);

        requireStatus(
                "VALIDATED",
                "ADJUSTED",
                "MODERATION"
        );

        this.approvedAt = Instant.now();
        this.approvedBy = actorId;

        this.scoreStatus = "APPROVED";
    }


    private void requireStatus(
            String... allowed
    ) {

        for (String candidate : allowed) {

            if (
                    candidate.equals(
                            this.scoreStatus
                    )
            ) {

                return;
            }
        }

        throw new IllegalStateException(
                "Candidate score lifecycle does not allow operation from "
                        + this.scoreStatus
        );
    }


    private void requireActor(
            UUID actorId
    ) {

        if (actorId == null) {

            throw new IllegalArgumentException(
                    "Actor is required"
            );
        }
    }


    public UUID getMarkSheetId() {
        return markSheetId;
    }


    public UUID getMarkEntryBatchId() {
        return markEntryBatchId;
    }


    public UUID getExaminationCandidateId() {
        return examinationCandidateId;
    }


    public UUID getCandidatePaperRegistrationId() {
        return candidatePaperRegistrationId;
    }


    public UUID getStudentId() {
        return studentId;
    }


    public UUID getStudentEnrollmentId() {
        return studentEnrollmentId;
    }


    public BigDecimal getRawScore() {
        return rawScore;
    }


    public BigDecimal getAdjustedScore() {
        return adjustedScore;
    }


    public BigDecimal getFinalScore() {
        return finalScore;
    }


    public String getScoreStatus() {
        return scoreStatus;
    }


    public boolean isAbsent() {
        return absent;
    }


    public boolean isExempted() {
        return exempted;
    }


    public boolean isMissingMark() {
        return missingMark;
    }


    public boolean isWithheld() {
        return withheld;
    }


    public String getAbsenceReason() {
        return absenceReason;
    }


    public String getWithholdingReason() {
        return withholdingReason;
    }


    public Instant getEnteredAt() {
        return enteredAt;
    }


    public UUID getEnteredBy() {
        return enteredBy;
    }


    public Instant getValidatedAt() {
        return validatedAt;
    }


    public UUID getValidatedBy() {
        return validatedBy;
    }


    public Instant getApprovedAt() {
        return approvedAt;
    }


    public UUID getApprovedBy() {
        return approvedBy;
    }


    public String getSourceType() {
        return sourceType;
    }


    public String getSourceReference() {
        return sourceReference;
    }
}
