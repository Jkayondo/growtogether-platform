package africa.growtogether.platform.school.grading;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;
import java.math.BigDecimal;


@Entity
@Table(name = "gts_grade_boundary")
public class GradeBoundary extends AuditedTenantEntity {


    @Column(
            name = "grading_scheme_id",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private UUID gradingSchemeId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "grading_scheme_id",
            nullable = false
    )
    private GradingScheme gradingScheme;


    @Column(
            name = "grade_code",
            nullable = false,
            length = 40
    )
    private String gradeCode;


    @Column(
            name = "grade_name",
            nullable = false,
            length = 120
    )
    private String gradeName;


    @Column(
            name = "minimum_score",
            nullable = false
    )
    private BigDecimal minimumScore;


    @Column(
            name = "maximum_score",
            nullable = false
    )
    private BigDecimal maximumScore;


    @Column(
            name = "grade_point"
    )
    private BigDecimal gradePoint;


    @Column(
            name = "pass_grade",
            nullable = false
    )
    private boolean passGrade = true;


    @Column(
            name = "distinction_grade",
            nullable = false
    )
    private boolean distinctionGrade = false;


    @Column(
            name = "description",
            length = 500
    )
    private String description;


    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber;


    protected GradeBoundary() {
    }


    public GradeBoundary(
            UUID gradingSchemeId,
            String gradeCode,
            String gradeName,
            BigDecimal minimumScore,
            BigDecimal maximumScore,
            BigDecimal gradePoint,
            Integer sequenceNumber
    ) {

        this.gradingSchemeId = gradingSchemeId;
        this.gradeCode = gradeCode;
        this.gradeName = gradeName;
        this.minimumScore = minimumScore;
        this.maximumScore = maximumScore;
        this.gradePoint = gradePoint;
        this.sequenceNumber = sequenceNumber;

    }


    public UUID getGradingSchemeId() {
        return gradingSchemeId;
    }


    public GradingScheme getGradingScheme() {
        return gradingScheme;
    }


    public String getGradeCode() {
        return gradeCode;
    }


    public String getGradeName() {
        return gradeName;
    }


    public BigDecimal getMinimumScore() {
        return minimumScore;
    }


    public BigDecimal getMaximumScore() {
        return maximumScore;
    }


    public BigDecimal getGradePoint() {
        return gradePoint;
    }


    public boolean isPassGrade() {
        return passGrade;
    }


    public boolean isDistinctionGrade() {
        return distinctionGrade;
    }


    public String getDescription() {
        return description;
    }


    public Integer getSequenceNumber() {
        return sequenceNumber;
    }


    public boolean matches(
            BigDecimal score
    ) {

        return score != null
                && score.compareTo(minimumScore) >= 0
                && score.compareTo(maximumScore) <= 0;

    }

}
