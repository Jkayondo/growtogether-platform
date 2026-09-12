package africa.growtogether.platform.school.finance.invoice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.StudentFeeAssignmentView;
import africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentService;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.FeeItemView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.FeeStructureItemView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.FeeStructureView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.StudentFinancialAccountView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationService;
import africa.growtogether.platform.school.finance.invoice.FinanceInvoiceDtos.CreateDraftInvoiceRequest;
import africa.growtogether.platform.school.finance.invoice.FinanceInvoiceDtos.StudentInvoiceView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class FinanceInvoiceServiceTest {

    @Mock
    FinanceFeeAssignmentService assignments;

    @Mock
    FinanceFoundationService foundation;

    @Mock
    FinanceInvoiceJdbcRepository repository;

    FinanceInvoiceService service;

    UUID tenantId;
    UUID studentId;
    UUID accountId;
    UUID structureId;
    UUID assignmentId;
    UUID feeItemId;
    UUID structureItemId;

    @BeforeEach
    void setUp() {

        service =
                new FinanceInvoiceService(
                        assignments,
                        foundation,
                        repository
                );

        tenantId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        structureId = UUID.randomUUID();
        assignmentId = UUID.randomUUID();
        feeItemId = UUID.randomUUID();
        structureItemId = UUID.randomUUID();
    }

    @Test
    void createsDraftInvoiceFromActiveAssignmentAndStructureItems() {

        LocalDate invoiceDate =
                LocalDate.of(
                        2026,
                        9,
                        12
                );

        UUID academicYearId =
                UUID.randomUUID();

        UUID academicTermId =
                UUID.randomUUID();

        StudentFeeAssignmentView assignment =
                new StudentFeeAssignmentView(
                        assignmentId,
                        tenantId,
                        "ASG-001",
                        accountId,
                        studentId,
                        null,
                        structureId,
                        Instant.now(),
                        UUID.randomUUID(),
                        LocalDate.of(
                                2026,
                                1,
                                1
                        ),
                        LocalDate.of(
                                2026,
                                12,
                                31
                        ),
                        null,
                        "ACTIVE",
                        "ACTIVE"
                );

        FeeStructureView structure =
                new FeeStructureView(
                        structureId,
                        tenantId,
                        "FS-001",
                        "Primary Fees",
                        null,
                        academicYearId,
                        academicTermId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "UGX",
                        LocalDate.of(
                                2026,
                                1,
                                1
                        ),
                        LocalDate.of(
                                2026,
                                12,
                                31
                        ),
                        null,
                        UUID.randomUUID(),
                        "ACTIVE",
                        "ACTIVE"
                );

        StudentFinancialAccountView account =
                new StudentFinancialAccountView(
                        accountId,
                        tenantId,
                        "ACC-001",
                        studentId,
                        null,
                        "UGX",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        "ACTIVE",
                        "ACTIVE"
                );

        FeeStructureItemView structureItem =
                new FeeStructureItemView(
                        structureItemId,
                        tenantId,
                        structureId,
                        feeItemId,
                        new BigDecimal("150000.00"),
                        new BigDecimal("2.000"),
                        true,
                        false,
                        LocalDate.of(
                                2026,
                                9,
                                30
                        ),
                        1,
                        "ACTIVE"
                );

        FeeItemView feeItem =
                new FeeItemView(
                        feeItemId,
                        tenantId,
                        UUID.randomUUID(),
                        "TUITION",
                        "Tuition",
                        null,
                        "UGX",
                        new BigDecimal("150000.00"),
                        "TERM",
                        true,
                        true,
                        false,
                        null,
                        true,
                        "ACTIVE"
                );

        CreateDraftInvoiceRequest request =
                new CreateDraftInvoiceRequest(
                        "INV-001",
                        assignmentId,
                        invoiceDate,
                        LocalDate.of(
                                2026,
                                9,
                                30
                        )
                );

        StudentInvoiceView expected =
                new StudentInvoiceView(
                        UUID.randomUUID(),
                        tenantId,
                        "INV-001",
                        accountId,
                        studentId,
                        null,
                        academicYearId,
                        academicTermId,
                        structureId,
                        invoiceDate,
                        request.dueDate(),
                        "UGX",
                        new BigDecimal("300000.00"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("300000.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("300000.00"),
                        "DRAFT",
                        "ACTIVE",
                        List.of()
                );

        when(
                repository.existsInvoiceNumber(
                        tenantId,
                        "INV-001"
                )
        ).thenReturn(
                false
        );

        when(
                assignments.findStudentFeeAssignment(
                        tenantId,
                        assignmentId
                )
        ).thenReturn(
                Optional.of(
                        assignment
                )
        );

        when(
                foundation.listFeeStructures(
                        tenantId
                )
        ).thenReturn(
                List.of(
                        structure
                )
        );

        when(
                foundation.findStudentAccount(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(
                Optional.of(
                        account
                )
        );

        when(
                foundation.listFeeStructureItems(
                        tenantId,
                        structureId
                )
        ).thenReturn(
                List.of(
                        structureItem
                )
        );

        when(
                foundation.listFeeItems(
                        tenantId
                )
        ).thenReturn(
                List.of(
                        feeItem
                )
        );

        when(
                repository.createDraftInvoice(
                        eq(tenantId),
                        eq("INV-001"),
                        eq(assignment),
                        eq(structure),
                        eq(account),
                        eq(invoiceDate),
                        eq(request.dueDate()),
                        eq(new BigDecimal("300000.00")),
                        anyList(),
                        eq("actor-1")
                )
        ).thenReturn(
                expected
        );

        StudentInvoiceView actual =
                service.createDraftInvoice(
                        tenantId,
                        request,
                        "actor-1"
                );

        assertSame(
                expected,
                actual
        );

        ArgumentCaptor<List<FinanceInvoiceJdbcRepository.DraftInvoiceLine>>
                lines =
                ArgumentCaptor.forClass(
                        List.class
                );

        verify(repository).createDraftInvoice(
                eq(tenantId),
                eq("INV-001"),
                eq(assignment),
                eq(structure),
                eq(account),
                eq(invoiceDate),
                eq(request.dueDate()),
                eq(new BigDecimal("300000.00")),
                lines.capture(),
                eq("actor-1")
        );

        assertEquals(
                1,
                lines.getValue().size()
        );

        FinanceInvoiceJdbcRepository.DraftInvoiceLine line =
                lines.getValue().getFirst();

        assertEquals(
                feeItemId,
                line.feeItemId()
        );

        assertEquals(
                structureItemId,
                line.feeStructureItemId()
        );

        assertEquals(
                "Tuition",
                line.description()
        );

        assertEquals(
                new BigDecimal("300000.00"),
                line.netAmount()
        );
    }

    @Test
    void rejectsDuplicateInvoiceNumberBeforeCreatingInvoice() {

        when(
                repository.existsInvoiceNumber(
                        tenantId,
                        "INV-001"
                )
        ).thenReturn(
                true
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.createDraftInvoice(
                                        tenantId,
                                        new CreateDraftInvoiceRequest(
                                                " INV-001 ",
                                                assignmentId,
                                                LocalDate.of(
                                                        2026,
                                                        9,
                                                        12
                                                ),
                                                null
                                        ),
                                        "actor-1"
                                )
                );

        assertEquals(
                "Invoice number already exists in this tenant.",
                error.getMessage()
        );

        verifyNoInteractions(
                assignments,
                foundation
        );
    }

    @Test
    void rejectsNonActiveAssignment() {

        StudentFeeAssignmentView assignment =
                new StudentFeeAssignmentView(
                        assignmentId,
                        tenantId,
                        "ASG-001",
                        accountId,
                        studentId,
                        null,
                        structureId,
                        Instant.now(),
                        UUID.randomUUID(),
                        LocalDate.of(
                                2026,
                                1,
                                1
                        ),
                        null,
                        null,
                        "SUSPENDED",
                        "ACTIVE"
                );

        when(
                repository.existsInvoiceNumber(
                        tenantId,
                        "INV-001"
                )
        ).thenReturn(
                false
        );

        when(
                assignments.findStudentFeeAssignment(
                        tenantId,
                        assignmentId
                )
        ).thenReturn(
                Optional.of(
                        assignment
                )
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.createDraftInvoice(
                                        tenantId,
                                        new CreateDraftInvoiceRequest(
                                                "INV-001",
                                                assignmentId,
                                                LocalDate.of(
                                                        2026,
                                                        9,
                                                        12
                                                ),
                                                null
                                        ),
                                        "actor-1"
                                )
                );

        assertEquals(
                "Only an active fee assignment may be invoiced.",
                error.getMessage()
        );

        verifyNoInteractions(
                foundation
        );
    }

    @Test
    void readAndListRemainTenantScopedThroughRepository() {

        UUID invoiceId =
                UUID.randomUUID();

        StudentInvoiceView invoice =
                mock(
                        StudentInvoiceView.class
                );

        when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                Optional.of(
                        invoice
                )
        );

        when(
                repository.studentExists(
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        when(
                repository.listStudentInvoices(
                        tenantId,
                        studentId
                )
        ).thenReturn(
                List.of(
                        invoice
                )
        );

        assertSame(
                invoice,
                service.findStudentInvoice(
                        tenantId,
                        invoiceId
                ).orElseThrow()
        );

        assertEquals(
                List.of(invoice),
                service.listStudentInvoices(
                        tenantId,
                        studentId
                )
        );
    }
}
