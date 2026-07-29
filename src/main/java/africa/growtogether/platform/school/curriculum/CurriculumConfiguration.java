package africa.growtogether.platform.school.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "curriculum_configurations",
        indexes = {
                @Index(
                        name = "ix_curriculum_configuration_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class CurriculumConfiguration
        extends AuditedTenantEntity {


    @Column(
            name = "school_configuration_id",
            nullable = false
    )
    private UUID schoolConfigurationId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "curriculum_type",
            nullable = false,
            length = 50
    )
    private CurriculumType curriculumType;


    @Column(
            name = "curriculum_name",
            nullable = false,
            length = 200
    )
    private String curriculumName;


    @Column(
            name = "country_code",
            length = 10
    )
    private String countryCode;


    protected CurriculumConfiguration() {
    }


    public CurriculumConfiguration(
            UUID tenantId,
            UUID schoolConfigurationId,
            CurriculumType curriculumType,
            String curriculumName,
            String countryCode
    ) {

        setTenantId(tenantId);

        this.schoolConfigurationId = schoolConfigurationId;
        this.curriculumType = curriculumType;
        this.curriculumName = curriculumName;
        this.countryCode = countryCode;
    }


    public UUID getSchoolConfigurationId() {
        return schoolConfigurationId;
    }


    public CurriculumType getCurriculumType() {
        return curriculumType;
    }


    public String getCurriculumName() {
        return curriculumName;
    }


    public String getCountryCode() {
        return countryCode;
    }
}
