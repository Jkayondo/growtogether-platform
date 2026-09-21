package africa.growtogether.platform.school.finance.statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class FinanceStudentAccountStatementServiceTest {

    @Test
    void buildsSummaryFromAuthoritativeRepositoryValues() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        FinanceStudentAccountStatementJdbcRepository repository =
                mock(
                        FinanceStudentAccountStatementJdbcRepository.class
                );

        LocalDate asOf = LocalDate.of(2026, 9, 21);

        when(
                repository.findAccountScope(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(
                Optional.of(
                        new FinanceStudentAccountStatementJdbcRepository.AccountScope(
                                accountId,
                                tenantId,
                                studentId,
                                "UGX",
                                "ACC-001",
                                "ACTIVE",
                                "ACTIVE"
                        )
                )
        );

        when(
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "UGX",
                        asOf
                )
        ).thenReturn(
                new FinanceStudentAccountStatementJdbcRepository.SummaryNumbers(
                        new BigDecimal("1000000.00"),
                        new BigDecimal("300000.00"),
                        new BigDecimal("700000.00"),
                        new BigDecimal("500000.00"),
                        2,
                        1
                )
        );

        when(
                repository.loadUnallocatedPaymentAmount(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(
                new BigDecimal("50000.00")
        );

        FinanceStudentAccountStatementService service =
                new FinanceStudentAccountStatementService(
                        repository,
                        Clock.fixed(
                                Instant.parse("2026-09-21T10:00:00Z"),
                                ZoneOffset.UTC
                        )
                );

        var summary =
                service.accountSummary(
                        tenantId,
                        studentId,
                        "ugx",
                        asOf
                );

        assertEquals(accountId, summary.studentFinancialAccountId());
        assertEquals("UGX", summary.currencyCode());
        assertEquals(new BigDecimal("700000.00"), summary.totalOutstanding());
        assertEquals(new BigDecimal("500000.00"), summary.arrearsOutstanding());
        assertEquals(new BigDecimal("50000.00"), summary.unallocatedPaymentAmount());
        assertEquals(2, summary.invoiceCount());
        assertEquals(1, summary.overdueInvoiceCount());
    }

    @Test
    void omittedAsOfDateUsesInjectedClock() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        FinanceStudentAccountStatementJdbcRepository repository =
                mock(
                        FinanceStudentAccountStatementJdbcRepository.class
                );

        when(
                repository.findAccountScope(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(
                Optional.of(
                        new FinanceStudentAccountStatementJdbcRepository.AccountScope(
                                accountId,
                                tenantId,
                                studentId,
                                "UGX",
                                "ACC-002",
                                "ACTIVE",
                                "ACTIVE"
                        )
                )
        );

        LocalDate fixedDate = LocalDate.of(2026, 9, 21);

        when(
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "UGX",
                        fixedDate
                )
        ).thenReturn(
                new FinanceStudentAccountStatementJdbcRepository.SummaryNumbers(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        0
                )
        );

        when(
                repository.loadUnallocatedPaymentAmount(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(BigDecimal.ZERO);

        FinanceStudentAccountStatementService service =
                new FinanceStudentAccountStatementService(
                        repository,
                        Clock.fixed(
                                Instant.parse("2026-09-21T00:30:00Z"),
                                ZoneOffset.UTC
                        )
                );

        assertEquals(
                fixedDate,
                service.accountSummary(
                        tenantId,
                        studentId,
                        "UGX",
                        null
                ).asOfDate()
        );
    }

    @Test
    void missingAccountIsRejected() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        FinanceStudentAccountStatementJdbcRepository repository =
                mock(
                        FinanceStudentAccountStatementJdbcRepository.class
                );

        when(
                repository.findAccountScope(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(Optional.empty());

        FinanceStudentAccountStatementService service =
                new FinanceStudentAccountStatementService(
                        repository,
                        Clock.systemUTC()
                );

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.accountSummary(
                                        tenantId,
                                        studentId,
                                        "UGX",
                                        LocalDate.of(
                                                2026,
                                                9,
                                                21
                                        )
                                )
                );

        assertTrue(
                ex.getMessage().contains(
                        "not available"
                )
        );
    }

    @Test
    void statementRejectsInvalidDateRange() {

        FinanceStudentAccountStatementJdbcRepository repository =
                mock(
                        FinanceStudentAccountStatementJdbcRepository.class
                );

        FinanceStudentAccountStatementService service =
                new FinanceStudentAccountStatementService(
                        repository,
                        Clock.systemUTC()
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.accountStatement(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "UGX",
                                LocalDate.of(
                                        2026,
                                        9,
                                        22
                                ),
                                LocalDate.of(
                                        2026,
                                        9,
                                        21
                                ),
                                null
                        )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void arrearsViewUsesDerivedRepositoryItems() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        LocalDate asOf = LocalDate.of(2026, 9, 21);

        FinanceStudentAccountStatementJdbcRepository repository =
                mock(
                        FinanceStudentAccountStatementJdbcRepository.class
                );

        when(
                repository.findAccountScope(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(
                Optional.of(
                        new FinanceStudentAccountStatementJdbcRepository.AccountScope(
                                accountId,
                                tenantId,
                                studentId,
                                "UGX",
                                "ACC-003",
                                "ACTIVE",
                                "ACTIVE"
                        )
                )
        );

        when(
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "UGX",
                        asOf
                )
        ).thenReturn(
                new FinanceStudentAccountStatementJdbcRepository.SummaryNumbers(
                        new BigDecimal("700000"),
                        BigDecimal.ZERO,
                        new BigDecimal("700000"),
                        new BigDecimal("700000"),
                        1,
                        1
                )
        );

        when(
                repository.loadUnallocatedPaymentAmount(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(BigDecimal.ZERO);

        when(
                repository.listArrears(
                        tenantId,
                        studentId,
                        "UGX",
                        asOf
                )
        ).thenReturn(
                List.of(
                        new FinanceStudentAccountStatementDtos.ArrearsItem(
                                invoiceId,
                                "INV-001",
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 9, 1),
                                new BigDecimal("700000"),
                                20,
                                "OVERDUE"
                        )
                )
        );

        FinanceStudentAccountStatementService service =
                new FinanceStudentAccountStatementService(
                        repository,
                        Clock.systemUTC()
                );

        var result =
                service.arrears(
                        tenantId,
                        studentId,
                        "UGX",
                        asOf
                );

        assertEquals(1, result.overdueInvoiceCount());
        assertEquals(
                new BigDecimal("700000"),
                result.arrearsOutstanding()
        );
        assertEquals(invoiceId, result.items().getFirst().invoiceId());
    }
}
