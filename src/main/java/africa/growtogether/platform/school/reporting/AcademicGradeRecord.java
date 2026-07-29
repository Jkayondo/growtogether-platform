package africa.growtogether.platform.school.reporting;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "academic_grade_records",
        indexes = {
                @Index(
                        name = "ix_academic_grade_record_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class AcademicGradeRecord
        extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "subject_configuration_id",
            nullable = false
    )
    private UUID subjectConfigurationId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "grade_scale",
            nullable = false,
            length = 30
    )
    private GradeScale gradeScale;


    @Column(
            name = "score",
            nullable = false
    )
    private Integer score;


    @Column(
            name = "grade_value",
            length = 20
    )
    private String gradeValue;


    protected AcademicGradeRecord() {
    }


    public AcademicGradeRecord(
            UUID tenantId,
            UUID learnerId,
            UUID subjectConfigurationId,
            GradeScale gradeScale,
            Integer score,
            String gradeValue
    ) {

        setTenantId(tenantId);

        this.learnerId = learnerId;
        this.subjectConfigurationId = subjectConfigurationId;
        this.gradeScale = gradeScale;
        this.score = score;
        this.gradeValue = gradeValue;
    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public UUID getSubjectConfigurationId() {
        return subjectConfigurationId;
    }


    public GradeScale getGradeScale() {
        return gradeScale;
    }


    public Integer getScore() {
        return score;
    }


    public String getGradeValue() {
        return gradeValue;
    }
}
