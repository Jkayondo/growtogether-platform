package africa.growtogether.platform.school.admission.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AdmissionPaymentDtos {

    private AdmissionPaymentDtos() {
    }

    public record AllocatePaymentRequest(

            @NotNull
            UUID paymentTransactionId

    ) {
    }

    public record WaiverRequest(

            @NotNull
            @DecimalMin("0.01")
            BigDecimal waiverAmount,

            @NotBlank
            @Size(max = 1000)
            String reason

    ) {
    }

    public record ObligationView(

            UUID id,

            UUID admissionApplicationId,

            UUID feeItemId,

            String currencyCode,

            BigDecimal requiredAmount,

            BigDecimal waivedAmount,

            boolean requiredForOnboarding,

            String gateStatus,

            String waiverReason,

            Instant waivedAt,

            Instant satisfiedAt

    ) {
    }

    public record AllocationView(

            UUID id,

            UUID admissionPaymentObligationId,

            UUID eipPaymentTransactionId,

            BigDecimal allocatedAmount,

            String allocationStatus,

            Instant appliedAt,

            Instant reversedAt,

            String reversalReason

    ) {
    }

    public record AllocationResultView(

            AllocationView allocation,

            BigDecimal requiredAmount,

            BigDecimal waivedAmount,

            BigDecimal totalAllocatedAmount,

            BigDecimal outstandingAmount,

            String gateStatus

    ) {
    }

    public record WaiverResultView(

            ObligationView obligation,

            BigDecimal requiredAmount,

            BigDecimal allocatedAmount,

            BigDecimal waivedAmount,

            BigDecimal outstandingAmount,

            String gateStatus

    ) {
    }

    public record OnboardingGateView(

            UUID admissionApplicationId,

            boolean unlocked,

            int requiredObligationCount,

            int satisfiedObligationCount,

            List<UUID> blockingObligationIds

    ) {

        public OnboardingGateView {

            blockingObligationIds =
                    blockingObligationIds == null
                            ? List.of()
                            : List.copyOf(
                                    blockingObligationIds
                            );
        }
    }

    public record ReconciliationView(

            UUID paymentTransactionId,

            String paymentStatus,

            int affectedAllocationCount,

            List<UUID> affectedObligationIds,

            boolean manualReviewRequired

    ) {

        public ReconciliationView {

            affectedObligationIds =
                    affectedObligationIds == null
                            ? List.of()
                            : List.copyOf(
                                    affectedObligationIds
                            );
        }
    }

    public static ObligationView obligation(
            AdmissionPaymentObligation source
    ) {

        return new ObligationView(
                source.getId(),
                source.getAdmissionApplicationId(),
                source.getFeeItemId(),
                source.getCurrencyCode(),
                source.getRequiredAmount(),
                source.getWaivedAmount(),
                source.isRequiredForOnboarding(),
                source.getGateStatus().name(),
                source.getWaiverReason(),
                source.getWaivedAt(),
                source.getSatisfiedAt()
        );
    }

    public static AllocationView allocation(
            AdmissionPaymentAllocation source
    ) {

        return new AllocationView(
                source.getId(),
                source.getAdmissionPaymentObligationId(),
                source.getEipPaymentTransactionId(),
                source.getAllocatedAmount(),
                source.getAllocationStatus().name(),
                source.getAppliedAt(),
                source.getReversedAt(),
                source.getReversalReason()
        );
    }

    public static AllocationResultView allocationResult(
            AdmissionPaymentAllocationResult source
    ) {

        return new AllocationResultView(
                allocation(
                        source.allocation()
                ),
                source.requiredAmount(),
                source.waivedAmount(),
                source.totalAllocatedAmount(),
                source.outstandingAmount(),
                source.gateStatus().name()
        );
    }

    public static WaiverResultView waiverResult(
            AdmissionPaymentWaiverResult source
    ) {

        return new WaiverResultView(
                obligation(
                        source.obligation()
                ),
                source.requiredAmount(),
                source.allocatedAmount(),
                source.waivedAmount(),
                source.outstandingAmount(),
                source.gateStatus().name()
        );
    }

    public static OnboardingGateView onboardingGate(
            AdmissionOnboardingGateDecision source
    ) {

        return new OnboardingGateView(
                source.admissionApplicationId(),
                source.unlocked(),
                source.requiredObligationCount(),
                source.satisfiedObligationCount(),
                source.blockingObligationIds()
        );
    }

    public static ReconciliationView reconciliation(
            AdmissionPaymentReconciliationResult source
    ) {

        return new ReconciliationView(
                source.paymentTransactionId(),
                source.paymentStatus(),
                source.affectedAllocationCount(),
                source.affectedObligationIds(),
                source.manualReviewRequired()
        );
    }
}
