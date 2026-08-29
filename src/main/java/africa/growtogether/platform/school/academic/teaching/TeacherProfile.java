package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gts_teacher_profile")
public class TeacherProfile extends AuditedTenantEntity {

    @Column(
            name = "workforce_member_id",
            nullable = false
    )
    private UUID workforceMemberId;

    @Column(
            name = "teacher_number",
            nullable = false,
            length = 80
    )
    private String teacherNumber;

    @Column(
            name = "teacher_registration_number",
            length = 120
    )
    private String teacherRegistrationNumber;

    @Column(
            name = "teaching_licence_number",
            length = 120
    )
    private String teachingLicenceNumber;

    @Column(name = "teaching_licence_issued_at")
    private LocalDate teachingLicenceIssuedAt;

    @Column(name = "teaching_licence_expires_at")
    private LocalDate teachingLicenceExpiresAt;

    @Column(
            name = "highest_teaching_level",
            length = 80
    )
    private String highestTeachingLevel;

    @Column(
            name = "primary_specialization",
            length = 200
    )
    private String primarySpecialization;

    @Column(
            name = "secondary_specialization",
            length = 200
    )
    private String secondarySpecialization;

    @Column(
            name = "teacher_category",
            nullable = false,
            length = 40
    )
    private String teacherCategory = "CLASSROOM_TEACHER";

    @Column(
            name = "teaching_status",
            nullable = false,
            length = 30
    )
    private String teachingStatus = "ACTIVE";

    @Column(
            name = "qualified_for_boarding_duty",
            nullable = false
    )
    private boolean qualifiedForBoardingDuty;

    @Column(
            name = "qualified_for_special_needs",
            nullable = false
    )
    private boolean qualifiedForSpecialNeeds;

    @Column(
            name = "qualified_for_counselling",
            nullable = false
    )
    private boolean qualifiedForCounselling;

    @Column(name = "maximum_weekly_periods")
    private Integer maximumWeeklyPeriods;

    @Column(
            name = "notes",
            length = 1500
    )
    private String notes;

    protected TeacherProfile() {
    }

    public TeacherProfile(
            UUID workforceMemberId,
            String teacherNumber,
            String teacherRegistrationNumber,
            String teachingLicenceNumber,
            LocalDate teachingLicenceIssuedAt,
            LocalDate teachingLicenceExpiresAt,
            String highestTeachingLevel,
            String primarySpecialization,
            String secondarySpecialization,
            String teacherCategory,
            boolean qualifiedForBoardingDuty,
            boolean qualifiedForSpecialNeeds,
            boolean qualifiedForCounselling,
            Integer maximumWeeklyPeriods,
            String notes
    ) {
        this.workforceMemberId = workforceMemberId;
        this.teacherNumber = teacherNumber;
        this.teacherRegistrationNumber = teacherRegistrationNumber;
        this.teachingLicenceNumber = teachingLicenceNumber;
        this.teachingLicenceIssuedAt = teachingLicenceIssuedAt;
        this.teachingLicenceExpiresAt = teachingLicenceExpiresAt;
        this.highestTeachingLevel = highestTeachingLevel;
        this.primarySpecialization = primarySpecialization;
        this.secondarySpecialization = secondarySpecialization;

        if (teacherCategory != null && !teacherCategory.isBlank()) {
            this.teacherCategory = teacherCategory;
        }

        this.qualifiedForBoardingDuty = qualifiedForBoardingDuty;
        this.qualifiedForSpecialNeeds = qualifiedForSpecialNeeds;
        this.qualifiedForCounselling = qualifiedForCounselling;
        this.maximumWeeklyPeriods = maximumWeeklyPeriods;
        this.notes = notes;
    }

    public UUID getWorkforceMemberId() {
        return workforceMemberId;
    }

    public String getTeacherNumber() {
        return teacherNumber;
    }

    public String getTeacherRegistrationNumber() {
        return teacherRegistrationNumber;
    }

    public String getTeachingLicenceNumber() {
        return teachingLicenceNumber;
    }

    public LocalDate getTeachingLicenceIssuedAt() {
        return teachingLicenceIssuedAt;
    }

    public LocalDate getTeachingLicenceExpiresAt() {
        return teachingLicenceExpiresAt;
    }

    public String getHighestTeachingLevel() {
        return highestTeachingLevel;
    }

    public String getPrimarySpecialization() {
        return primarySpecialization;
    }

    public String getSecondarySpecialization() {
        return secondarySpecialization;
    }

    public String getTeacherCategory() {
        return teacherCategory;
    }

    public String getTeachingStatus() {
        return teachingStatus;
    }

    public boolean isQualifiedForBoardingDuty() {
        return qualifiedForBoardingDuty;
    }

    public boolean isQualifiedForSpecialNeeds() {
        return qualifiedForSpecialNeeds;
    }

    public boolean isQualifiedForCounselling() {
        return qualifiedForCounselling;
    }

    public Integer getMaximumWeeklyPeriods() {
        return maximumWeeklyPeriods;
    }

    public String getNotes() {
        return notes;
    }

    public void activate() {
        setStatus(EntityStatus.ACTIVE);
        teachingStatus = "ACTIVE";
    }

    public void deactivate() {
        setStatus(EntityStatus.INACTIVE);
        teachingStatus = "INACTIVE";
    }
}
