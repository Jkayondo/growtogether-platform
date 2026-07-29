package africa.growtogether.platform.school.configuration;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "school_configurations",
        indexes = {
                @Index(
                        name = "ix_school_configuration_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class SchoolConfiguration extends AuditedTenantEntity {


    @Column(
            name = "school_name",
            nullable = false,
            length = 200
    )
    private String schoolName;


    @Column(
            name = "registration_number",
            length = 100
    )
    private String registrationNumber;


    @Column(
            name = "country_code",
            nullable = false,
            length = 10
    )
    private String countryCode;


    @Column(
            name = "region",
            length = 100
    )
    private String region;


    @Column(
            name = "school_type",
            nullable = false,
            length = 50
    )
    private String schoolType;


    @Column(
            name = "ownership_type",
            length = 50
    )
    private String ownershipType;


    @Column(
            name = "logo_document_id"
    )
    private UUID logoDocumentId;


    protected SchoolConfiguration() {
    }


    public SchoolConfiguration(
            UUID tenantId,
            String schoolName,
            String countryCode,
            String schoolType
    ) {

        setTenantId(tenantId);

        this.schoolName = schoolName;
        this.countryCode = countryCode;
        this.schoolType = schoolType;
    }


    public String getSchoolName() {
        return schoolName;
    }


    public String getRegistrationNumber() {
        return registrationNumber;
    }


    public String getCountryCode() {
        return countryCode;
    }


    public String getRegion() {
        return region;
    }


    public String getSchoolType() {
        return schoolType;
    }


    public String getOwnershipType() {
        return ownershipType;
    }


    public UUID getLogoDocumentId() {
        return logoDocumentId;
    }


    public void updateProfile(
            String schoolName,
            String registrationNumber,
            String region,
            String ownershipType
    ) {

        this.schoolName = schoolName;
        this.registrationNumber = registrationNumber;
        this.region = region;
        this.ownershipType = ownershipType;
    }
}
