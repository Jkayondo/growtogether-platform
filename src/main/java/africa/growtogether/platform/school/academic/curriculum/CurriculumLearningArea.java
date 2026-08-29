package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "gts_curriculum_learning_area",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_gts_learning_area_code",
                        columnNames = {
                                "tenant_id",
                                "curriculum_version_id",
                                "learning_area_code"
                        }
                )
        }
)
public class CurriculumLearningArea extends AuditedTenantEntity {


    @Column(
            name = "curriculum_version_id",
            nullable = false
    )
    private UUID curriculumVersionId;


    @Column(
            name = "learning_area_code",
            nullable = false,
            length = 50
    )
    private String learningAreaCode;


    @Column(
            name = "learning_area_name",
            nullable = false,
            length = 150
    )
    private String learningAreaName;


    @Column(
            name = "learning_area_type",
            nullable = false,
            length = 50
    )
    private String learningAreaType;


    @Column(
            length = 500
    )
    private String description;


    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber = 1;



    protected CurriculumLearningArea() {
    }


    public CurriculumLearningArea(
            UUID curriculumVersionId,
            String learningAreaCode,
            String learningAreaName,
            String learningAreaType,
            String description,
            Integer sequenceNumber
    ) {

        this.curriculumVersionId = curriculumVersionId;
        this.learningAreaCode = learningAreaCode;
        this.learningAreaName = learningAreaName;
        this.learningAreaType = learningAreaType;
        this.description = description;
        this.sequenceNumber = sequenceNumber;

    }


    public UUID getCurriculumVersionId() {
        return curriculumVersionId;
    }


    public String getLearningAreaCode() {
        return learningAreaCode;
    }


    public String getLearningAreaName() {
        return learningAreaName;
    }


    public String getLearningAreaType() {
        return learningAreaType;
    }


    public String getDescription() {
        return description;
    }


    public Integer getSequenceNumber() {
        return sequenceNumber;
    }


    public void deactivate() {
        setStatus(
            africa.growtogether.platform.common.persistence.EntityStatus.INACTIVE
        );
    }


    public void activate() {
        setStatus(
            africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
        );
     }

}
