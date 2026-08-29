package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "gts_admission_payment_obligation")
public class AdmissionPaymentObligation
        extends AuditedTenantEntity {

    @Column(
            name = "admission_application_id",
            nullable = false
    )
    private UUID admissionApplicationId;

    @Column(
            name = "fee_item_id",
            nullable = false
    )
    private UUID feeItemId;

    @Column(
            name = "currency_code",
            nullable = false,
            length = 3
    )
    private String currencyCode;

    @Column(
            name = "required_amount",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal requiredAmount;

    @Column(
            name = "waived_amount",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal waivedAmount =
            BigDecimal.ZERO;

    @Column(
            name = "required_for_onboarding",
            nullable = false
    )
    private boolean requiredForOnboarding = true;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "gate_status",
            nullable = false,
            length = 30
    )
    private AdmissionPaymentGateStatus gateStatus =
            AdmissionPaymentGateStatus.PAYMENT_REQUIRED;

    @Column(
            name = "waiver_reason",
            length = 1000
    )
    private String waiverReason;

    @Column(name = "waived_at")
    private Instant waivedAt;

    @Column(name = "waived_by")
    private UUID waivedBy;

    @Column(name = "satisfied_at")
    private Instant satisfiedAt;

    @Column(name = "satisfied_by")
    private UUID satisfiedBy;

    protected AdmissionPaymentObligation() {
    }

    public AdmissionPaymentObligation(
            UUID admissionApplicationId,
            UUID feeItemId,
            String currencyCode,
            BigDecimal requiredAmount,
            boolean requiredForOnboarding
    ) {

        if (admissionApplicationId == null) {
            throw new IllegalArgumentException(
                    "admissionApplicationId must not be null"
            );
        }

        if (feeItemId == null) {
            throw new IllegalArgumentException(
                    "feeItemId must not be null"
            );
        }

        if (
                currencyCode == null
                || currencyCode.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "currencyCode must not be blank"
            );
        }

        if (
                requiredAmount == null
                || requiredAmount.signum() < 0
        ) {
            throw new IllegalArgumentException(
                    "requiredAmount must not be negative"
            );
        }

        String normalizedCurrency =
                currencyCode
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (normalizedCurrency.length() != 3) {
            throw new IllegalArgumentException(
                    "currencyCode must be ISO-4217"
            );
        }

        this.admissionApplicationId =
                admissionApplicationId;

        this.feeItemId =
                feeItemId;

        this.currencyCode =
                normalizedCurrency;

        this.requiredAmount =
                requiredAmount;

        this.requiredForOnboarding =
                requiredForOnboarding;

        this.waivedAmount =
                BigDecimal.ZERO;

        this.gateStatus =
                requiredAmount.signum() == 0
                        ? AdmissionPaymentGateStatus.SATISFIED
                        : AdmissionPaymentGateStatus.PAYMENT_REQUIRED;

        if (
                this.gateStatus
                        == AdmissionPaymentGateStatus.SATISFIED
        ) {
            this.satisfiedAt =
                    Instant.now();
        }
    }

    public void applyWaiver(
            BigDecimal amount,
            String reason,
            UUID actorId
    ) {

        if (
                amount == null
                || amount.signum() <= 0
        ) {
            throw new IllegalArgumentException(
                    "waiver amount must be positive"
            );
        }

        if (
                reason == null
                || reason.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "waiver reason must not be blank"
            );
        }

        if (actorId == null) {
            throw new IllegalArgumentException(
                    "waiver actorId must not be null"
            );
        }

        if (
                amount.compareTo(
                        requiredAmount
                ) > 0
        ) {
            throw new IllegalArgumentException(
                    "waiver amount cannot exceed required amount"
            );
        }

        this.waivedAmount =
                amount;

        this.waiverReason =
                reason.trim();

        this.waivedAt =
                Instant.now();

        this.waivedBy =
                actorId;
    }

    public void markFullyWaived(
            UUID actorId
    ) {

        if (actorId == null) {
            throw new IllegalArgumentException(
                    "waiver actorId must not be null"
            );
        }

        /*
         * IMPROVEMENT:
         * WAIVED is a consequential financial state.
         * It must be backed by complete waiver evidence rather than
         * being reachable through this state-transition method alone.
         */
        if (
                waivedAmount == null
                || waivedAmount.compareTo(
                        requiredAmount
                ) != 0
                || waiverReason == null
                || waiverReason.isBlank()
                || waivedAt == null
                || waivedBy == null
        ) {
            throw new IllegalStateException(
                    "Admission payment obligation cannot be marked fully waived without full waiver evidence"
            );
        }

        this.gateStatus =
                AdmissionPaymentGateStatus.WAIVED;

        this.satisfiedAt =
                Instant.now();

        this.satisfiedBy =
                actorId;
    }

    public void markReviewRequired() {

        this.gateStatus =
                AdmissionPaymentGateStatus.REVIEW_REQUIRED;

        /*
         * A previously satisfied obligation must no longer present
         * itself as financially cleared while its payment evidence
         * requires review.
         */
        this.satisfiedAt =
                null;

        this.satisfiedBy =
                null;
    }

    public void updateFromPaymentEvaluation(
            AdmissionPaymentGateStatus newStatus,
            UUID actorId
    ) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "newStatus must not be null"
            );
        }

        if (
                newStatus != AdmissionPaymentGateStatus.PAYMENT_REQUIRED
                && newStatus != AdmissionPaymentGateStatus.PAYMENT_PENDING
                && newStatus != AdmissionPaymentGateStatus.PARTIALLY_SATISFIED
                && newStatus != AdmissionPaymentGateStatus.SATISFIED
        ) {
            throw new IllegalArgumentException(
                    "Invalid payment evaluation status: "
                            + newStatus
            );
        }

        this.gateStatus =
                newStatus;

        if (
                newStatus
                        == AdmissionPaymentGateStatus.SATISFIED
        ) {
            this.satisfiedAt =
                    Instant.now();

            this.satisfiedBy =
                    actorId;
        } else {
            this.satisfiedAt =
                    null;

            this.satisfiedBy =
                    null;
        }
    }

    public UUID getAdmissionApplicationId() {
        return admissionApplicationId;
    }

    public UUID getFeeItemId() {
        return feeItemId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getRequiredAmount() {
        return requiredAmount;
    }

    public BigDecimal getWaivedAmount() {
        return waivedAmount;
    }

    public boolean isRequiredForOnboarding() {
        return requiredForOnboarding;
    }

    public AdmissionPaymentGateStatus getGateStatus() {
        return gateStatus;
    }

    public String getWaiverReason() {
        return waiverReason;
    }

    public Instant getWaivedAt() {
        return waivedAt;
    }

    public UUID getWaivedBy() {
        return waivedBy;
    }

    public Instant getSatisfiedAt() {
        return satisfiedAt;
    }

    public UUID getSatisfiedBy() {
        return satisfiedBy;
    }
}
