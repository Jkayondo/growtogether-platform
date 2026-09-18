
package africa.growtogether.platform.school.finance.allocation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class FinancePaymentAllocationDtos {

    private FinancePaymentAllocationDtos() {
    }

    public record CreateRequest(
            @NotNull UUID invoiceId,
            UUID invoiceLineId,
            UUID paymentInstallmentId,
            @NotNull
            @DecimalMin(value = "0.01")
            BigDecimal allocatedAmount
    ) {
    }

    public record AllocationResponse(
            UUID allocationId,
            UUID studentPaymentId,
            UUID invoiceId,
            UUID invoiceLineId,
            UUID paymentInstallmentId,
            BigDecimal allocatedAmount,
            Instant allocatedAt,
            UUID allocatedBy,
            String allocationStatus,
            String status
    ) {
    }
}
