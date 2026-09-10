package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "gts_mark_sheet")
public class MarkSheet extends AuditedTenantEntity {


    @Column(
            name = "mark_sheet_reference",
            nullable = false,
            length = 100
    )
    private String markSheetReference;


    @Column(
            name = "assessment_component_id",
            nullable = false
    )
    private UUID assessmentComponentId;


    @Column(name = "assessment_paper_id")
    private UUID assessmentPaperId;


    @Column(name = "examination_schedule_id")
    private UUID examinationScheduleId;


    @Column(
            name = "subject_offering_id",
            nullable = false
    )
    private UUID subjectOfferingId;


    @Column(
            name = "class_offering_id",
            nullable = false
    )
    private UUID classOfferingId;


    @Column(name = "stream_id")
    private UUID streamId;


    @Column(name = "teacher_profile_id")
    private UUID teacherProfileId;


    @Column(
            name = "maximum_score",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal maximumScore;


    @Column(
            name = "pass_score",
            precision = 10,
            scale = 2
    )
    private BigDecimal passScore;


    @Column(
            name = "expected_candidate_count",
            nullable = false
    )
    private int expectedCandidateCount;


    @Column(
            name = "recorded_candidate_count",
            nullable = false
    )
    private int recordedCandidateCount;


    @Column(name = "entry_opened_at")
    private Instant entryOpenedAt;


    @Column(name = "entry_opened_by")
    private UUID entryOpenedBy;


    @Column(name = "submitted_at")
    private Instant submittedAt;


    @Column(name = "submitted_by")
    private UUID submittedBy;


    @Column(name = "locked_at")
    private Instant lockedAt;


    @Column(name = "locked_by")
    private UUID lockedBy;


    @Column(
            name = "lock_reason",
            length = 1000
    )
    private String lockReason;


    @Column(
            name = "mark_sheet_status",
            nullable = false,
            length = 30
    )
    private String markSheetStatus;


    protected MarkSheet() {
    }


    public MarkSheet(
            String markSheetReference,
            UUID assessmentComponentId,
            UUID subjectOfferingId,
            UUID classOfferingId,
            BigDecimal maximumScore
    ) {

        this(
                markSheetReference,
                assessmentComponentId,
                null,
                null,
                subjectOfferingId,
                classOfferingId,
                null,
                null,
                maximumScore,
                null
        );
    }


    public MarkSheet(
            String markSheetReference,
            UUID assessmentComponentId,
            UUID assessmentPaperId,
            UUID examinationScheduleId,
            UUID subjectOfferingId,
            UUID classOfferingId,
            UUID streamId,
            UUID teacherProfileId,
            BigDecimal maximumScore,
            BigDecimal passScore
    ) {

        this.markSheetReference =
                requireReference(
                        markSheetReference
                );

        this.assessmentComponentId =
                Objects.requireNonNull(
                        assessmentComponentId,
                        "Assessment component is required"
                );

        this.assessmentPaperId =
                assessmentPaperId;

        this.examinationScheduleId =
                examinationScheduleId;

        this.subjectOfferingId =
                Objects.requireNonNull(
                        subjectOfferingId,
                        "Subject offering is required"
                );

        this.classOfferingId =
                Objects.requireNonNull(
                        classOfferingId,
                        "Class offering is required"
                );

        this.streamId =
                streamId;

        this.teacherProfileId =
                teacherProfileId;

        validateScores(
                maximumScore,
                passScore
        );

        this.maximumScore =
                maximumScore;

        this.passScore =
                passScore;

        this.expectedCandidateCount = 0;
        this.recordedCandidateCount = 0;

        this.markSheetStatus =
                "DRAFT";
    }


    public void open(
            UUID actorId
    ) {

        requireActor(
                actorId
        );

        requireStatus(
                "DRAFT",
                "RETURNED"
        );

        this.entryOpenedAt =
                Instant.now();

        this.entryOpenedBy =
                actorId;

        this.markSheetStatus =
                "OPEN";
    }


    public void submit(
            UUID actorId
    ) {

        requireActor(
                actorId
        );

        requireStatus(
                "OPEN"
        );

        this.submittedAt =
                Instant.now();

        this.submittedBy =
                actorId;

        this.markSheetStatus =
                "SUBMITTED";
    }


    public void startModeration() {

        requireStatus(
                "SUBMITTED",
                "VALIDATED"
        );

        requireSubmittedEvidence();

        this.markSheetStatus =
                "MODERATION";
    }


    public void approve() {

        requireStatus(
                "MODERATION"
        );

        requireSubmittedEvidence();

        this.markSheetStatus =
                "APPROVED";
    }


    public void returnForCorrection() {

        requireStatus(
                "SUBMITTED",
                "VALIDATED",
                "MODERATION"
        );

        requireSubmittedEvidence();

        this.markSheetStatus =
                "RETURNED";
    }


    public void lock(
            UUID actorId,
            String reason
    ) {

        requireActor(
                actorId
        );

        requireStatus(
                "APPROVED"
        );

        requireSubmittedEvidence();

        String normalizedReason =
                reason == null
                        ? null
                        : reason.trim();

        if (
                normalizedReason == null
                || normalizedReason.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Lock reason is required"
            );
        }

        if (
                normalizedReason.length() > 1000
        ) {

            throw new IllegalArgumentException(
                    "Lock reason must not exceed 1000 characters"
            );
        }

        this.lockedAt =
                Instant.now();

        this.lockedBy =
                actorId;

        this.lockReason =
                normalizedReason;

        this.markSheetStatus =
                "LOCKED";
    }


