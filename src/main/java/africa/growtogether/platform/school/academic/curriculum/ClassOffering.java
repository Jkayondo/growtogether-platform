package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(
        name = "gts_class_offering"
)
public class ClassOffering extends AuditedTenantEntity {


    @Column(
            name = "offering_code",
            nullable = false,
            length = 100
    )
    private String offeringCode;


    @Column(
            name = "academic_year_id",
            nullable = false
    )
    private UUID academicYearId;


    @Column(
            name = "campus_id",
            nullable = false
    )
    private UUID campusId;


    @Column(
            name = "academic_programme_id"
    )
    private UUID academicProgrammeId;


    @Column(
            name = "study_track_id"
    )
    private UUID studyTrackId;


    @Column(
            name = "curriculum_version_id"
    )
    private UUID curriculumVersionId;


    @Column(
            name = "class_grade_id",
            nullable = false
    )
    private UUID classGradeId;


    @Column(
            name = "planned_capacity"
    )
    private Integer plannedCapacity;


    @Column(
            name = "minimum_enrollment"
    )
    private Integer minimumEnrollment;


    @Column(
            name = "maximum_enrollment"
    )
    private Integer maximumEnrollment;


    @Column(
            name = "enrollment_open_date"
    )
    private LocalDate enrollmentOpenDate;


    @Column(
            name = "enrollment_close_date"
    )
    private LocalDate enrollmentCloseDate;


    @Column(
            name = "offering_status",
            nullable = false,
            length = 30
    )
    private String offeringStatus = "PLANNED";


    protected ClassOffering() {
    }


    public ClassOffering(
            String offeringCode,
            UUID academicYearId,
            UUID campusId,
            UUID academicProgrammeId,
            UUID studyTrackId,
            UUID curriculumVersionId,
            UUID classGradeId,
            Integer plannedCapacity,
            Integer minimumEnrollment,
            Integer maximumEnrollment,
            LocalDate enrollmentOpenDate,
            LocalDate enrollmentCloseDate
    ) {

        this.offeringCode = offeringCode;
        this.academicYearId = academicYearId;
        this.campusId = campusId;
        this.academicProgrammeId = academicProgrammeId;
        this.studyTrackId = studyTrackId;
        this.curriculumVersionId = curriculumVersionId;
        this.classGradeId = classGradeId;
        this.plannedCapacity = plannedCapacity;
        this.minimumEnrollment = minimumEnrollment;
        this.maximumEnrollment = maximumEnrollment;
        this.enrollmentOpenDate = enrollmentOpenDate;
        this.enrollmentCloseDate = enrollmentCloseDate;

    }


    public String getOfferingCode() {
        return offeringCode;
    }


    public UUID getAcademicYearId() {
        return academicYearId;
    }


    public UUID getCampusId() {
        return campusId;
    }


    public UUID getAcademicProgrammeId() {
        return academicProgrammeId;
    }


    public UUID getStudyTrackId() {
        return studyTrackId;
    }


    public UUID getCurriculumVersionId() {
        return curriculumVersionId;
    }


    public UUID getClassGradeId() {
        return classGradeId;
    }


    public Integer getPlannedCapacity() {
        return plannedCapacity;
    }


    public Integer getMinimumEnrollment() {
        return minimumEnrollment;
    }


    public Integer getMaximumEnrollment() {
        return maximumEnrollment;
    }


    public LocalDate getEnrollmentOpenDate() {
        return enrollmentOpenDate;
    }


    public LocalDate getEnrollmentCloseDate() {
        return enrollmentCloseDate;
    }


    public String getOfferingStatus() {
        return offeringStatus;
    }


    public void activate() {

        setStatus(EntityStatus.ACTIVE);
        offeringStatus = "ACTIVE";

    }


    public void deactivate() {

        setStatus(EntityStatus.INACTIVE);
        offeringStatus = "CANCELLED";

    }

}
