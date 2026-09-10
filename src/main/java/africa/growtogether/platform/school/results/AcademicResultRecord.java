package africa.growtogether.platform.school.results;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "academic_result_record")
public class AcademicResultRecord extends AuditedTenantEntity {


    @Column(
            name = "student_id",
            nullable = false
    )
    private UUID studentId;


    @Column(
            name = "academic_year_id"
    )
    private UUID academicYearId;


    @Column(
            name = "term_id"
    )
    private UUID termId;


    @Column(
            name = "grading_profile_id"
    )
    private UUID gradingProfileId;


    @Column(
            name = "grading_scheme_id"
    )
    private UUID gradingSchemeId;


    @Column(
            name = "subject_id"
    )
    private UUID subjectId;


    @Column(
            name = "class_id"
    )
    private UUID classId;


    @Column(
            name = "assessment_id"
    )
    private UUID assessmentId;


    @Column(
            name = "aggregation_rule_id"
    )
    private UUID aggregationRuleId;


    @Column(
            name = "division_rule_id"
    )
    private UUID divisionRuleId;


    @Column(
            name = "subject_name",
            nullable = false,
            length = 200
    )
    private String subjectName;


    @Column(
            name = "assessment_score",
            precision = 10,
            scale = 2
    )
    private BigDecimal assessmentScore;


    @Column(
            name = "grade_code",
            length = 40
    )
    private String gradeCode;


    @Column(
            name = "grade_name",
            length = 120
    )
    private String gradeName;


    @Column(
            name = "grade_point",
            precision = 10,
            scale = 2
    )
    private BigDecimal gradePoint;


    @Column(
            name = "aggregate_value",
            precision = 10,
            scale = 2
    )
    private BigDecimal aggregateValue;


    @Column(
            name = "division_code",
            length = 50
    )
    private String divisionCode;


    @Column(
            name = "division_name",
            length = 120
    )
    private String divisionName;


    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status;



    protected AcademicResultRecord() {
    }



    public AcademicResultRecord(

            UUID studentId,

            UUID academicYearId,

            UUID termId,

            UUID gradingProfileId,

            UUID gradingSchemeId,

            UUID subjectId,

            String subjectName,

            BigDecimal assessmentScore,

            String gradeCode,

            String gradeName,

            BigDecimal gradePoint,

            BigDecimal aggregateValue,

            String divisionCode,

            String divisionName

    ) {

        this.studentId = studentId;
        this.academicYearId = academicYearId;
        this.termId = termId;
        this.gradingProfileId = gradingProfileId;
        this.gradingSchemeId = gradingSchemeId;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.assessmentScore = assessmentScore;
        this.gradeCode = gradeCode;
        this.gradeName = gradeName;
        this.gradePoint = gradePoint;
        this.aggregateValue = aggregateValue;
        this.divisionCode = divisionCode;
        this.divisionName = divisionName;

    }



    public UUID getStudentId() {
        return studentId;
    }


    public UUID getAcademicYearId() {
        return academicYearId;
    }


    public UUID getTermId() {
        return termId;
    }


    public UUID getSubjectId() {
        return subjectId;
    }


    public UUID getGradingSchemeId() {
        return gradingSchemeId;
    }


    public UUID getClassId() {
        return classId;
    }


    public UUID getAssessmentId() {
        return assessmentId;
    }


    public UUID getAggregationRuleId() {
        return aggregationRuleId;
    }


    public UUID getDivisionRuleId() {
        return divisionRuleId;
    }


    public String getSubjectName() {
        return subjectName;
    }


    public BigDecimal getAssessmentScore() {
        return assessmentScore;
    }


    public String getGradeCode() {
        return gradeCode;
    }


    public String getGradeName() {
        return gradeName;
    }


    public BigDecimal getGradePoint() {
        return gradePoint;
    }


    public BigDecimal getAggregateValue() {
        return aggregateValue;
    }


    public String getDivisionCode() {
        return divisionCode;
    }


    public String getDivisionName() {
        return divisionName;
    }
}
