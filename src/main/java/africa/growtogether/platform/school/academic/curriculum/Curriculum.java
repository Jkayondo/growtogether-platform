package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;


@Entity
@Table(name = "gts_curriculum")
public class Curriculum extends AuditedTenantEntity {


    @Column(
            name = "curriculum_code",
            nullable = false,
            length = 100
    )
    private String curriculumCode;


    @Column(
            name = "curriculum_name",
            nullable = false,
            length = 250
    )
    private String curriculumName;


    @Column(
            name = "description",
            length = 1500
    )
    private String description;


    @Column(
            name = "curriculum_authority",
            length = 250
    )
    private String curriculumAuthority;


    @Column(
            name = "country_code",
            length = 3
    )
    private String countryCode;


    @Column(
            name = "education_system",
            length = 120
    )
    private String educationSystem;


    @Column(
            name = "curriculum_type",
            nullable = false,
            length = 40
    )
    private String curriculumType;


    @Column(
            name = "approval_reference",
            length = 160
    )
    private String approvalReference;


    @Column(
            name = "effective_from"
    )
    private LocalDate effectiveFrom;


    @Column(
            name = "effective_to"
    )
    private LocalDate effectiveTo;


    @Column(
            name = "curriculum_status",
            nullable = false,
            length = 30
    )
    private String curriculumStatus;


    protected Curriculum() {
    }


    public Curriculum(
            String curriculumCode,
            String curriculumName,
            String curriculumType
    ) {

        this.curriculumCode = curriculumCode;
        this.curriculumName = curriculumName;
        this.curriculumType = curriculumType;
        this.curriculumStatus = "DRAFT";
    }


    public String getCurriculumCode() {
        return curriculumCode;
    }


    public String getCurriculumName() {
        return curriculumName;
    }


    public String getCurriculumType() {
        return curriculumType;
    }


    public String getCurriculumStatus() {
        return curriculumStatus;
    }

public void activate() {

    if (!"APPROVED".equals(this.curriculumStatus)
            && !"DRAFT".equals(this.curriculumStatus)) {

        throw new IllegalStateException(
                "Only draft or approved curriculum can be activated"
        );
    }

    this.curriculumStatus = "ACTIVE";
}

}
