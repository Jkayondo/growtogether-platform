package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "gts_class_grade"
)
public class ClassGrade extends AuditedTenantEntity {


    @Column(
            name = "education_level_id",
            nullable = false
    )
    private UUID educationLevelId;


    @Column(
            name = "class_code",
            nullable = false,
            length = 60
    )
    private String classCode;


    @Column(
            name = "class_name",
            nullable = false,
            length = 160
    )
    private String className;


    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber;


    @Column
    private Integer capacity;


    protected ClassGrade() {
    }


    public ClassGrade(
            UUID educationLevelId,
            String classCode,
            String className,
            Integer sequenceNumber,
            Integer capacity
    ) {

        this.educationLevelId = educationLevelId;
        this.classCode = classCode;
        this.className = className;
        this.sequenceNumber = sequenceNumber;
        this.capacity = capacity;

    }


    public UUID getEducationLevelId() {
        return educationLevelId;
    }


    public String getClassCode() {
        return classCode;
    }


    public String getClassName() {
        return className;
    }


    public Integer getSequenceNumber() {
        return sequenceNumber;
    }


    public Integer getCapacity() {
        return capacity;
    }


    public void activate() {

        setStatus(EntityStatus.ACTIVE);

    }


    public void deactivate() {

        setStatus(EntityStatus.INACTIVE);

    }

}
