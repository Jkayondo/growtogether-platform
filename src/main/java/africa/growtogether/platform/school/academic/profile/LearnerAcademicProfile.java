package africa.growtogether.platform.school.academic.profile;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "gts_learner_academic_profile")
public class LearnerAcademicProfile extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "current_academic_year_id"
    )
    private UUID currentAcademicYearId;


    @Column(
            name = "current_class_grade_id"
    )
    private UUID currentClassGradeId;


    @Column(
            name = "current_curriculum_version_id"
    )
    private UUID currentCurriculumVersionId;


    @Column(
            name = "academic_status",
            length = 50
    )
    private String academicStatus;


    @Column(
            name = "learning_stage",
            length = 50
    )
    private String learningStage;


    @Column(
            name = "profile_summary",
            columnDefinition = "text"
    )
    private String profileSummary;


    protected LearnerAcademicProfile() {
    }


    public LearnerAcademicProfile(
            UUID learnerId
    ) {

        this.learnerId = learnerId;
        this.academicStatus = "ACTIVE";
    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public UUID getCurrentAcademicYearId() {
        return currentAcademicYearId;
    }


    public UUID getCurrentClassGradeId() {
        return currentClassGradeId;
    }


    public UUID getCurrentCurriculumVersionId() {
        return currentCurriculumVersionId;
    }


    public String getAcademicStatus() {
        return academicStatus;
    }


    public String getLearningStage() {
        return learningStage;
    }


    public String getProfileSummary() {
        return profileSummary;
    }


    public void archive() {

        this.setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );

    }

}