    private void requireSubmittedEvidence() {

        if (
                submittedAt == null
        ) {

            throw new IllegalStateException(
                    "Submitted mark sheet evidence is required"
            );
        }
    }


    private void requireStatus(
            String... allowed
    ) {

        for (
                String value : allowed
        ) {

            if (
                    Objects.equals(
                            markSheetStatus,
                            value
                    )
            ) {

                return;
            }
        }

        throw new IllegalStateException(
                "Invalid mark sheet lifecycle transition from "
                        + markSheetStatus
        );
    }


    private static void requireActor(
            UUID actorId
    ) {

        Objects.requireNonNull(
                actorId,
                "Actor is required"
        );
    }


    private static String requireReference(
            String reference
    ) {

        if (
                reference == null
                || reference.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Mark sheet reference is required"
            );
        }

        String normalized =
                reference.trim();

        if (
                normalized.length() > 100
        ) {

            throw new IllegalArgumentException(
                    "Mark sheet reference must not exceed 100 characters"
            );
        }

        return normalized;
    }


    private static void validateScores(
            BigDecimal maximumScore,
            BigDecimal passScore
    ) {

        if (
                maximumScore == null
                || maximumScore.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {

            throw new IllegalArgumentException(
                    "Maximum score must be greater than zero"
            );
        }

        if (
                passScore != null
                && (
                    passScore.compareTo(
                            BigDecimal.ZERO
                    ) < 0
                    || passScore.compareTo(
                            maximumScore
                    ) > 0
                )
        ) {

            throw new IllegalArgumentException(
                    "Pass score must be between zero and maximum score"
            );
        }
    }


    public String getMarkSheetReference() {
        return markSheetReference;
    }


    public UUID getAssessmentComponentId() {
        return assessmentComponentId;
    }


    public UUID getAssessmentPaperId() {
        return assessmentPaperId;
    }


    public UUID getExaminationScheduleId() {
        return examinationScheduleId;
    }


    public UUID getSubjectOfferingId() {
        return subjectOfferingId;
    }


    public UUID getClassOfferingId() {
        return classOfferingId;
    }


    public UUID getStreamId() {
        return streamId;
    }


    public UUID getTeacherProfileId() {
        return teacherProfileId;
    }


    public BigDecimal getMaximumScore() {
        return maximumScore;
    }


    public BigDecimal getPassScore() {
        return passScore;
    }


    public int getExpectedCandidateCount() {
        return expectedCandidateCount;
    }


    public int getRecordedCandidateCount() {
        return recordedCandidateCount;
    }


    public Instant getEntryOpenedAt() {
        return entryOpenedAt;
    }


    public UUID getEntryOpenedBy() {
        return entryOpenedBy;
    }


    public Instant getSubmittedAt() {
        return submittedAt;
    }


    public UUID getSubmittedBy() {
        return submittedBy;
    }


    public Instant getLockedAt() {
        return lockedAt;
    }


    public UUID getLockedBy() {
        return lockedBy;
    }


    public String getLockReason() {
        return lockReason;
    }


    public String getMarkSheetStatus() {
        return markSheetStatus;
    }
}
