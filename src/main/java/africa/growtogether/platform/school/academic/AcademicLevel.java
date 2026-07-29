package africa.growtogether.platform.school.academic;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "academic_levels",
        indexes = {
                @Index(
                        name = "ix_academic_level_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class AcademicLevel extends AuditedTenantEntity {


    @Column(
            name = "curriculum_configuration_id",
            nullable = false
    )
    private UUID curriculumConfigurationId;


    @Column(
            name = "level_name",
            nullable = false,
            length = 100
    )
    private String levelName;


    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;


    protected AcademicLevel() {
    }


    public AcademicLevel(
            UUID tenantId,
            UUID curriculumConfigurationId,
            String levelName,
            Integer displayOrder
    ) {

        setTenantId(tenantId);

        this.curriculumConfigurationId = curriculumConfigurationId;
        this.levelName = levelName;
        this.displayOrder = displayOrder;
    }


    public UUID getCurriculumConfigurationId() {
        return curriculumConfigurationId;
    }


    public String getLevelName() {
        return levelName;
    }


    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
