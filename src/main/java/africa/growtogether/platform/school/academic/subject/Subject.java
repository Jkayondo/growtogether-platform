package africa.growtogether.platform.school.academic.subject;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;


@Entity
@Table(
        name = "gts_subject",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_gts_subject_tenant_code",
                        columnNames = {
                                "tenant_id",
                                "subject_code"
                        }
                )
        }
)
public class Subject extends AuditedTenantEntity {


    @Column(
            name = "subject_code",
            nullable = false,
            length = 60
    )
    private String subjectCode;


    @Column(
            name = "subject_name",
            nullable = false,
            length = 180
    )
    private String subjectName;


    @Column(
            name = "short_name",
            length = 80
    )
    private String shortName;


    @Column(
            name = "subject_type",
            nullable = false,
            length = 30
    )
    private String subjectType;


    @Column(
            length = 500
    )
    private String description;


    protected Subject() {
    }


    public Subject(
            String subjectCode,
            String subjectName,
            String shortName,
            String subjectType,
            String description
    ) {

        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.shortName = shortName;
        this.subjectType = subjectType;
        this.description = description;

    }


    public String getSubjectCode() {
        return subjectCode;
    }


    public String getSubjectName() {
        return subjectName;
    }


    public String getShortName() {
        return shortName;
    }


    public String getSubjectType() {
        return subjectType;
    }


    public String getDescription() {
        return description;
    }


    public void activate() {

        setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
        );

    }


    public void deactivate() {

        setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.INACTIVE
        );

    }

}
