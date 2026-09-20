package africa.growtogether.platform.school.finance.receipt;

import static africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.ReceiptResponse;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancePaymentReceiptService {

    private final FinancePaymentReceiptJdbcRepository repository;
    private final FinancePaymentReceiptNumberService numbers;
    private final EnterpriseIdentityContext identity;

    public FinancePaymentReceiptService(
            FinancePaymentReceiptJdbcRepository repository,
            FinancePaymentReceiptNumberService numbers,
            EnterpriseIdentityContext identity
    ) {
        this.repository = repository;
        this.numbers = numbers;
        this.identity = identity;
    }

    @Transactional
    public ReceiptResponse issue(
            UUID tenantId,
            UUID paymentId
    ) {

        requireTenant(tenantId);

        if (paymentId == null) {
            throw new IllegalArgumentException(
                    "paymentId must not be null"
            );
        }

        /*
         * Durable idempotency:
         * repeat issuance returns the existing receipt.
         */
        var existing =
                repository.findByPayment(
                        tenantId,
                        paymentId
                );

        if (existing.isPresent()) {
            return existing.get();
        }

        /*
         * The authoritative payment row is locked before
         * allocating a receipt number.
         */
        var payment =
                repository.lockReceiptablePayment(
                        tenantId,
                        paymentId
                );

        /*
         * Recheck after acquiring the payment lock.
         * Concurrent issuance attempts therefore converge
         * on the same durable receipt identity.
         */
        existing =
                repository.findByPayment(
                        tenantId,
                        paymentId
                );

        if (existing.isPresent()) {
            return existing.get();
        }

        UUID actorId =
                identity.requireUserId();

        if (actorId == null) {
            throw new IllegalStateException(
                    "Authenticated actor user ID is required"
            );
        }

        String receiptNumber =
                numbers.next(tenantId);

        Instant issuedAt =
                Instant.now();

        ReceiptResponse issued =
                repository.insert(
                        tenantId,
                        payment,
                        receiptNumber,
                        actorId,
                        issuedAt
                );

        repository.appendIssuedHistory(
                tenantId,
                issued.receiptId(),
                actorId,
                issuedAt,
                issued.receiptNumber()
        );

        return issued;
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getByPayment(
            UUID tenantId,
            UUID paymentId
    ) {

        requireTenant(tenantId);

        if (paymentId == null) {
            throw new IllegalArgumentException(
                    "paymentId must not be null"
            );
        }

        return repository
                .findByPayment(
                        tenantId,
                        paymentId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Receipt was not found"
                                )
                );
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getById(
            UUID tenantId,
            UUID receiptId
    ) {

        requireTenant(tenantId);

        if (receiptId == null) {
            throw new IllegalArgumentException(
                    "receiptId must not be null"
            );
        }

        return repository
                .findById(
                        tenantId,
                        receiptId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Receipt was not found"
                                )
                );
    }

    private void requireTenant(UUID tenantId) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "X-Tenant-ID is required"
            );
        }

        UUID authenticatedTenant =
                identity.requireTenantId();

        if (
                authenticatedTenant == null
                || !tenantId.equals(authenticatedTenant)
        ) {
            throw new IllegalArgumentException(
                    "X-Tenant-ID does not match "
                    + "the authenticated tenant"
            );
        }
    }
}
