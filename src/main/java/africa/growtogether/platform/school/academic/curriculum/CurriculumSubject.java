package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_curriculum_subject")
public class CurriculumSubject extends AuditedTenantEntity {


    @ManyToOne
    @JoinColumn(
            name = "curriculum_version_id",
            nullable = false
    )
    private CurriculumVersion curriculumVersion;


    @Column(name = "academic_programme_id")
    private UUID academicProgrammeId;


    @Column(name = "study_track_id")
    private UUID studyTrackId;


    @Column(
            name = "class_grade_id",
            nullable = false
    )
    private UUID classGradeId;


    @Column(
            name = "subject_id",
            nullable = false
    )
    private UUID subjectId;


    @Column(
            name = "subject_requirement",
            nullable = false,
            length = 30
    )
    private String subjectRequirement;


    @Column(name = "minimum_weekly_periods")
    private Integer minimumWeeklyPeriods;


    @Column(name = "maximum_weekly_periods")
    private Integer maximumWeeklyPeriods;


    @Column(name = "recommended_weekly_periods")
    private Integer recommendedWeeklyPeriods;


    @Column(name = "credit_value")
    private BigDecimal creditValue;


    @Column(name = "pass_mark")
    private BigDecimal passMark;


    @Column(name = "effective_from")
    private LocalDate effectiveFrom;


    @Column(name = "effective_to")
    private LocalDate effectiveTo;


    protected CurriculumSubject() {
    }


    public CurriculumSubject(
            CurriculumVersion curriculumVersion,
            UUID classGradeId,
            UUID subjectId
    ) {

        this.curriculumVersion = curriculumVersion;
        this.classGradeId = classGradeId;
        this.subjectId = subjectId;
        this.subjectRequirement = "CORE";
    }


    public CurriculumVersion getCurriculumVersion() {
        return curriculumVersion;
    }


    public UUID getClassGradeId() {
        return classGradeId;
    }


    public UUID getSubjectId() {
        return subjectId;
    }


    public String getSubjectRequirement() {
        return subjectRequirement;
    }


    public Integer getRecommendedWeeklyPeriods() {
        return recommendedWeeklyPeriods;
    }


    public BigDecimal getCreditValue() {
        return creditValue;
    }


    public BigDecimal getPassMark() {
        return passMark;
    }


    public void changeRequirement(
            String requirement
    ) {

        this.subjectRequirement = requirement;
    }


}
