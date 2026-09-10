package africa.growtogether.platform.school.assessment.examination.registration;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "gts_candidate_paper_registration")
public class CandidatePaperRegistration
        extends AuditedTenantEntity {


    @Column(
            name = "examination_candidate_id",
            nullable = false
    )
    private UUID examinationCandidateId;


    @Column(
            name = "assessment_paper_id",
            nullable = false
    )
    private UUID assessmentPaperId;


    @Column(
            name = "examination_schedule_id"
    )
    private UUID examinationScheduleId;


    @Column(
            name = "registration_type",
            nullable = false,
            length = 30
    )
    private String registrationType;


    @Column(
            name = "registration_status",
            nullable = false,
            length = 30
    )
    private String registrationStatus;


    @Column(
            name = "registered_at",
            nullable = false
    )
    private Instant registeredAt;


    @Column(
            name = "registered_by"
    )
    private UUID registeredBy;


    @Column(
            name = "withdrawn_at"
    )
    private Instant withdrawnAt;


    @Column(
            name = "withdrawal_reason",
            length = 1000
    )
    private String withdrawalReason;


    protected CandidatePaperRegistration() {
    }


    public CandidatePaperRegistration(
            UUID examinationCandidateId,
            UUID assessmentPaperId,
            UUID examinationScheduleId
    ) {

        this(
                examinationCandidateId,
                assessmentPaperId,
                examinationScheduleId,
                "STANDARD",
                null
        );
    }


    public CandidatePaperRegistration(
            UUID examinationCandidateId,
            UUID assessmentPaperId,
            UUID examinationScheduleId,
            String registrationType,
            UUID registeredBy
    ) {

        this.examinationCandidateId =
                examinationCandidateId;

        this.assessmentPaperId =
                assessmentPaperId;

        this.examinationScheduleId =
                examinationScheduleId;

        this.registrationType =
                registrationType;

        this.registrationStatus =
                "REGISTERED";

        this.registeredAt =
                Instant.now();

        this.registeredBy =
                registeredBy;
    }


    public UUID getExaminationCandidateId() {
        return examinationCandidateId;
    }


    public UUID getAssessmentPaperId() {
        return assessmentPaperId;
    }


    public UUID getExaminationScheduleId() {
        return examinationScheduleId;
    }


    public String getRegistrationType() {
        return registrationType;
    }


    public String getRegistrationStatus() {
        return registrationStatus;
    }


    public Instant getRegisteredAt() {
        return registeredAt;
    }


    public UUID getRegisteredBy() {
        return registeredBy;
    }


    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }


    public String getWithdrawalReason() {
        return withdrawalReason;
    }


    public void verify() {

        if (!"REGISTERED".equals(
                registrationStatus
        )) {

            throw new IllegalStateException(
                    "Only REGISTERED candidate paper registrations can be verified"
            );
        }

        this.registrationStatus =
                "VERIFIED";
    }


    public void complete() {

        if (!"VERIFIED".equals(
                registrationStatus
        )) {

            throw new IllegalStateException(
                    "Only VERIFIED candidate paper registrations can be completed"
            );
        }

        this.registrationStatus =
                "COMPLETED";
    }


    public void withdraw(
            String reason
    ) {

        if (
                !"REGISTERED".equals(
                        registrationStatus
                )
                && !"VERIFIED".equals(
                        registrationStatus
                )
        ) {

            throw new IllegalStateException(
                    "Only REGISTERED or VERIFIED candidate paper registrations can be withdrawn"
            );
        }


        if (
                reason == null
                || reason.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Withdrawal reason is required"
            );
        }


        this.registrationStatus =
                "WITHDRAWN";

        this.withdrawnAt =
                Instant.now();

        this.withdrawalReason =
                reason.trim();
    }

}
