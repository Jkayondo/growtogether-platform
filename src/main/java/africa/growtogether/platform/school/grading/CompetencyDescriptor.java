package africa.growtogether.platform.school.grading;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "gts_competency_descriptor"
)
public class CompetencyDescriptor
        extends AuditedTenantEntity {


    @Column(
            name = "grading_scheme_id",
            nullable = false
    )
    private UUID gradingSchemeId;


    @Column(
            name = "descriptor_code",
            nullable = false,
            length = 100
    )
    private String descriptorCode;


    @Column(
            name = "descriptor_name",
            nullable = false,
            length = 250
    )
    private String descriptorName;


    @Column(
            name = "performance_level",
            nullable = false,
            length = 100
    )
    private String performanceLevel;


    @Column(
            name = "description",
            length = 1500
    )
    private String description;


    @Column(
            name = "teacher_guidance",
            length = 2000
    )
    private String teacherGuidance;


    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber;


    protected CompetencyDescriptor() {
    }


    public UUID getGradingSchemeId() {
        return gradingSchemeId;
    }


    public String getDescriptorCode() {
        return descriptorCode;
    }


    public String getDescriptorName() {
        return descriptorName;
    }


    public String getPerformanceLevel() {
        return performanceLevel;
    }


    public String getDescription() {
        return description;
    }


    public String getTeacherGuidance() {
        return teacherGuidance;
    }


    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

}
