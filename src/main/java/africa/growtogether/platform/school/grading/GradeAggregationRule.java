package africa.growtogether.platform.school.grading;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "gts_grade_aggregation_rule")
public class GradeAggregationRule
        extends AuditedTenantEntity {


    @Column(
            name = "grading_scheme_id",
            nullable = false
    )
    private UUID gradingSchemeId;


    @Column(
            name = "rule_code",
            nullable = false,
            length = 100
    )
    private String ruleCode;


    @Column(
            name = "rule_name",
            nullable = false,
            length = 250
    )
    private String ruleName;


    @Column(
            name = "aggregation_type",
            nullable = false,
            length = 80
    )
    private String aggregationType;


    @Column(
            name = "required_subject_count"
    )
    private Integer requiredSubjectCount;


    @Column(
            name = "best_subject_count"
    )
    private Integer bestSubjectCount;


    @Column(
            name = "include_compulsory_subjects",
            nullable = false
    )
    private boolean includeCompulsorySubjects;


    @Column(
            name = "uses_grade_points",
            nullable = false
    )
    private boolean usesGradePoints;


    @Column(
            name = "description",
            length = 1500
    )
    private String description;


    protected GradeAggregationRule() {
    }


    public UUID getGradingSchemeId() {
        return gradingSchemeId;
    }


    public String getRuleCode() {
        return ruleCode;
    }


    public String getRuleName() {
        return ruleName;
    }


    public String getAggregationType() {
        return aggregationType;
    }


    public Integer getRequiredSubjectCount() {
        return requiredSubjectCount;
    }


    public Integer getBestSubjectCount() {
        return bestSubjectCount;
    }


    public boolean isIncludeCompulsorySubjects() {
        return includeCompulsorySubjects;
    }


    public boolean isUsesGradePoints() {
        return usesGradePoints;
    }


    public String getDescription() {
        return description;
    }

}
