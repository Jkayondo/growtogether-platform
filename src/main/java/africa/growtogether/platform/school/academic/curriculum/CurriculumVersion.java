package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_curriculum_version")
public class CurriculumVersion extends AuditedTenantEntity {


    @ManyToOne
    @JoinColumn(
            name = "curriculum_id",
            nullable = false
    )
    private Curriculum curriculum;


    @Column(
            name = "version_code",
            nullable = false,
            length = 80
    )
    private String versionCode;


    @Column(
            name = "version_name",
            length = 200
    )
    private String versionName;


    @Column(
            name = "publication_date"
    )
    private LocalDate publicationDate;


    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom;


    @Column(
            name = "effective_to"
    )
    private LocalDate effectiveTo;


    @Column(
            name = "change_summary",
            length = 2000
    )
    private String changeSummary;


    @Column(
            name = "approval_reference",
            length = 160
    )
    private String approvalReference;


    @Column(
            name = "approved_at"
    )
    private Instant approvedAt;


    @Column(
            name = "approved_by"
    )
    private UUID approvedBy;


    @Column(
            name = "version_status",
            nullable = false,
            length = 30
    )
    private String versionStatus;


    protected CurriculumVersion() {
    }


    public CurriculumVersion(
            Curriculum curriculum,
            String versionCode,
            String versionName,
            LocalDate effectiveFrom
    ) {

        this.curriculum = curriculum;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.effectiveFrom = effectiveFrom;
        this.versionStatus = "DRAFT";
    }


    public Curriculum getCurriculum() {
        return curriculum;
    }


    public String getVersionCode() {
        return versionCode;
    }


    public String getVersionName() {
        return versionName;
    }


    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }


    public String getVersionStatus() {
        return versionStatus;
    }


    public void approve(
            UUID approvedBy,
            String approvalReference
    ) {

        if (!"UNDER_REVIEW".equals(versionStatus)
                && !"DRAFT".equals(versionStatus)) {

            throw new IllegalStateException(
                    "Only draft or review versions can be approved"
            );
        }


        this.versionStatus = "APPROVED";
        this.approvedBy = approvedBy;
        this.approvalReference = approvalReference;
        this.approvedAt = Instant.now();
    }


    public void activate() {

        if (!"APPROVED".equals(versionStatus)) {

            throw new IllegalStateException(
                    "Only approved curriculum versions can be activated"
            );
        }


        this.versionStatus = "ACTIVE";
    }

}
