package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gts_admission_payment_gate_history")
public class AdmissionPaymentGateHistory
        extends AuditedTenantEntity {

    @Column(
            name = "admission_payment_obligation_id",
            nullable = false
    )
    private UUID admissionPaymentObligationId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "previous_gate_status",
            length = 30
    )
    private AdmissionPaymentGateStatus previousGateStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "new_gate_status",
            nullable = false,
            length = 30
    )
    private AdmissionPaymentGateStatus newGateStatus;

    @Column(
            name = "reason",
            length = 1000
    )
    private String reason;

    @Column(
            name = "changed_at",
            nullable = false
    )
    private Instant changedAt;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "correlation_id")
    private String correlationId;

    protected AdmissionPaymentGateHistory() {
    }

    public AdmissionPaymentGateHistory(
            UUID admissionPaymentObligationId,
            AdmissionPaymentGateStatus previousGateStatus,
            AdmissionPaymentGateStatus newGateStatus,
            String reason,
            UUID changedBy,
            String correlationId
    ) {

        if (admissionPaymentObligationId == null) {
            throw new IllegalArgumentException(
                    "admissionPaymentObligationId must not be null"
            );
        }

        if (newGateStatus == null) {
            throw new IllegalArgumentException(
                    "newGateStatus must not be null"
            );
        }

        this.admissionPaymentObligationId =
                admissionPaymentObligationId;

        this.previousGateStatus =
                previousGateStatus;

        this.newGateStatus =
                newGateStatus;

        this.reason =
                reason == null
                        || reason.isBlank()
                        ? null
                        : reason.trim();

        this.changedAt =
                Instant.now();

        this.changedBy =
                changedBy;

        this.correlationId =
                correlationId;
    }

    public UUID getAdmissionPaymentObligationId() {
        return admissionPaymentObligationId;
    }

    public AdmissionPaymentGateStatus getPreviousGateStatus() {
        return previousGateStatus;
    }

    public AdmissionPaymentGateStatus getNewGateStatus() {
        return newGateStatus;
    }

    public String getReason() {
        return reason;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
