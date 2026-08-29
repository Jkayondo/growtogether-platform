package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "gts_subject_catalogue",
        indexes = {
                @Index(
                        name = "ix_subject_catalogue_curriculum_version",
                        columnList = "tenant_id, curriculum_version_id"
                ),
                @Index(
                        name = "ix_subject_catalogue_learning_area",
                        columnList = "tenant_id, learning_area_id"
                ),
                @Index(
                        name = "ix_subject_catalogue_status",
                        columnList = "tenant_id, status"
                )
        }
)
public class SubjectCatalogue extends AuditedTenantEntity {


    @Column(
            name = "curriculum_version_id",
            nullable = false
    )
    private UUID curriculumVersionId;


    @Column(
            name = "learning_area_id",
            nullable = false
    )
    private UUID learningAreaId;


    @Column(
            name = "subject_code",
            nullable = false,
            length = 50
    )
    private String subjectCode;


    @Column(
            name = "subject_name",
            nullable = false,
            length = 150
    )
    private String subjectName;


    @Column(
            name = "subject_type",
            nullable = false,
            length = 50
    )
    private String subjectType;


    @Column(
            length = 500
    )
    private String description;


    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber = 1;


    protected SubjectCatalogue() {
    }


    public SubjectCatalogue(
            UUID curriculumVersionId,
            UUID learningAreaId,
            String subjectCode,
            String subjectName,
            String subjectType,
            String description,
            Integer sequenceNumber
    ) {

        this.curriculumVersionId = curriculumVersionId;
        this.learningAreaId = learningAreaId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.subjectType = subjectType;
        this.description = description;
        this.sequenceNumber = sequenceNumber;

    }


    public UUID getCurriculumVersionId() {
        return curriculumVersionId;
    }


    public UUID getLearningAreaId() {
        return learningAreaId;
    }


    public String getSubjectCode() {
        return subjectCode;
    }


    public String getSubjectName() {
        return subjectName;
    }


    public String getSubjectType() {
        return subjectType;
    }


    public String getDescription() {
        return description;
    }


    public Integer getSequenceNumber() {
        return sequenceNumber;
    }


    public void activate() {

        setStatus(
                EntityStatus.ACTIVE
        );

    }


    public void deactivate() {

        setStatus(
                EntityStatus.INACTIVE
        );

    }

}
