package africa.growtogether.platform.school.visitor.domain;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "school_visitors")
public class SchoolVisitor extends AuditedTenantEntity {


    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;


    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;


    @Column(name = "phone_number", length = 50)
    private String phoneNumber;


    @Column(name = "email", length = 150)
    private String email;


    @Column(name = "identification_type", length = 50)
    private String identificationType;


    @Column(name = "identification_reference", length = 100)
    private String identificationReference;


    @Column(name = "visitor_category", length = 50)
    private String visitorCategory;


    protected SchoolVisitor() {
        // JPA constructor
    }


    public SchoolVisitor(
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            String identificationType,
            String identificationReference,
            String visitorCategory
    ) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.identificationType = identificationType;
        this.identificationReference = identificationReference;
        this.visitorCategory = visitorCategory;
    }


    public String getFirstName() {
        return firstName;
    }


    public String getLastName() {
        return lastName;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }


    public String getEmail() {
        return email;
    }


    public String getIdentificationType() {
        return identificationType;
    }


    public String getIdentificationReference() {
        return identificationReference;
    }


    public String getVisitorCategory() {
        return visitorCategory;
    }
}
