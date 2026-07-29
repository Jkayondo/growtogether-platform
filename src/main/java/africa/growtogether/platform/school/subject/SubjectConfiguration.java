package africa.growtogether.platform.school.subject;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "subject_configurations",
        indexes = {
                @Index(
                        name = "ix_subject_configuration_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class SubjectConfiguration extends AuditedTenantEntity {


    @Column(
            name = "academic_grade_id",
            nullable = false
    )
    private UUID academicGradeId;


    @Column(
            name = "subject_name",
            nullable = false,
            length = 150
    )
    private String subjectName;


    @Column(
            name = "subject_code",
            length = 50
    )
    private String subjectCode;


    @Column(
            name = "mandatory",
            nullable = false
    )
    private boolean mandatory;


    protected SubjectConfiguration() {
    }


    public SubjectConfiguration(
            UUID tenantId,
            UUID academicGradeId,
            String subjectName,
            String subjectCode,
            boolean mandatory
    ) {

        setTenantId(tenantId);

        this.academicGradeId = academicGradeId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.mandatory = mandatory;
    }


    public UUID getAcademicGradeId() {
        return academicGradeId;
    }


    public String getSubjectName() {
        return subjectName;
    }


    public String getSubjectCode() {
        return subjectCode;
    }


    public boolean isMandatory() {
        return mandatory;
    }
}
