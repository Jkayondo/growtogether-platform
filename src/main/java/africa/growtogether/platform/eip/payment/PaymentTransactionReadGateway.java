package africa.growtogether.platform.eip.payment;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class PaymentTransactionReadGateway {

    private final PaymentTransactionRepository transactions;

    public PaymentTransactionReadGateway(
            PaymentTransactionRepository transactions
    ) {
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public PaymentTransactionSnapshot require(
            UUID tenantId,
            UUID paymentTransactionId
    ) {

        validate(
                tenantId,
                paymentTransactionId
        );

        PaymentTransaction payment =
                transactions
                        .findByTenantIdAndId(
                                tenantId,
                                paymentTransactionId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Payment transaction not found for tenant"
                                )
                        );

        return snapshot(
                payment
        );
    }

    @Transactional
    public PaymentTransactionSnapshot requireForUpdate(
            UUID tenantId,
            UUID paymentTransactionId
    ) {

        validate(
                tenantId,
                paymentTransactionId
        );

        PaymentTransaction payment =
                transactions
                        .findForUpdate(
                                tenantId,
                                paymentTransactionId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Payment transaction not found for tenant"
                                )
                        );

        return snapshot(
                payment
        );
    }

    private void validate(
            UUID tenantId,
            UUID paymentTransactionId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (paymentTransactionId == null) {
            throw new IllegalArgumentException(
                    "paymentTransactionId must not be null"
            );
        }
    }

    private PaymentTransactionSnapshot snapshot(
            PaymentTransaction payment
    ) {

        return new PaymentTransactionSnapshot(
                payment.id(),
                payment.merchantReference(),
                payment.amount(),
                payment.currency(),
                payment.status().name(),
                payment.providerReference(),
                payment.completedAt()
        );
    }
}
