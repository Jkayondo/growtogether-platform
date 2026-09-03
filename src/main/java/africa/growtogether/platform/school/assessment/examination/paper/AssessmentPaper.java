package africa.growtogether.platform.school.assessment.examination.paper;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "gts_assessment_paper")
public class AssessmentPaper extends AuditedTenantEntity {


    @Column(
            name = "paper_code",
            nullable = false,
            length = 100
    )
    private String paperCode;


    @Column(
            name = "paper_name",
            nullable = false,
            length = 250
    )
    private String paperName;


    @Column(
            name = "assessment_component_id"
    )
    private UUID assessmentComponentId;


    @Column(
            name = "examination_session_id"
    )
    private UUID examinationSessionId;


    @Column(
            name = "subject_offering_id",
            nullable = false
    )
    private UUID subjectOfferingId;


    @Column(
            name = "paper_type",
            nullable = false,
            length = 40
    )
    private String paperType;


    @Column(
            name = "paper_number"
    )
    private Integer paperNumber;


    @Column(
            name = "duration_minutes"
    )
    private Integer durationMinutes;


    @Column(
            name = "maximum_score",
            precision = 10,
            scale = 2,
            nullable = false
    )
    private BigDecimal maximumScore;


    @Column(
            name = "instructions",
            length = 3000
    )
    private String instructions;


    @Column(
            name = "eds_question_paper_document_id"
    )
    private UUID questionPaperDocumentId;


    @Column(
            name = "eds_marking_guide_document_id"
    )
    private UUID markingGuideDocumentId;


    @Column(
            name = "eds_answer_booklet_document_id"
    )
    private UUID answerBookletDocumentId;


    @Column(
            name = "confidential",
            nullable = false
    )
    private boolean confidential = true;


    @Column(
            name = "paper_status",
            nullable = false,
            length = 30
    )
    private String paperStatus;


    protected AssessmentPaper() {
    }


    public AssessmentPaper(
            String paperCode,
            String paperName,
            UUID subjectOfferingId,
            BigDecimal maximumScore
    ) {

        this.paperCode = paperCode;
        this.paperName = paperName;
        this.subjectOfferingId = subjectOfferingId;
        this.maximumScore = maximumScore;

        this.paperType = "WRITTEN";
        this.paperStatus = "DRAFT";
        this.confidential = true;

    }


    public String getPaperCode() {
        return paperCode;
    }


    public String getPaperName() {
        return paperName;
    }


    public UUID getAssessmentComponentId() {
        return assessmentComponentId;
    }


    public UUID getExaminationSessionId() {
        return examinationSessionId;
    }


    public UUID getSubjectOfferingId() {
        return subjectOfferingId;
    }


    public String getPaperStatus() {
        return paperStatus;
    }


    public BigDecimal getMaximumScore() {
        return maximumScore;
    }


    public void submitForReview() {

        this.paperStatus = "UNDER_REVIEW";

    }


    public void moderate() {

        this.paperStatus = "MODERATED";

    }


    public void approve() {

        this.paperStatus = "APPROVED";

    }


    public void schedule() {

        this.paperStatus = "SCHEDULED";

    }


    public void complete() {

        this.paperStatus = "COMPLETED";

    }

}
