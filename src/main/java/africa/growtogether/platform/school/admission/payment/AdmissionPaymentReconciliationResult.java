package africa.growtogether.platform.school.admission.payment;

import java.util.List;
import java.util.UUID;

public record AdmissionPaymentReconciliationResult(

        UUID paymentTransactionId,

        String paymentStatus,

        int affectedAllocationCount,

        List<UUID> affectedObligationIds,

        boolean manualReviewRequired

) {

    public AdmissionPaymentReconciliationResult {

        affectedObligationIds =
                affectedObligationIds == null
                        ? List.of()
                        : List.copyOf(
                                affectedObligationIds
                        );
    }
}
