package africa.growtogether.platform.school.academic;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "academic_grades",
        indexes = {
                @Index(
                        name = "ix_academic_grade_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class AcademicGrade extends AuditedTenantEntity {


    @Column(
            name = "academic_level_id",
            nullable = false
    )
    private UUID academicLevelId;


    @Column(
            name = "grade_name",
            nullable = false,
            length = 100
    )
    private String gradeName;


    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;


    protected AcademicGrade() {
    }


    public AcademicGrade(
            UUID tenantId,
            UUID academicLevelId,
            String gradeName,
            Integer displayOrder
    ) {

        setTenantId(tenantId);

        this.academicLevelId = academicLevelId;
        this.gradeName = gradeName;
        this.displayOrder = displayOrder;
    }


    public UUID getAcademicLevelId() {
        return academicLevelId;
    }


    public String getGradeName() {
        return gradeName;
    }


    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
