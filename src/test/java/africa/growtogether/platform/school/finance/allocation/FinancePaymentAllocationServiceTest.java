
package africa.growtogether.platform.school.finance.allocation;

import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.AllocationResponse;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.CreateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FinancePaymentAllocationServiceTest {

    private static final UUID TENANT =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID PAYMENT =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private static final UUID INVOICE =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private static final UUID ACTOR =
            UUID.fromString(
                    "44444444-4444-4444-4444-444444444444"
            );

    @Test
    void createDelegatesFrozenContract() {
        FinancePaymentAllocationJdbcRepository repository =
                mock(
                        FinancePaymentAllocationJdbcRepository.class
                );

        FinancePaymentAllocationService service =
                new FinancePaymentAllocationService(repository);

        CreateRequest request =
                new CreateRequest(
                        INVOICE,
                        null,
                        null,
                        new BigDecimal("25.00")
                );

        AllocationResponse expected =
                new AllocationResponse(
                        UUID.randomUUID(),
                        PAYMENT,
                        INVOICE,
                        null,
                        null,
                        new BigDecimal("25.00"),
                        Instant.now(),
                        ACTOR,
                        "ACTIVE",
                        "ACTIVE"
                );

        when(
                repository.create(
                        TENANT,
                        PAYMENT,
                        request,
                        ACTOR,
                        ACTOR.toString()
                )
        ).thenReturn(expected);

        assertEquals(
                expected,
                service.create(
                        TENANT,
                        PAYMENT,
                        request,
                        ACTOR
                )
        );

        verify(repository).create(
                TENANT,
                PAYMENT,
                request,
                ACTOR,
                ACTOR.toString()
        );
    }

    @Test
    void rejectsNonPositiveAmount() {
        FinancePaymentAllocationJdbcRepository repository =
                mock(
                        FinancePaymentAllocationJdbcRepository.class
                );

        FinancePaymentAllocationService service =
                new FinancePaymentAllocationService(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        TENANT,
                        PAYMENT,
                        new CreateRequest(
                                INVOICE,
                                null,
                                null,
                                BigDecimal.ZERO
                        ),
                        ACTOR
                )
        );
    }

    @Test
    void readsRemainTenantAndPaymentScoped() {
        FinancePaymentAllocationJdbcRepository repository =
                mock(
                        FinancePaymentAllocationJdbcRepository.class
                );

        FinancePaymentAllocationService service =
                new FinancePaymentAllocationService(repository);

        UUID allocationId =
                UUID.randomUUID();

        AllocationResponse expected =
                new AllocationResponse(
                        allocationId,
                        PAYMENT,
                        INVOICE,
                        null,
                        null,
                        new BigDecimal("10.00"),
                        Instant.now(),
                        ACTOR,
                        "ACTIVE",
                        "ACTIVE"
                );

        when(
                repository.get(
                        TENANT,
                        PAYMENT,
                        allocationId
                )
        ).thenReturn(expected);

        when(
                repository.list(
                        TENANT,
                        PAYMENT
                )
        ).thenReturn(List.of(expected));

        assertEquals(
                expected,
                service.get(
                        TENANT,
                        PAYMENT,
                        allocationId
                )
        );

        assertEquals(
                List.of(expected),
                service.list(
                        TENANT,
                        PAYMENT
                )
        );
    }

    @org.junit.jupiter.api.Test
    void correctionReasonIsMandatoryBeforeRepositoryMutation() {
        FinancePaymentAllocationJdbcRepository repository =
                org.mockito.Mockito.mock(
                        FinancePaymentAllocationJdbcRepository.class
                );

        FinancePaymentAllocationService service =
                new FinancePaymentAllocationService(
                        repository
                );

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID paymentId =
                java.util.UUID.randomUUID();

        java.util.UUID allocationId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID actorId =
                java.util.UUID.randomUUID();

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.reverse(
                        tenantId,
                        paymentId,
                        allocationId,
                        new FinancePaymentAllocationDtos.ReverseRequest(
                                "   "
                        ),
                        actorId
                )
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.reallocate(
                        tenantId,
                        paymentId,
                        allocationId,
                        new FinancePaymentAllocationDtos.ReallocateRequest(
                                invoiceId,
                                null,
                                null,
                                ""
                        ),
                        actorId
                )
        );

        org.mockito.Mockito.verifyNoInteractions(
                repository
        );
    }
}
