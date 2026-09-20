package africa.growtogether.platform.school.finance.receipt;

import static africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.ReceiptResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FinancePaymentReceiptServiceContractTest {

    private static final UUID TENANT =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID OTHER_TENANT =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private static final UUID ACTOR =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private static final UUID PAYMENT =
            UUID.fromString(
                    "44444444-4444-4444-4444-444444444444"
            );

    private static final UUID STUDENT =
            UUID.fromString(
                    "55555555-5555-5555-5555-555555555555"
            );

    private static final UUID RECEIPT =
            UUID.fromString(
                    "66666666-6666-6666-6666-666666666666"
            );

    private FinancePaymentReceiptJdbcRepository repository;
    private FinancePaymentReceiptNumberService numbers;
    private EnterpriseIdentityContext identity;
    private FinancePaymentReceiptService service;

    @BeforeEach
    void setUp() {

        repository =
                Mockito.mock(
                        FinancePaymentReceiptJdbcRepository.class
                );

        numbers =
                Mockito.mock(
                        FinancePaymentReceiptNumberService.class
                );

        identity =
                Mockito.mock(
                        EnterpriseIdentityContext.class
                );

        service =
                new FinancePaymentReceiptService(
                        repository,
                        numbers,
                        identity
                );
    }

    @Test
    void repeatIssueReturnsSameDurableReceiptWithoutNewNumber() {

        when(
                identity.requireTenantId()
        ).thenReturn(
                TENANT
        );

        ReceiptResponse existing =
                receipt();

        when(
                repository.findByPayment(
                        TENANT,
                        PAYMENT
                )
        ).thenReturn(
                Optional.of(existing)
        );

        ReceiptResponse result =
                service.issue(
                        TENANT,
                        PAYMENT
                );

        assertSame(
                existing,
                result
        );

        verifyNoInteractions(numbers);

        verify(
                repository,
                never()
        ).lockReceiptablePayment(
                TENANT,
                PAYMENT
        );

        verify(
                identity,
                never()
        ).requireUserId();
    }

    @Test
    void newIssueLocksPaymentBeforeCreatingReceiptAndRecordsHistory() {

        when(
                identity.requireTenantId()
        ).thenReturn(
                TENANT
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                ACTOR
        );

        FinancePaymentReceiptJdbcRepository.PaymentFacts payment =
                payment();

        when(
                repository.findByPayment(
                        TENANT,
                        PAYMENT
                )
        ).thenReturn(
                Optional.empty(),
                Optional.empty()
        );

        when(
                repository.lockReceiptablePayment(
                        TENANT,
                        PAYMENT
                )
        ).thenReturn(
                payment
        );

        when(
                numbers.next(TENANT)
        ).thenReturn(
                "RCT-0000000001"
        );

        ReceiptResponse issued =
                receipt();

        when(
                repository.insert(
                        eq(TENANT),
                        eq(payment),
                        eq("RCT-0000000001"),
                        eq(ACTOR),
                        any(Instant.class)
                )
        ).thenReturn(
                issued
        );

        ReceiptResponse result =
                service.issue(
                        TENANT,
                        PAYMENT
                );

        assertSame(
                issued,
                result
        );

        verify(
                repository
        ).lockReceiptablePayment(
                TENANT,
                PAYMENT
        );

        verify(
                numbers
        ).next(
                TENANT
        );

        verify(
                repository
        ).appendIssuedHistory(
                eq(TENANT),
                eq(RECEIPT),
                eq(ACTOR),
                any(Instant.class),
                eq("RCT-0000000001")
        );
    }

    @Test
    void postLockRecheckReturnsConcurrentReceiptWithoutConsumingNumber() {

        when(
                identity.requireTenantId()
        ).thenReturn(
                TENANT
        );

        ReceiptResponse concurrent =
                receipt();

        when(
                repository.findByPayment(
                        TENANT,
                        PAYMENT
                )
        ).thenReturn(
                Optional.empty(),
                Optional.of(concurrent)
        );

        when(
                repository.lockReceiptablePayment(
                        TENANT,
                        PAYMENT
                )
        ).thenReturn(
                payment()
        );

        ReceiptResponse result =
                service.issue(
                        TENANT,
                        PAYMENT
                );

        assertSame(
                concurrent,
                result
        );

        verifyNoInteractions(numbers);

        verify(
                identity,
                never()
        ).requireUserId();
    }

    @Test
    void crossTenantAccessIsRejectedBeforeRepositoryAccess() {

        when(
                identity.requireTenantId()
        ).thenReturn(
                OTHER_TENANT
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.getByPayment(
                                TENANT,
                                PAYMENT
                        )
        );

        verify(
                repository,
                never()
        ).findByPayment(
                TENANT,
                PAYMENT
        );

        verifyNoInteractions(numbers);
    }

    @Test
    void retrievalDoesNotGenerateAnotherReceiptNumber() {

        when(
                identity.requireTenantId()
        ).thenReturn(
                TENANT
        );

        when(
                repository.findByPayment(
                        TENANT,
                        PAYMENT
                )
        ).thenReturn(
                Optional.of(receipt())
        );

        ReceiptResponse result =
                service.getByPayment(
                        TENANT,
                        PAYMENT
                );

        assertEquals(
                RECEIPT,
                result.receiptId()
        );

        assertEquals(
                "RCT-0000000001",
                result.receiptNumber()
        );

        verifyNoInteractions(numbers);
    }

    @Test
    void unknownReceiptIsRejectedWithoutNumberGeneration() {

        when(
                identity.requireTenantId()
        ).thenReturn(
                TENANT
        );

        when(
                repository.findById(
                        TENANT,
                        RECEIPT
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.getById(
                                TENANT,
                                RECEIPT
                        )
        );

        verifyNoInteractions(numbers);
    }

    private static FinancePaymentReceiptJdbcRepository.PaymentFacts payment() {

        return new FinancePaymentReceiptJdbcRepository.PaymentFacts(
                PAYMENT,
                STUDENT,
                "PAY-001",
                Instant.parse(
                        "2026-09-19T10:15:30Z"
                ),
                "UGX",
                new BigDecimal(
                        "150000.00"
                ),
                "CASH",
                null,
                null,
                null,
                "RECEIVED",
                "ACTIVE"
        );
    }

    private static ReceiptResponse receipt() {

        return new ReceiptResponse(
                RECEIPT,
                PAYMENT,
                STUDENT,
                "RCT-0000000001",
                Instant.parse(
                        "2026-09-19T10:15:30Z"
                ),
                "UGX",
                new BigDecimal(
                        "150000.00"
                ),
                Instant.parse(
                        "2026-09-19T10:16:00Z"
                ),
                ACTOR,
                null,
                null,
                null,
                "ISSUED",
                "ACTIVE",
                "PAY-001",
                "CASH",
                null,
                null,
                null
        );
    }
}
