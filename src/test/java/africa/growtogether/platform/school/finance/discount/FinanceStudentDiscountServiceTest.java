package africa.growtogether.platform.school.finance.discount;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static africa.growtogether.platform.school.finance.discount.FinanceStudentDiscountDtos.CreateStudentDiscountRequest;
import static africa.growtogether.platform.school.finance.discount.FinanceStudentDiscountDtos.StudentDiscountRequestView;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinanceStudentDiscountServiceTest {

    private FinanceStudentDiscountJdbcRepository repository;
    private FinanceFoundationJdbcRepository foundationRepository;
    private FinanceDiscountService discountService;
    private africa.growtogether.platform.school.finance.invoice.FinanceInvoiceJdbcRepository invoiceRepository;
    private FinanceStudentDiscountService service;

    private UUID tenantId;
    private UUID studentId;
    private UUID accountId;
    private UUID schemeId;
    private UUID actorId;

    @BeforeEach
    void setUp() {
        repository = mock(
                FinanceStudentDiscountJdbcRepository.class
        );

        foundationRepository = mock(
                FinanceFoundationJdbcRepository.class
        );

        discountService = mock(
                FinanceDiscountService.class
        );

        invoiceRepository = mock(
                africa.growtogether.platform.school.finance.invoice.FinanceInvoiceJdbcRepository.class
        );

        service = new FinanceStudentDiscountService(
                repository,
                foundationRepository,
                discountService,
                invoiceRepository
        );

        tenantId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        schemeId = UUID.randomUUID();
        actorId = UUID.randomUUID();
    }

    @Test
    void createsNormalizedPendingRequest() {
        CreateStudentDiscountRequest request =
                request(
                        "  DISC-001  ",
                        LocalDate.of(
                                2026,
                                1,
                                1
                        ),
                        null
                );

        StudentDiscountRequestView persisted =
                view(
                        "DISC-001"
                );

        allowValidDependencies();

        when(
                repository.createStudentDiscountRequest(
                        eq(tenantId),
                        eq("DISC-001"),
                        eq(studentId),
                        eq(accountId),
                        eq(schemeId),
                        eq(
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                )
                        ),
                        eq(null),
                        eq(null),
                        eq(null),
                        eq(actorId),
                        eq(actorId.toString())
                )
        ).thenReturn(
                persisted
        );

        StudentDiscountRequestView result =
                service.createStudentDiscountRequest(
                        tenantId,
                        request,
                        actorId
                );

        assertSame(
                persisted,
                result
        );

        verify(repository).createStudentDiscountRequest(
                tenantId,
                "DISC-001",
                studentId,
                accountId,
                schemeId,
                LocalDate.of(
                        2026,
                        1,
                        1
                ),
                null,
                null,
                null,
                actorId,
                actorId.toString()
        );
    }

    @Test
    void rejectsDuplicateTenantReference() {
        when(
                repository.existsStudentDiscountReference(
                        tenantId,
                        "DISC-001"
                )
        ).thenReturn(
                true
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createStudentDiscountRequest(
                                        tenantId,
                                        request(
                                                "DISC-001",
                                                LocalDate.now(),
                                                null
                                        ),
                                        actorId
                                )
                );

        assertEquals(
                "discountReference already exists in this tenant.",
                error.getMessage()
        );

        verify(
                foundationRepository,
                never()
        ).existsTenantReference(
                anyString(),
                any(),
                any()
        );
    }

    @Test
    void rejectsUnavailableStudent() {
        when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                false
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createStudentDiscountRequest(
                                        tenantId,
                                        request(
                                                "DISC-001",
                                                LocalDate.now(),
                                                null
                                        ),
                                        actorId
                                )
                );

        assertEquals(
                "studentId is not available in this tenant.",
                error.getMessage()
        );
    }

    @Test
    void rejectsUnavailableStudentFinancialAccount() {
        when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        when(
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        accountId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createStudentDiscountRequest(
                                        tenantId,
                                        request(
                                                "DISC-001",
                                                LocalDate.now(),
                                                null
                                        ),
                                        actorId
                                )
                );

        assertEquals(
                "Student financial account is not available in this tenant.",
                error.getMessage()
        );
    }

    @Test
    void rejectsAccountStudentMismatch() {
        when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        when(
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        accountId
                )
        ).thenReturn(
                Optional.of(
                        new FinanceStudentDiscountJdbcRepository.StudentFinancialAccountScope(
                                UUID.randomUUID(),
                                "ACTIVE",
                                "ACTIVE"
                        )
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createStudentDiscountRequest(
                                        tenantId,
                                        request(
                                                "DISC-001",
                                                LocalDate.now(),
                                                null
                                        ),
                                        actorId
                                )
                );

        assertEquals(
                "Student financial account does not belong to the requested student.",
                error.getMessage()
        );
    }

    @Test
    void rejectsInactiveStudentFinancialAccount() {
        when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        when(
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        accountId
                )
        ).thenReturn(
                Optional.of(
                        new FinanceStudentDiscountJdbcRepository.StudentFinancialAccountScope(
                                studentId,
                                "ON_HOLD",
                                "ACTIVE"
                        )
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createStudentDiscountRequest(
                                        tenantId,
                                        request(
                                                "DISC-001",
                                                LocalDate.now(),
                                                null
                                        ),
                                        actorId
                                )
                );

        assertEquals(
                "The student financial account is not active.",
                error.getMessage()
        );
    }

    @Test
    void rejectsUnavailableDiscountScheme() {
        allowStudentAndAccount();

        when(
                discountService.getFeeDiscountScheme(
                        tenantId,
                        schemeId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Fee discount scheme is not available in this tenant."
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createStudentDiscountRequest(
                                        tenantId,
                                        request(
                                                "DISC-001",
                                                LocalDate.now(),
                                                null
                                        ),
                                        actorId
                                )
                );

        assertEquals(
                "Fee discount scheme is not available in this tenant.",
                error.getMessage()
        );
    }

    @Test
    void rejectsInactiveDiscountScheme() {
        allowStudentAndAccount();

        when(
                discountService.getFeeDiscountScheme(
                        tenantId,
                        schemeId
                )
        ).thenReturn(
                scheme(
                        false,
                        "ACTIVE"
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createStudentDiscountRequest(
                                        tenantId,
                                        request(
                                                "DISC-001",
                                                LocalDate.now(),
                                                null
                                        ),
                                        actorId
                                )
                );

        assertEquals(
                "Fee discount scheme is not active.",
                error.getMessage()
        );
    }

    @Test
    void rejectsNonActiveDiscountSchemeStatus() {
        allowStudentAndAccount();

        when(
                discountService.getFeeDiscountScheme(
                        tenantId,
                        schemeId
                )
        ).thenReturn(
                scheme(
                        true,
                        "INACTIVE"
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createStudentDiscountRequest(
                                        tenantId,
                                        request(
                                                "DISC-001",
                                                LocalDate.now(),
                                                null
                                        ),
                                        actorId
                                )
                );

        assertEquals(
                "Fee discount scheme is not active.",
                error.getMessage()
        );
    }

    @Test
    void rejectsInvalidEffectiveDateRange() {
        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createStudentDiscountRequest(
                                        tenantId,
                                        request(
                                                "DISC-001",
                                                LocalDate.of(
                                                        2026,
                                                        2,
                                                        2
                                                ),
                                                LocalDate.of(
                                                        2026,
                                                        2,
                                                        1
                                                )
                                        ),
                                        actorId
                                )
                );

        assertEquals(
                "effectiveTo must not be before effectiveFrom",
                error.getMessage()
        );
    }

    @Test
    void rejectsMissingOrCrossTenantRead() {
        UUID requestId = UUID.randomUUID();

        when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.getStudentDiscountRequest(
                                        tenantId,
                                        requestId
                                )
                );

        assertEquals(
                "Student discount request is not available in this tenant.",
                error.getMessage()
        );
    }

    @Test
    void delegatesTenantScopedList() {
        List<StudentDiscountRequestView> expected =
                List.of(
                        view(
                                "DISC-001"
                        )
                );

        when(
                repository.listStudentDiscountRequests(
                        tenantId
                )
        ).thenReturn(
                expected
        );

        List<StudentDiscountRequestView> result =
                service.listStudentDiscountRequests(
                        tenantId
                );

        assertSame(
                expected,
                result
        );

        verify(repository).listStudentDiscountRequests(
                tenantId
        );
    }


    @Test
    void approvesPendingRequestUsingSchemeDiscountValue() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID schemeId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        StudentDiscountRequestView pending =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        studentId,
                        accountId,
                        schemeId,
                        7L,
                        "PENDING",
                        "ACTIVE"
                );

        FinanceDiscountDtos.FeeDiscountSchemeView scheme =
                activeDecisionScheme(
                        new java.math.BigDecimal(
                                "12.50"
                        )
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        pending
                )
        );

        org.mockito.Mockito.when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        org.mockito.Mockito.when(
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        accountId
                )
        ).thenReturn(
                java.util.Optional.of(
                        new FinanceStudentDiscountJdbcRepository.StudentFinancialAccountScope(
                                studentId,
                                "ACTIVE",
                                "ACTIVE"
                        )
                )
        );

        org.mockito.Mockito.when(
                discountService.getFeeDiscountScheme(
                        tenantId,
                        schemeId
                )
        ).thenReturn(
                scheme
        );

        org.mockito.Mockito.when(
                repository.approveStudentDiscountRequest(
                        tenantId,
                        requestId,
                        7L,
                        scheme.discountValue(),
                        actorId,
                        actorId.toString()
                )
        ).thenReturn(
                pending
        );

        service.approveStudentDiscountRequest(
                tenantId,
                requestId,
                actorId,
                actorId.toString()
        );

        org.mockito.Mockito.verify(
                repository
        ).approveStudentDiscountRequest(
                tenantId,
                requestId,
                7L,
                scheme.discountValue(),
                actorId,
                actorId.toString()
        );
    }

    @Test
    void approvalRejectsMissingOrCrossTenantRequest() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );
    }

    @Test
    void approvalRejectsNonPendingOrInactiveRequest() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        StudentDiscountRequestView approved =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        1L,
                        "APPROVED",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        approved
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                actorId,
                                actorId.toString()
                        )
        );

        StudentDiscountRequestView inactive =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        1L,
                        "PENDING",
                        "INACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        inactive
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                actorId,
                                actorId.toString()
                        )
        );
    }

    @Test
    void approvalRevalidatesStudentOwnership() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        StudentDiscountRequestView pending =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        studentId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        1L,
                        "PENDING",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        pending
                )
        );

        org.mockito.Mockito.when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                false
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );
    }

    @Test
    void approvalRejectsUnavailableAccountOrAccountStudentMismatch() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        StudentDiscountRequestView pending =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        studentId,
                        accountId,
                        UUID.randomUUID(),
                        1L,
                        "PENDING",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        pending
                )
        );

        org.mockito.Mockito.when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        org.mockito.Mockito.when(
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        accountId
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );

        org.mockito.Mockito.when(
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        accountId
                )
        ).thenReturn(
                java.util.Optional.of(
                        new FinanceStudentDiscountJdbcRepository.StudentFinancialAccountScope(
                                UUID.randomUUID(),
                                "ACTIVE",
                                "ACTIVE"
                        )
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );
    }

    @Test
    void approvalRejectsInactiveAccount() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        StudentDiscountRequestView pending =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        studentId,
                        accountId,
                        UUID.randomUUID(),
                        1L,
                        "PENDING",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        pending
                )
        );

        org.mockito.Mockito.when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        org.mockito.Mockito.when(
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        accountId
                )
        ).thenReturn(
                java.util.Optional.of(
                        new FinanceStudentDiscountJdbcRepository.StudentFinancialAccountScope(
                                studentId,
                                "PENDING",
                                "ACTIVE"
                        )
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );
    }

    @Test
    void approvalRejectsUnavailableOrInactiveScheme() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID schemeId = UUID.randomUUID();

        StudentDiscountRequestView pending =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        studentId,
                        accountId,
                        schemeId,
                        1L,
                        "PENDING",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        pending
                )
        );

        org.mockito.Mockito.when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        org.mockito.Mockito.when(
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        accountId
                )
        ).thenReturn(
                java.util.Optional.of(
                        new FinanceStudentDiscountJdbcRepository.StudentFinancialAccountScope(
                                studentId,
                                "ACTIVE",
                                "ACTIVE"
                        )
                )
        );

        org.mockito.Mockito.when(
                discountService.getFeeDiscountScheme(
                        tenantId,
                        schemeId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Fee discount scheme is not available in this tenant."
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );

        FinanceDiscountDtos.FeeDiscountSchemeView inactive =
                org.mockito.Mockito.mock(
                        FinanceDiscountDtos.FeeDiscountSchemeView.class
                );

        org.mockito.Mockito.when(
                inactive.active()
        ).thenReturn(
                false
        );

        org.mockito.Mockito.when(
                inactive.status()
        ).thenReturn(
                "ACTIVE"
        );

        org.mockito.Mockito.doReturn(
                inactive
        ).when(
                discountService
        ).getFeeDiscountScheme(
                tenantId,
                schemeId
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );

        FinanceDiscountDtos.FeeDiscountSchemeView nonActiveStatus =
                org.mockito.Mockito.mock(
                        FinanceDiscountDtos.FeeDiscountSchemeView.class
                );

        org.mockito.Mockito.when(
                nonActiveStatus.active()
        ).thenReturn(
                true
        );

        org.mockito.Mockito.when(
                nonActiveStatus.status()
        ).thenReturn(
                "INACTIVE"
        );

        org.mockito.Mockito.doReturn(
                nonActiveStatus
        ).when(
                discountService
        ).getFeeDiscountScheme(
                tenantId,
                schemeId
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantId,
                                requestId,
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );
    }

    @Test
    void rejectsPendingRequest() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        StudentDiscountRequestView pending =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        9L,
                        "PENDING",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        pending
                )
        );

        org.mockito.Mockito.when(
                repository.rejectStudentDiscountRequest(
                        tenantId,
                        requestId,
                        9L,
                        actorId.toString()
                )
        ).thenReturn(
                pending
        );

        service.rejectStudentDiscountRequest(
                tenantId,
                requestId,
                actorId,
                actorId.toString()
        );

        org.mockito.Mockito.verify(
                repository
        ).rejectStudentDiscountRequest(
                tenantId,
                requestId,
                9L,
                actorId.toString()
        );
    }

    @Test
    void rejectionRejectsMissingNonPendingOrInactiveRequest() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.rejectStudentDiscountRequest(
                                tenantId,
                                requestId,
                                actorId,
                                actorId.toString()
                        )
        );

        StudentDiscountRequestView approved =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        1L,
                        "APPROVED",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        approved
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.rejectStudentDiscountRequest(
                                tenantId,
                                requestId,
                                actorId,
                                actorId.toString()
                        )
        );

        StudentDiscountRequestView inactive =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        1L,
                        "PENDING",
                        "INACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        inactive
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.rejectStudentDiscountRequest(
                                tenantId,
                                requestId,
                                actorId,
                                actorId.toString()
                        )
        );
    }

    @Test
    void staleDecisionRepositoryFailureIsPropagated() {
        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        StudentDiscountRequestView pending =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        5L,
                        "PENDING",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        pending
                )
        );

        org.mockito.Mockito.when(
                repository.rejectStudentDiscountRequest(
                        tenantId,
                        requestId,
                        5L,
                        actorId.toString()
                )
        ).thenThrow(
                new IllegalStateException(
                        "Student discount request is not eligible for rejection."
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.rejectStudentDiscountRequest(
                                tenantId,
                                requestId,
                                actorId,
                                actorId.toString()
                        )
        );
    }

    private StudentDiscountRequestView pendingDecisionRequest(
            UUID tenantId,
            UUID requestId,
            UUID studentId,
            UUID accountId,
            UUID schemeId,
            long version,
            String discountStatus,
            String status
    ) {
        StudentDiscountRequestView request =
                org.mockito.Mockito.mock(
                        StudentDiscountRequestView.class
                );

        org.mockito.Mockito.when(
                request.id()
        ).thenReturn(
                requestId
        );

        org.mockito.Mockito.when(
                request.tenantId()
        ).thenReturn(
                tenantId
        );

        org.mockito.Mockito.when(
                request.studentId()
        ).thenReturn(
                studentId
        );

        org.mockito.Mockito.when(
                request.studentFinancialAccountId()
        ).thenReturn(
                accountId
        );

        org.mockito.Mockito.when(
                request.discountSchemeId()
        ).thenReturn(
                schemeId
        );

        org.mockito.Mockito.when(
                request.version()
        ).thenReturn(
                version
        );

        org.mockito.Mockito.when(
                request.discountStatus()
        ).thenReturn(
                discountStatus
        );

        org.mockito.Mockito.when(
                request.status()
        ).thenReturn(
                status
        );

        return request;
    }

    private FinanceDiscountDtos.FeeDiscountSchemeView activeDecisionScheme(
            java.math.BigDecimal discountValue
    ) {
        FinanceDiscountDtos.FeeDiscountSchemeView scheme =
                org.mockito.Mockito.mock(
                        FinanceDiscountDtos.FeeDiscountSchemeView.class
                );

        org.mockito.Mockito.when(
                scheme.active()
        ).thenReturn(
                true
        );

        org.mockito.Mockito.when(
                scheme.status()
        ).thenReturn(
                "ACTIVE"
        );

        org.mockito.Mockito.when(
                scheme.discountValue()
        ).thenReturn(
                discountValue
        );

        return scheme;
    }

    private void allowValidDependencies() {
        allowStudentAndAccount();

        when(
                discountService.getFeeDiscountScheme(
                        tenantId,
                        schemeId
                )
        ).thenReturn(
                scheme(
                        true,
                        "ACTIVE"
                )
        );
    }

    private void allowStudentAndAccount() {
        when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        when(
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        accountId
                )
        ).thenReturn(
                Optional.of(
                        new FinanceStudentDiscountJdbcRepository.StudentFinancialAccountScope(
                                studentId,
                                "ACTIVE",
                                "ACTIVE"
                        )
                )
        );
    }

    private CreateStudentDiscountRequest request(
            String reference,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        return new CreateStudentDiscountRequest(
                reference,
                studentId,
                accountId,
                schemeId,
                effectiveFrom,
                effectiveTo,
                null,
                null
        );
    }

    private FinanceDiscountDtos.FeeDiscountSchemeView scheme(
            boolean active,
            String status
    ) {
        Instant now = Instant.now();

        return new FinanceDiscountDtos.FeeDiscountSchemeView(
                schemeId,
                tenantId,
                "SCHEME-001",
                "Scholarship",
                null,
                "PERCENTAGE",
                BigDecimal.TEN,
                null,
                null,
                null,
                Map.of(),
                LocalDate.of(
                        2026,
                        1,
                        1
                ),
                null,
                true,
                active,
                status,
                now,
                actorId.toString(),
                now,
                actorId.toString(),
                0L
        );
    }

    private StudentDiscountRequestView view(
            String reference
    ) {
        Instant now = Instant.now();

        return new StudentDiscountRequestView(
                UUID.randomUUID(),
                tenantId,
                reference,
                studentId,
                accountId,
                schemeId,
                null,
                null,
                LocalDate.of(
                        2026,
                        1,
                        1
                ),
                null,
                null,
                null,
                now,
                actorId,
                null,
                null,
                "PENDING",
                "ACTIVE",
                now,
                actorId.toString(),
                now,
                actorId.toString(),
                0L
        );
    }

    @Test
    void appliesApprovedPercentageDiscountToDraftInvoice() {

        UUID requestId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();

        StudentDiscountRequestView approved =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        studentId,
                        accountId,
                        schemeId,
                        7L,
                        "APPROVED",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                approved.approvedDiscountValue()
        ).thenReturn(
                new java.math.BigDecimal(
                        "10.00"
                )
        );

        org.mockito.Mockito.when(
                approved.approvedDiscountAmount()
        ).thenReturn(
                null
        );

        org.mockito.Mockito.when(
                approved.effectiveFrom()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        1,
                        1
                )
        );

        org.mockito.Mockito.when(
                approved.effectiveTo()
        ).thenReturn(
                null
        );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        approved
                ),
                java.util.Optional.of(
                        approved
                )
        );

        org.mockito.Mockito.when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );

        africa.growtogether.platform.school.finance.invoice.FinanceInvoiceJdbcRepository.S3DiscountInvoiceLine line =
                new africa.growtogether.platform.school.finance.invoice.FinanceInvoiceJdbcRepository.S3DiscountInvoiceLine(
                        lineId,
                        null,
                        null,
                        new java.math.BigDecimal(
                                "100.00"
                        ),
                        new java.math.BigDecimal(
                                "0.00"
                        ),
                        new java.math.BigDecimal(
                                "0.00"
                        ),
                        "ACTIVE",
                        "ACTIVE",
                        0L
                );

        africa.growtogether.platform.school.finance.invoice.FinanceInvoiceJdbcRepository.S3DiscountInvoiceSnapshot invoice =
                new africa.growtogether.platform.school.finance.invoice.FinanceInvoiceJdbcRepository.S3DiscountInvoiceSnapshot(
                        invoiceId,
                        accountId,
                        studentId,
                        LocalDate.of(
                                2026,
                                1,
                                15
                        ),
                        "UGX",
                        new java.math.BigDecimal(
                                "100.00"
                        ),
                        new java.math.BigDecimal(
                                "0.00"
                        ),
                        new java.math.BigDecimal(
                                "0.00"
                        ),
                        new java.math.BigDecimal(
                                "100.00"
                        ),
                        new java.math.BigDecimal(
                                "0.00"
                        ),
                        new java.math.BigDecimal(
                                "100.00"
                        ),
                        "DRAFT",
                        "ACTIVE",
                        3L,
                        java.util.List.of(
                                line
                        )
                );

        org.mockito.Mockito.when(
                invoiceRepository.findDiscountApplicationInvoice(
                        tenantId,
                        invoiceId
                )
        ).thenReturn(
                java.util.Optional.of(
                        invoice
                )
        );

        africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.StudentFinancialAccountView account =
                org.mockito.Mockito.mock(
                        africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.StudentFinancialAccountView.class
                );

        org.mockito.Mockito.when(
                account.id()
        ).thenReturn(
                accountId
        );

        org.mockito.Mockito.when(
                account.billingStatus()
        ).thenReturn(
                "ACTIVE"
        );

        org.mockito.Mockito.when(
                account.status()
        ).thenReturn(
                "ACTIVE"
        );

        org.mockito.Mockito.when(
                foundationRepository.findStudentAccount(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(
                java.util.Optional.of(
                        account
                )
        );

        FinanceDiscountDtos.FeeDiscountSchemeView scheme =
                activeDecisionScheme(
                        new java.math.BigDecimal(
                                "10.00"
                        )
                );

        org.mockito.Mockito.when(
                scheme.discountType()
        ).thenReturn(
                "PERCENTAGE"
        );

        org.mockito.Mockito.when(
                scheme.effectiveFrom()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        1,
                        1
                )
        );

        org.mockito.Mockito.when(
                scheme.effectiveTo()
        ).thenReturn(
                null
        );

        org.mockito.Mockito.when(
                discountService.getFeeDiscountScheme(
                        tenantId,
                        schemeId
                )
        ).thenReturn(
                scheme
        );

        StudentDiscountRequestView result =
                service.applyStudentDiscount(
                        tenantId,
                        requestId,
                        new FinanceStudentDiscountDtos.ApplyStudentDiscountRequest(
                                invoiceId
                        ),
                        actorId,
                        actorId.toString()
                );

        assertSame(
                approved,
                result
        );

        org.mockito.Mockito.verify(
                repository
        ).applyStudentDiscountToBilling(
                tenantId,
                requestId,
                7L,
                new java.math.BigDecimal(
                        "10.00"
                ),
                actorId.toString()
        );

        org.mockito.Mockito.verify(
                invoiceRepository
        ).updateDiscountApplicationLine(
                tenantId,
                invoiceId,
                lineId,
                0L,
                new java.math.BigDecimal(
                        "10.00"
                ),
                new java.math.BigDecimal(
                        "90.00"
                ),
                actorId.toString()
        );

        org.mockito.Mockito.verify(
                invoiceRepository
        ).updateDiscountApplicationInvoice(
                tenantId,
                invoiceId,
                3L,
                new java.math.BigDecimal(
                        "10.00"
                ),
                new java.math.BigDecimal(
                        "90.00"
                ),
                new java.math.BigDecimal(
                        "90.00"
                ),
                actorId.toString()
        );

        org.mockito.Mockito.verify(
                repository
        ).insertAppliedStudentDiscountAdjustment(
                tenantId,
                requestId,
                accountId,
                invoiceId,
                new java.math.BigDecimal(
                        "10.00"
                ),
                actorId,
                actorId.toString()
        );
    }

    @Test
    void applicationRejectsAlreadyAppliedDiscountBeforeInvoiceMutation() {

        UUID requestId = UUID.randomUUID();

        StudentDiscountRequestView applied =
                pendingDecisionRequest(
                        tenantId,
                        requestId,
                        studentId,
                        accountId,
                        schemeId,
                        8L,
                        "ACTIVE",
                        "ACTIVE"
                );

        org.mockito.Mockito.when(
                applied.approvedDiscountValue()
        ).thenReturn(
                new java.math.BigDecimal(
                        "10.00"
                )
        );

        org.mockito.Mockito.when(
                applied.approvedDiscountAmount()
        ).thenReturn(
                new java.math.BigDecimal(
                        "10.00"
                )
        );

        org.mockito.Mockito.when(
                repository.getStudentDiscountRequest(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                java.util.Optional.of(
                        applied
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.applyStudentDiscount(
                                tenantId,
                                requestId,
                                new FinanceStudentDiscountDtos.ApplyStudentDiscountRequest(
                                        UUID.randomUUID()
                                ),
                                actorId,
                                actorId.toString()
                        )
        );

        org.mockito.Mockito.verifyNoInteractions(
                invoiceRepository
        );
    }

}
