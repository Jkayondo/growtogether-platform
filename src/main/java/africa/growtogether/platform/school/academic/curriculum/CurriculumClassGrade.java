package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_curriculum_class_grade")
public class CurriculumClassGrade extends AuditedTenantEntity {


    @ManyToOne
    @JoinColumn(
            name = "curriculum_version_id",
            nullable = false
    )
    private CurriculumVersion curriculumVersion;


    @Column(
            name = "academic_programme_id"
    )
    private UUID academicProgrammeId;


    @Column(
            name = "study_track_id"
    )
    private UUID studyTrackId;


    @Column(
            name = "class_grade_id",
            nullable = false
    )
    private UUID classGradeId;


    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber;


    @Column(
            name = "minimum_age"
    )
    private Integer minimumAge;


    @Column(
            name = "maximum_age"
    )
    private Integer maximumAge;


    @Column(
            name = "mandatory_stage",
            nullable = false
    )
    private boolean mandatoryStage;


    @Column(
            name = "effective_from"
    )
    private LocalDate effectiveFrom;


    @Column(
            name = "effective_to"
    )
    private LocalDate effectiveTo;



    protected CurriculumClassGrade() {
    }


    public CurriculumClassGrade(
            CurriculumVersion curriculumVersion,
            UUID classGradeId,
            Integer sequenceNumber
    ) {

        this.curriculumVersion = curriculumVersion;
        this.classGradeId = classGradeId;
        this.sequenceNumber = sequenceNumber;
        this.mandatoryStage = true;
    }


    public CurriculumVersion getCurriculumVersion() {
        return curriculumVersion;
    }


    public UUID getClassGradeId() {
        return classGradeId;
    }


    public Integer getSequenceNumber() {
        return sequenceNumber;
    }


    public Integer getMinimumAge() {
        return minimumAge;
    }


    public Integer getMaximumAge() {
        return maximumAge;
    }


    public boolean isMandatoryStage() {
        return mandatoryStage;
    }


    public void archive() {

        setStatus(
            africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
    );
    }

}
