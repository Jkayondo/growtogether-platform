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

    @org.junit.jupiter.api.Test
    void issuesActiveDraftInvoiceAndReturnsIssuanceMetadata() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID issuerId =
                java.util.UUID.randomUUID();

        String actor =
                issuerId.toString();

        StudentInvoiceView draft =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "DRAFT",
                        "ACTIVE",
                        null,
                        null
                );

        java.time.Instant issuedAt =
                java.time.Instant.parse(
                        "2026-09-13T00:00:00Z"
                );

        StudentInvoiceView issued =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "ISSUED",
                        "ACTIVE",
                        issuedAt,
                        issuerId
                );

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.of(
                        draft
                )
        );

        org.mockito.Mockito.when(
                repository.issueStudentInvoice(
                        tenantId,
                        invoiceId,
                        issuerId,
                        actor
                )
        ).thenReturn(
                java.util.Optional.of(
                        issued
                )
        );

        StudentInvoiceView result =
                service.issueStudentInvoice(
                        tenantId,
                        invoiceId,
                        issuerId,
                        actor
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "ISSUED",
                result.invoiceStatus()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "ACTIVE",
                result.status()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                issuedAt,
                result.issuedAt()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                issuerId,
                result.issuedBy()
        );

        org.mockito.Mockito.verify(
                repository
        ).issueStudentInvoice(
                tenantId,
                invoiceId,
                issuerId,
                actor
        );
    }

    @org.junit.jupiter.api.Test
    void rejectsIssuingNonDraftInvoice() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID issuerId =
                java.util.UUID.randomUUID();

        String actor =
                issuerId.toString();

        StudentInvoiceView issued =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "ISSUED",
                        "ACTIVE",
                        java.time.Instant.parse(
                                "2026-09-13T00:00:00Z"
                        ),
                        issuerId
                );

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.of(
                        issued
                )
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () ->
                        service.issueStudentInvoice(
                                tenantId,
                                invoiceId,
                                issuerId,
                                actor
                        )
        );

        org.mockito.Mockito.verify(
                repository,
                org.mockito.Mockito.never()
        ).issueStudentInvoice(
                tenantId,
                invoiceId,
                issuerId,
                actor
        );
    }

    @org.junit.jupiter.api.Test
    void rejectsIssuingInactiveDraftInvoice() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID issuerId =
                java.util.UUID.randomUUID();

        String actor =
                issuerId.toString();

        StudentInvoiceView inactiveDraft =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "DRAFT",
                        "INACTIVE",
                        null,
                        null
                );

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.of(
                        inactiveDraft
                )
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () ->
                        service.issueStudentInvoice(
                                tenantId,
                                invoiceId,
                                issuerId,
                                actor
                        )
        );

        org.mockito.Mockito.verify(
                repository,
                org.mockito.Mockito.never()
        ).issueStudentInvoice(
                tenantId,
                invoiceId,
                issuerId,
                actor
        );
    }

    @org.junit.jupiter.api.Test
    void rejectsTenantUnavailableInvoiceBeforeIssuance() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID issuerId =
                java.util.UUID.randomUUID();

        String actor =
                issuerId.toString();

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.issueStudentInvoice(
                                tenantId,
                                invoiceId,
                                issuerId,
                                actor
                        )
        );

        org.mockito.Mockito.verify(
                repository,
                org.mockito.Mockito.never()
        ).issueStudentInvoice(
                tenantId,
                invoiceId,
                issuerId,
                actor
        );
    }

    @Test
    void cancelsActiveUnpaidDraftInvoiceWithTrimmedReason() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID cancellerId =
                java.util.UUID.randomUUID();

        String actor =
                cancellerId.toString();

        StudentInvoiceView draft =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "DRAFT",
                        "ACTIVE",
                        null,
                        null
                );

        StudentInvoiceView cancelled =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "CANCELLED",
                        "ACTIVE",
                        null,
                        null
                );

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.of(
                        draft
                )
        );

        org.mockito.Mockito.when(
                repository.cancelStudentInvoice(
                        tenantId,
                        invoiceId,
                        cancellerId,
                        "Administrative correction",
                        actor
                )
        ).thenReturn(
                java.util.Optional.of(
                        cancelled
                )
        );

        StudentInvoiceView result =
                service.cancelStudentInvoice(
                        tenantId,
                        invoiceId,
                        new FinanceInvoiceDtos.CancelStudentInvoiceRequest(
                                "  Administrative correction  "
                        ),
                        cancellerId,
                        actor
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "CANCELLED",
                result.invoiceStatus()
        );

        org.mockito.Mockito.verify(
                repository
        ).cancelStudentInvoice(
                tenantId,
                invoiceId,
                cancellerId,
                "Administrative correction",
                actor
        );
    }

    @Test
    void cancelsActiveUnpaidIssuedInvoiceWithoutChangingIssueMetadata() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID issuerId =
                java.util.UUID.randomUUID();

        java.util.UUID cancellerId =
                java.util.UUID.randomUUID();

        java.time.Instant issuedAt =
                java.time.Instant.parse(
                        "2026-09-13T10:00:00Z"
                );

        String actor =
                cancellerId.toString();

        StudentInvoiceView issued =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "ISSUED",
                        "ACTIVE",
                        issuedAt,
                        issuerId
                );

        StudentInvoiceView cancelled =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "CANCELLED",
                        "ACTIVE",
                        issuedAt,
                        issuerId
                );

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.of(
                        issued
                )
        );

        org.mockito.Mockito.when(
                repository.cancelStudentInvoice(
                        tenantId,
                        invoiceId,
                        cancellerId,
                        "Duplicate billing",
                        actor
                )
        ).thenReturn(
                java.util.Optional.of(
                        cancelled
                )
        );

        StudentInvoiceView result =
                service.cancelStudentInvoice(
                        tenantId,
                        invoiceId,
                        new FinanceInvoiceDtos.CancelStudentInvoiceRequest(
                                "Duplicate billing"
                        ),
                        cancellerId,
                        actor
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "CANCELLED",
                result.invoiceStatus()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                issuedAt,
                result.issuedAt()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                issuerId,
                result.issuedBy()
        );
    }

    @Test
    void rejectsCancellingInvoiceWithPayment() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID cancellerId =
                java.util.UUID.randomUUID();

        StudentInvoiceView paidDraft =
                withPaidAmount(
                        issuanceInvoiceView(
                                tenantId,
                                invoiceId,
                                "DRAFT",
                                "ACTIVE",
                                null,
                                null
                        ),
                        new java.math.BigDecimal(
                                "1.00"
                        )
                );

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.of(
                        paidDraft
                )
        );

        IllegalStateException error =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.cancelStudentInvoice(
                                        tenantId,
                                        invoiceId,
                                        new FinanceInvoiceDtos.CancelStudentInvoiceRequest(
                                                "Correction"
                                        ),
                                        cancellerId,
                                        cancellerId.toString()
                                )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Only an active unpaid draft or issued invoice may be cancelled.",
                error.getMessage()
        );

        org.mockito.Mockito.verify(
                repository,
                org.mockito.Mockito.never()
        ).cancelStudentInvoice(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void rejectsCancellingInactiveInvoice() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID cancellerId =
                java.util.UUID.randomUUID();

        StudentInvoiceView inactive =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "DRAFT",
                        "INACTIVE",
                        null,
                        null
                );

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.of(
                        inactive
                )
        );

        IllegalStateException error =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.cancelStudentInvoice(
                                        tenantId,
                                        invoiceId,
                                        new FinanceInvoiceDtos.CancelStudentInvoiceRequest(
                                                "Correction"
                                        ),
                                        cancellerId,
                                        cancellerId.toString()
                                )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Only an active unpaid draft or issued invoice may be cancelled.",
                error.getMessage()
        );
    }

    @Test
    void rejectsRepeatOrOtherIneligibleCancellation() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID cancellerId =
                java.util.UUID.randomUUID();

        StudentInvoiceView cancelled =
                issuanceInvoiceView(
                        tenantId,
                        invoiceId,
                        "CANCELLED",
                        "ACTIVE",
                        null,
                        null
                );

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.of(
                        cancelled
                )
        );

        IllegalStateException error =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.cancelStudentInvoice(
                                        tenantId,
                                        invoiceId,
                                        new FinanceInvoiceDtos.CancelStudentInvoiceRequest(
                                                "Again"
                                        ),
                                        cancellerId,
                                        cancellerId.toString()
                                )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Only an active unpaid draft or issued invoice may be cancelled.",
                error.getMessage()
        );
    }

    @Test
    void rejectsBlankAndOverlongCancellationReason() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID cancellerId =
                java.util.UUID.randomUUID();

        IllegalArgumentException blank =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.cancelStudentInvoice(
                                        tenantId,
                                        invoiceId,
                                        new FinanceInvoiceDtos.CancelStudentInvoiceRequest(
                                                "   "
                                        ),
                                        cancellerId,
                                        cancellerId.toString()
                                )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "cancellationReason must not be blank",
                blank.getMessage()
        );

        IllegalArgumentException overlong =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.cancelStudentInvoice(
                                        tenantId,
                                        invoiceId,
                                        new FinanceInvoiceDtos.CancelStudentInvoiceRequest(
                                                "x".repeat(
                                                        1001
                                                )
                                        ),
                                        cancellerId,
                                        cancellerId.toString()
                                )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "cancellationReason must not exceed 1000 characters.",
                overlong.getMessage()
        );
    }

    @Test
    void rejectsTenantUnavailableInvoiceBeforeCancellation() {

        java.util.UUID tenantId =
                java.util.UUID.randomUUID();

        java.util.UUID invoiceId =
                java.util.UUID.randomUUID();

        java.util.UUID cancellerId =
                java.util.UUID.randomUUID();

        org.mockito.Mockito.when(
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        IllegalArgumentException error =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.cancelStudentInvoice(
                                        tenantId,
                                        invoiceId,
                                        new FinanceInvoiceDtos.CancelStudentInvoiceRequest(
                                                "Correction"
                                        ),
                                        cancellerId,
                                        cancellerId.toString()
                                )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Student invoice is not available in this tenant.",
                error.getMessage()
        );
    }

    private static StudentInvoiceView withPaidAmount(
            StudentInvoiceView source,
            java.math.BigDecimal paidAmount
    ) {

        return new StudentInvoiceView(
                source.id(),
                source.tenantId(),
                source.invoiceNumber(),
                source.studentFinancialAccountId(),
                source.studentId(),
                source.studentEnrollmentId(),
                source.academicYearId(),
                source.academicTermId(),
                source.feeStructureId(),
                source.invoiceDate(),
                source.dueDate(),
                source.currencyCode(),
                source.subtotalAmount(),
                source.discountAmount(),
                source.taxAmount(),
                source.totalAmount(),
                paidAmount,
                source.outstandingAmount(),
                source.invoiceStatus(),
                source.issuedAt(),
                source.issuedBy(),
                source.status(),
                source.lines()
        );
    }

    private static StudentInvoiceView issuanceInvoiceView(
            java.util.UUID tenantId,
            java.util.UUID invoiceId,
            String invoiceStatus,
            String status,
            java.time.Instant issuedAt,
            java.util.UUID issuedBy
    ) {

        java.math.BigDecimal amount =
                new java.math.BigDecimal(
                        "500000.00"
                );

        return new StudentInvoiceView(
                invoiceId,
                tenantId,
                "INV-S2-001",
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                null,
                null,
                null,
                null,
                java.time.LocalDate.of(
                        2026,
                        9,
                        13
                ),
                java.time.LocalDate.of(
                        2026,
                        9,
                        30
                ),
                "UGX",
                amount,
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO,
                amount,
                java.math.BigDecimal.ZERO,
                amount,
                invoiceStatus,
                issuedAt,
                issuedBy,
                status,
                java.util.List.of()
        );
    }

}
