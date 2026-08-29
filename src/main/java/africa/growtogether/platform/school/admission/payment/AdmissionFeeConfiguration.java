package africa.growtogether.platform.school.admission.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AdmissionFeeConfiguration(

        UUID feeItemId,

        String itemCode,

        String itemName,

        String currencyCode,

        BigDecimal unitAmount,

        BigDecimal quantity,

        boolean partialPaymentAllowed,

        boolean refundable,

        LocalDate dueDate

) {

    public AdmissionFeeConfiguration {

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
                unitAmount == null
                || unitAmount.signum() < 0
        ) {
            throw new IllegalArgumentException(
                    "unitAmount must not be negative"
            );
        }

        if (
                quantity == null
                || quantity.signum() <= 0
        ) {
            throw new IllegalArgumentException(
                    "quantity must be positive"
            );
        }
    }

    public BigDecimal totalAmount() {

        return unitAmount
                .multiply(
                        quantity
                );
    }
}
