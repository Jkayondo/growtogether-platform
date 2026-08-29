package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;

import jakarta.persistence.*;


@Entity
@Table(
        name = "gts_education_level"
)
public class EducationLevel extends AuditedTenantEntity {


    @Column(
            name = "level_code",
            nullable = false,
            length = 60
    )
    private String levelCode;


    @Column(
            name = "level_name",
            nullable = false,
            length = 160
    )
    private String levelName;


    @Column(
            length = 500
    )
    private String description;


    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber;


    protected EducationLevel() {
    }


    public EducationLevel(
            String levelCode,
            String levelName,
            String description,
            Integer sequenceNumber
    ) {

        this.levelCode = levelCode;
        this.levelName = levelName;
        this.description = description;
        this.sequenceNumber = sequenceNumber;

    }


    public String getLevelCode() {
        return levelCode;
    }


    public String getLevelName() {
        return levelName;
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
