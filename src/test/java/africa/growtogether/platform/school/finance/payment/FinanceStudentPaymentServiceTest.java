package africa.growtogether.platform.school.finance.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FinanceStudentPaymentServiceTest {

    private static final UUID TENANT_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "20000000-0000-0000-0000-000000000001"
            );

    private static final String ACTOR =
            "90000000-0000-0000-0000-000000000001";

    @Mock
    private FinanceStudentPaymentJdbcRepository repository;

    private FinanceStudentPaymentService service;

    @BeforeEach
    void setUp() {
        service =
                new FinanceStudentPaymentService(
                        repository
                );
    }

    @Test
    void rejectsNonPositiveAmount() {
        var request = request(
                BigDecimal.ZERO
        );

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.create(
                                TENANT_ID,
                                request,
                                ACTOR
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                error.getStatusCode()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsUnknownFinancialAccount() {
        var request = request(
                new BigDecimal("1000.00")
        );

        when(
                repository.findFinancialAccountContext(
                        TENANT_ID,
                        ACCOUNT_ID
                )
        ).thenReturn(Optional.empty());

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.create(
                                TENANT_ID,
                                request,
                                ACTOR
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                error.getStatusCode()
        );
    }

    private static FinanceStudentPaymentDtos.CreateRequest request(
            BigDecimal amount
    ) {
        return new FinanceStudentPaymentDtos.CreateRequest(
                ACCOUNT_ID,
            amount,
            "BANK_DEPOSIT",
            "FIN-B5-S1-UNIT-001",
            Instant.parse("2026-09-15T05:00:00Z")
        );
    }
}
