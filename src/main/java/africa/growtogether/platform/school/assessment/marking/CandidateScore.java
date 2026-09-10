package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "gts_candidate_score")
public class CandidateScore extends AuditedTenantEntity {


    @Column(name = "mark_sheet_id",
            nullable = false)
    private UUID markSheetId;


    @Column(name = "mark_entry_batch_id")
    private UUID markEntryBatchId;


    @Column(name = "examination_candidate_id")
    private UUID examinationCandidateId;


    @Column(name = "candidate_paper_registration_id")
    private UUID candidatePaperRegistrationId;


    @Column(name = "student_id",
            nullable = false)
    private UUID studentId;


    @Column(name = "student_enrollment_id",
            nullable = false)
    private UUID studentEnrollmentId;


    @Column(name = "raw_score", precision = 10, scale = 2)
    private BigDecimal rawScore;


    @Column(name = "adjusted_score", precision = 10, scale = 2)
    private BigDecimal adjustedScore;


    @Column(name = "final_score", precision = 10, scale = 2)
    private BigDecimal finalScore;


    @Column(name = "score_status",
            nullable = false,
            length = 30)
    private String scoreStatus;


    @Column(name = "absent",
            nullable = false)
    private boolean absent;


    @Column(name = "exempted",
            nullable = false)
    private boolean exempted;


    @Column(name = "missing_mark",
            nullable = false)
    private boolean missingMark;


    @Column(name = "withheld",
            nullable = false)
    private boolean withheld;


    protected CandidateScore() {
    }


    public CandidateScore(
            UUID markSheetId,
            UUID studentId,
            UUID studentEnrollmentId
    ) {

        this.markSheetId = markSheetId;
        this.studentId = studentId;
        this.studentEnrollmentId = studentEnrollmentId;
        this.scoreStatus = "DRAFT";

        this.absent = false;
        this.exempted = false;
        this.missingMark = false;
        this.withheld = false;

    }


    public void enterScore(
            BigDecimal score
    ) {

        this.rawScore = score;
        this.finalScore = score;
        this.scoreStatus = "ENTERED";

    }


    public void validate() {

        this.scoreStatus = "VALIDATED";

    }


    public void approve() {

        this.scoreStatus = "APPROVED";

    }


    public void markAbsent() {

        this.absent = true;
        this.scoreStatus = "ABSENT";

    }


    public UUID getStudentId() {
        return studentId;
    }


    public BigDecimal getFinalScore() {
        return finalScore;
    }


    public String getScoreStatus() {
        return scoreStatus;
    }

}
