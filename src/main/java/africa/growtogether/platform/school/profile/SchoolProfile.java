package africa.growtogether.platform.school.profile;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "gts_school_profile")
public class SchoolProfile extends AuditedTenantEntity {

    @Column(name = "school_code", nullable = false, length = 80)
    private String schoolCode;

    @Column(name = "school_name", nullable = false, length = 200)
    private String schoolName;

    @Column(name = "legal_name", length = 250)
    private String legalName;

    @Column(name = "education_system", length = 100)
    private String educationSystem;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "default_currency", length = 3)
    private String defaultCurrency;

    @Column(length = 80)
    private String timezone;

    @Column(length = 200)
    private String email;

    @Column(name = "phone_number", length = 40)
    private String phoneNumber;

    @Column(length = 250)
    private String website;


    protected SchoolProfile() {
    }


    public SchoolProfile(
            String schoolCode,
            String schoolName,
            String legalName,
            String educationSystem,
            String countryCode,
            String defaultCurrency,
            String timezone,
            String email,
            String phoneNumber,
            String website
    ) {
        this.schoolCode = requireText(schoolCode, "schoolCode");
        this.schoolName = requireText(schoolName, "schoolName");
        this.legalName = legalName;
        this.educationSystem = educationSystem;
        this.countryCode = countryCode;
        this.defaultCurrency = defaultCurrency;
        this.timezone = timezone;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.website = website;
    }


    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value.trim();
    }


    public String getSchoolCode() {
        return schoolCode;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getEducationSystem() {
        return educationSystem;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getWebsite() {
        return website;
    }
}
