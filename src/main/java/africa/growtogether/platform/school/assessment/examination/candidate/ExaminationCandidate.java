package africa.growtogether.platform.school.assessment.examination.candidate;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "gts_examination_candidate")
public class ExaminationCandidate extends AuditedTenantEntity {


    @Column(
            name = "candidate_number",
            nullable = false,
            length = 120
    )
    private String candidateNumber;


    @Column(
            name = "examination_session_id",
            nullable = false
    )
    private UUID examinationSessionId;


    @Column(
            name = "student_id",
            nullable = false
    )
    private UUID studentId;


    @Column(
            name = "student_enrollment_id",
            nullable = false
    )
    private UUID studentEnrollmentId;


    @Column(
            name = "registration_date",
            nullable = false
    )
    private LocalDate registrationDate;


    @Column(
            name = "external_candidate_number",
            length = 160
    )
    private String externalCandidateNumber;


    @Column(
            name = "eligibility_status",
            nullable = false,
            length = 30
    )
    private String eligibilityStatus;


    @Column(
            name = "eligibility_reason",
            length = 1000
    )
    private String eligibilityReason;


    @Column(
            name = "registered_by"
    )
    private UUID registeredBy;


    @Column(
            name = "verified_at"
    )
    private Instant verifiedAt;


    @Column(
            name = "verified_by"
    )
    private UUID verifiedBy;


    @Column(
            name = "workflow_instance_id"
    )
    private UUID workflowInstanceId;


    @Column(
            name = "candidate_status",
            nullable = false,
            length = 30
    )
    private String candidateStatus;


    protected ExaminationCandidate() {
    }


    public ExaminationCandidate(
            String candidateNumber,
            UUID examinationSessionId,
            UUID studentId,
            UUID studentEnrollmentId
    ) {

        this.candidateNumber = candidateNumber;
        this.examinationSessionId = examinationSessionId;
        this.studentId = studentId;
        this.studentEnrollmentId = studentEnrollmentId;

        this.registrationDate = LocalDate.now();
        this.eligibilityStatus = "PENDING";
        this.candidateStatus = "REGISTERED";

    }


    public String getCandidateNumber() {
        return candidateNumber;
    }


    public UUID getExaminationSessionId() {
        return examinationSessionId;
    }


    public UUID getStudentId() {
        return studentId;
    }


    public UUID getStudentEnrollmentId() {
        return studentEnrollmentId;
    }


    public String getEligibilityStatus() {
        return eligibilityStatus;
    }


    public String getCandidateStatus() {
        return candidateStatus;
    }


    public void markEligible() {

        this.eligibilityStatus = "ELIGIBLE";

    }


    public void markConditionallyEligible(
            String reason
    ) {

        this.eligibilityStatus = "CONDITIONALLY_ELIGIBLE";
        this.eligibilityReason = reason;

    }


    public void markIneligible(
            String reason
    ) {

        this.eligibilityStatus = "INELIGIBLE";
        this.eligibilityReason = reason;

    }


    public void verify(
            UUID verifiedBy
    ) {

        this.verifiedBy = verifiedBy;
        this.verifiedAt = Instant.now();
        this.candidateStatus = "VERIFIED";

    }


    public void complete() {

        this.candidateStatus = "COMPLETED";

    }


    public void withdraw() {

        this.candidateStatus = "WITHDRAWN";

    }

}
