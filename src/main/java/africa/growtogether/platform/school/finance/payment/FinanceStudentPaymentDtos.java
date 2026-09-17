package africa.growtogether.platform.school.finance.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class FinanceStudentPaymentDtos {

    private FinanceStudentPaymentDtos() {
    }

    public record CreateRequest(
            UUID studentFinancialAccountId,
        BigDecimal amount,
        String paymentMethod,
        String paymentReference,
        Instant paidAt
    ) {
    }

    public record PaymentResponse(
            UUID id,
        UUID tenantId,
        UUID studentFinancialAccountId,
        UUID studentId,
        BigDecimal amount,
        String currencyCode,
        String paymentMethod,
        String paymentReference,
        String status,
        Instant paidAt,
        UUID eipPaymentTransactionId,
        Instant createdAt,
        Instant updatedAt
    ) {
    }
}
