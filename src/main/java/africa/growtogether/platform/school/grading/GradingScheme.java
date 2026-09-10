package africa.growtogether.platform.school.grading;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "gts_grading_scheme"
)
public class GradingScheme
        extends AuditedTenantEntity {


    @Column(
            name = "scheme_code",
            nullable = false,
            length = 100
    )
    private String schemeCode;


    @Column(
            name = "scheme_name",
            nullable = false,
            length = 250
    )
    private String schemeName;


    @Column(
            name = "grade_scale_type",
            nullable = false,
            length = 50
    )
    private String gradeScaleType;


    @Column(
            name = "curriculum_version_id"
    )
    private UUID curriculumVersionId;


    @Column(
            name = "education_level_id"
    )
    private UUID educationLevelId;


    @Column(
            name = "uses_marks",
            nullable = false
    )
    private boolean usesMarks;


    @Column(
            name = "uses_comments",
            nullable = false
    )
    private boolean usesComments;


    @Column(
            name = "uses_grade_points",
            nullable = false
    )
    private boolean usesGradePoints;


    @Column(
            name = "uses_aggregation",
            nullable = false
    )
    private boolean usesAggregation;


    @Column(
            name = "description",
            length = 1500
    )
    private String description;


    protected GradingScheme() {
    }


    public GradingScheme(
            String schemeCode,
            String schemeName,
            String gradeScaleType,
            UUID curriculumVersionId,
            UUID educationLevelId,
            boolean usesMarks,
            boolean usesComments,
            boolean usesGradePoints,
            boolean usesAggregation,
            String description
    ) {

        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
        this.gradeScaleType = gradeScaleType;
        this.curriculumVersionId = curriculumVersionId;
        this.educationLevelId = educationLevelId;
        this.usesMarks = usesMarks;
        this.usesComments = usesComments;
        this.usesGradePoints = usesGradePoints;
        this.usesAggregation = usesAggregation;
        this.description = description;

    }


    public String getSchemeCode() {
        return schemeCode;
    }


    public String getSchemeName() {
        return schemeName;
    }


    public String getGradeScaleType() {
        return gradeScaleType;
    }


    public UUID getCurriculumVersionId() {
        return curriculumVersionId;
    }


    public UUID getEducationLevelId() {
        return educationLevelId;
    }


    public boolean isUsesMarks() {
        return usesMarks;
    }


    public boolean isUsesComments() {
        return usesComments;
    }


    public boolean isUsesGradePoints() {
        return usesGradePoints;
    }


    public boolean isUsesAggregation() {
        return usesAggregation;
    }


    public String getDescription() {
        return description;
    }


    public void activate() {

        setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
        );

    }


    public void archive() {

        setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );

    }

}
