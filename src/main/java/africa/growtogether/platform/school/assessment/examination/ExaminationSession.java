package africa.growtogether.platform.school.assessment.examination;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "gts_examination_session")
public class ExaminationSession extends AuditedTenantEntity {


    @Column(
            name = "session_code",
            nullable = false,
            length = 100
    )
    private String sessionCode;


    @Column(
            name = "session_name",
            nullable = false,
            length = 250
    )
    private String sessionName;


    @Column(
            name = "description",
            length = 1500
    )
    private String description;


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
            name = "campus_id",
            nullable = false
    )
    private UUID campusId;


    @Column(
            name = "examination_type",
            nullable = false,
            length = 40
    )
    private String examinationType;


    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;


    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDate endDate;


    @Column(
            name = "registration_open_date"
    )
    private LocalDate registrationOpenDate;


    @Column(
            name = "registration_close_date"
    )
    private LocalDate registrationCloseDate;


    @Column(
            name = "external_authority",
            length = 250
    )
    private String externalAuthority;


    @Column(
            name = "external_session_reference",
            length = 160
    )
    private String externalSessionReference;


    @Column(
            name = "workflow_instance_id"
    )
    private UUID workflowInstanceId;


    @Column(
            name = "approved_at"
    )
    private Instant approvedAt;


    @Column(
            name = "approved_by"
    )
    private UUID approvedBy;


    @Column(
            name = "session_status",
            nullable = false,
            length = 30
    )
    private String sessionStatus;


    protected ExaminationSession() {
    }


    public ExaminationSession(
            String sessionCode,
            String sessionName,
            UUID academicYearId,
            UUID campusId,
            String examinationType,
            LocalDate startDate,
            LocalDate endDate
    ) {

        this.sessionCode = sessionCode;
        this.sessionName = sessionName;
        this.academicYearId = academicYearId;
        this.campusId = campusId;
        this.examinationType = examinationType;
        this.startDate = startDate;
        this.endDate = endDate;

        this.sessionStatus = "DRAFT";
    }


    public String getSessionCode() {
        return sessionCode;
    }


    public String getSessionName() {
        return sessionName;
    }


    public UUID getAcademicYearId() {
        return academicYearId;
    }


    public UUID getAcademicTermId() {
        return academicTermId;
    }


    public UUID getCampusId() {
        return campusId;
    }


    public String getExaminationType() {
        return examinationType;
    }


    public LocalDate getStartDate() {
        return startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }


    public String getSessionStatus() {
        return sessionStatus;
    }


    public void approve(
            UUID approvedBy
    ) {

        this.approvedBy = approvedBy;
        this.approvedAt = Instant.now();
        this.sessionStatus = "APPROVED";
    }


    public void openRegistration() {

        this.sessionStatus = "REGISTRATION_OPEN";

    }


    public void activate() {

        this.sessionStatus = "ACTIVE";

    }


    public void complete() {

        this.sessionStatus = "COMPLETED";

    }

}
