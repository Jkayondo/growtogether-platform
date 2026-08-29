package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(
        name = "gts_subject_offering"
)
public class SubjectOffering extends AuditedTenantEntity {


    @Column(
            name = "subject_offering_code",
            nullable = false,
            length = 120
    )
    private String subjectOfferingCode;


    @Column(
            name = "class_offering_id",
            nullable = false
    )
    private UUID classOfferingId;


    @Column(
            name = "academic_term_id"
    )
    private UUID academicTermId;


    @Column(
            name = "stream_id"
    )
    private UUID streamId;


    @Column(
            name = "subject_id",
            nullable = false
    )
    private UUID subjectId;


    @Column(
            name = "academic_department_id"
    )
    private UUID academicDepartmentId;


    @Column(
            name = "grading_scheme_id"
    )
    private UUID gradingSchemeId;


    @Column(
            name = "weekly_periods"
    )
    private Integer weeklyPeriods;


    @Column(
            name = "credit_value",
            precision = 8,
            scale = 2
    )
    private BigDecimal creditValue;


    @Column(
            name = "minimum_enrollment"
    )
    private Integer minimumEnrollment;


    @Column(
            name = "maximum_enrollment"
    )
    private Integer maximumEnrollment;


    @Column(
            name = "offering_status",
            nullable = false,
            length = 30
    )
    private String offeringStatus = "PLANNED";


    protected SubjectOffering() {
    }


    public SubjectOffering(
            String subjectOfferingCode,
            UUID classOfferingId,
            UUID academicTermId,
            UUID streamId,
            UUID subjectId,
            UUID academicDepartmentId,
            UUID gradingSchemeId,
            Integer weeklyPeriods,
            BigDecimal creditValue,
            Integer minimumEnrollment,
            Integer maximumEnrollment
    ) {

        this.subjectOfferingCode = subjectOfferingCode;
        this.classOfferingId = classOfferingId;
        this.academicTermId = academicTermId;
        this.streamId = streamId;
        this.subjectId = subjectId;
        this.academicDepartmentId = academicDepartmentId;
        this.gradingSchemeId = gradingSchemeId;
        this.weeklyPeriods = weeklyPeriods;
        this.creditValue = creditValue;
        this.minimumEnrollment = minimumEnrollment;
        this.maximumEnrollment = maximumEnrollment;

    }


    public String getSubjectOfferingCode() {
        return subjectOfferingCode;
    }


    public UUID getClassOfferingId() {
        return classOfferingId;
    }


    public UUID getAcademicTermId() {
        return academicTermId;
    }


    public UUID getStreamId() {
        return streamId;
    }


    public UUID getSubjectId() {
        return subjectId;
    }


    public UUID getAcademicDepartmentId() {
        return academicDepartmentId;
    }


    public UUID getGradingSchemeId() {
        return gradingSchemeId;
    }


    public Integer getWeeklyPeriods() {
        return weeklyPeriods;
    }


    public BigDecimal getCreditValue() {
        return creditValue;
    }


    public Integer getMinimumEnrollment() {
        return minimumEnrollment;
    }


    public Integer getMaximumEnrollment() {
        return maximumEnrollment;
    }


    public String getOfferingStatus() {
        return offeringStatus;
    }


    public void activate() {

        setStatus(EntityStatus.ACTIVE);
        this.offeringStatus = "ACTIVE";

    }


    public void deactivate() {

        setStatus(EntityStatus.INACTIVE);
        this.offeringStatus = "CANCELLED";

    }

}
