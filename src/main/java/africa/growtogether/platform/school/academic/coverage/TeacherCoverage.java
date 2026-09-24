package africa.growtogether.platform.school.academic.coverage;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_teacher_coverage")
public class TeacherCoverage extends AuditedTenantEntity {


    @Column(
            name = "teacher_profile_id",
            nullable = false
    )
    private UUID teacherProfileId;


    @Column(
            name = "teaching_assignment_id",
            nullable = false
    )
    private UUID teachingAssignmentId;


    @Column(
            name = "academic_year_id",
            nullable = false
    )
    private UUID academicYearId;


    @Column(
            name = "academic_term_id"
    )
    private UUID academicTermId;


    @Column(
            name = "curriculum_version_id",
            nullable = false
    )
    private UUID curriculumVersionId;


    @Column(
            name = "curriculum_subject_id"
    )
    private UUID curriculumSubjectId;


    @Column(
            name = "class_grade_id",
            nullable = false
    )
    private UUID classGradeId;


    @Column(
            name = "coverage_type",
            nullable = false,
            length = 40
    )
    private String coverageType;


    @Column(
            name = "coverage_item",
            nullable = false,
            length = 500
    )
    private String coverageItem;


    @Column(
            name = "planned_week"
    )
    private Integer plannedWeek;


    @Column(
            name = "coverage_status",
            nullable = false,
            length = 40
    )
    private String coverageStatus = "NOT_STARTED";


    @Column(
            name = "completion_date"
    )
    private LocalDate completionDate;


    @Column(
            name = "teacher_remarks",
            length = 1500
    )
    private String teacherRemarks;


    protected TeacherCoverage() {
    }


    public TeacherCoverage(
            UUID teacherProfileId,
            UUID teachingAssignmentId,
            UUID academicYearId,
            UUID curriculumVersionId,
            UUID classGradeId,
            String coverageType,
            String coverageItem
    ) {

        this.teacherProfileId = teacherProfileId;
        this.teachingAssignmentId = teachingAssignmentId;
        this.academicYearId = academicYearId;
        this.curriculumVersionId = curriculumVersionId;
        this.classGradeId = classGradeId;
        this.coverageType = coverageType;
        this.coverageItem = coverageItem;

    }


    public void markCompleted(
            LocalDate completionDate,
            String remarks
    ) {

        this.coverageStatus = "COMPLETED";
        this.completionDate = completionDate;
        this.teacherRemarks = remarks;

    }


    public void markInProgress() {

        this.coverageStatus = "IN_PROGRESS";

    }


    public void markRequiresRemediation() {

        this.coverageStatus = "REQUIRES_REMEDIATION";

    }


    public void markAheadOfSchedule() {

        this.coverageStatus = "AHEAD_OF_SCHEDULE";

    }


    public UUID getTeacherProfileId() {
        return teacherProfileId;
    }


    public UUID getTeachingAssignmentId() {
        return teachingAssignmentId;
    }


    public UUID getAcademicYearId() {
        return academicYearId;
    }


    public UUID getAcademicTermId() {
        return academicTermId;
    }


    public UUID getCurriculumVersionId() {
        return curriculumVersionId;
    }


    public UUID getCurriculumSubjectId() {
        return curriculumSubjectId;
    }


    public UUID getClassGradeId() {
        return classGradeId;
    }


    public String getCoverageType() {
        return coverageType;
    }


    public String getCoverageItem() {
        return coverageItem;
    }


    public Integer getPlannedWeek() {
        return plannedWeek;
    }


    public String getCoverageStatus() {
        return coverageStatus;
    }


    public LocalDate getCompletionDate() {
        return completionDate;
    }


    public String getTeacherRemarks() {
        return teacherRemarks;
    }

}
