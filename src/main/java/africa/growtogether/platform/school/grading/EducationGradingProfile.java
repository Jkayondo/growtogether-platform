package africa.growtogether.platform.school.grading;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;


@Entity
@Table(
        name = "gts_education_grading_profile"
)
public class EducationGradingProfile
        extends AuditedTenantEntity {


    @Column(
            name = "profile_code",
            nullable = false,
            length = 100
    )
    private String profileCode;


    @Column(
            name = "profile_name",
            nullable = false,
            length = 250
    )
    private String profileName;


    @Column(
            name = "country_code",
            length = 10
    )
    private String countryCode;


    @Column(
            name = "education_system",
            length = 100
    )
    private String educationSystem;


    @Column(
            name = "description",
            length = 1500
    )
    private String description;


    protected EducationGradingProfile() {
    }


    public String getProfileCode() {
        return profileCode;
    }


    public String getProfileName() {
        return profileName;
    }


    public String getCountryCode() {
        return countryCode;
    }


    public String getEducationSystem() {
        return educationSystem;
    }


    public String getDescription() {
        return description;
    }


    public void activate() {

        setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
        );

    }


    public void archive() {

        setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );

    }

}
