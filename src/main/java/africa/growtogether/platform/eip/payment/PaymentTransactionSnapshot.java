package africa.growtogether.platform.eip.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentTransactionSnapshot(

        UUID id,

        String merchantReference,

        BigDecimal amount,

        String currency,

        String status,

        String providerReference,

        Instant completedAt

) {

    public boolean isSucceeded() {
        return "SUCCEEDED".equals(status);
    }

    public boolean isReversed() {
        return "REVERSED".equals(status);
    }

    public boolean isRefunded() {
        return "REFUNDED".equals(status);
    }

    public boolean isPartiallyRefunded() {
        return "PARTIALLY_REFUNDED".equals(status);
    }

    public boolean isFinalNegativeState() {
        return "FAILED".equals(status)
                || "CANCELLED".equals(status)
                || isReversed()
                || isRefunded();
    }
}
