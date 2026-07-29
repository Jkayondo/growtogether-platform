package africa.growtogether.platform.school.assessment;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "assessment_configurations",
        indexes = {
                @Index(
                        name = "ix_assessment_configuration_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class AssessmentConfiguration
        extends AuditedTenantEntity {


    @Column(
            name = "subject_configuration_id",
            nullable = false
    )
    private UUID subjectConfigurationId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "assessment_type",
            nullable = false,
            length = 50
    )
    private AssessmentType assessmentType;


    @Column(
            name = "assessment_name",
            nullable = false,
            length = 150
    )
    private String assessmentName;


    @Column(
            name = "weight_percentage",
            nullable = false
    )
    private Integer weightPercentage;


    protected AssessmentConfiguration() {
    }


    public AssessmentConfiguration(
            UUID tenantId,
            UUID subjectConfigurationId,
            AssessmentType assessmentType,
            String assessmentName,
            Integer weightPercentage
    ) {

        setTenantId(tenantId);

        this.subjectConfigurationId = subjectConfigurationId;
        this.assessmentType = assessmentType;
        this.assessmentName = assessmentName;
        this.weightPercentage = weightPercentage;
    }


    public UUID getSubjectConfigurationId() {
        return subjectConfigurationId;
    }


    public AssessmentType getAssessmentType() {
        return assessmentType;
    }


    public String getAssessmentName() {
        return assessmentName;
    }


    public Integer getWeightPercentage() {
        return weightPercentage;
    }
}
