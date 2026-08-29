package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "gts_campus"
)
public class Campus extends AuditedTenantEntity {


    @Column(
            name = "school_profile_id",
            nullable = false
    )
    private UUID schoolProfileId;


    @Column(
            name = "campus_code",
            nullable = false,
            length = 80
    )
    private String campusCode;


    @Column(
            name = "campus_name",
            nullable = false,
            length = 200
    )
    private String campusName;


    @Column(
            name = "address_line",
            length = 300
    )
    private String addressLine;


    @Column(
            length = 120
    )
    private String district;


    @Column(
            length = 120
    )
    private String city;


    @Column(
            name = "country_code",
            length = 2
    )
    private String countryCode;


    @Column(
            name = "phone_number",
            length = 40
    )
    private String phoneNumber;


    @Column(
            length = 200
    )
    private String email;


    @Column(
            name = "main_campus",
            nullable = false
    )
    private boolean mainCampus;


    protected Campus() {
    }


    public Campus(
            UUID schoolProfileId,
            String campusCode,
            String campusName,
            String addressLine,
            String district,
            String city,
            String countryCode,
            String phoneNumber,
            String email,
            boolean mainCampus
    ) {

        this.schoolProfileId = schoolProfileId;
        this.campusCode = campusCode;
        this.campusName = campusName;
        this.addressLine = addressLine;
        this.district = district;
        this.city = city;
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.mainCampus = mainCampus;

    }


    public UUID getSchoolProfileId() {
        return schoolProfileId;
    }


    public String getCampusCode() {
        return campusCode;
    }


    public String getCampusName() {
        return campusName;
    }


    public String getAddressLine() {
        return addressLine;
    }


    public String getDistrict() {
        return district;
    }


    public String getCity() {
        return city;
    }


    public String getCountryCode() {
        return countryCode;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }


    public String getEmail() {
        return email;
    }


    public boolean isMainCampus() {
        return mainCampus;
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
