package africa.growtogether.platform.school.finance.invoice;

import static africa.growtogether.platform.school.finance.invoice.FinanceInvoiceDtos.*;

import africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.StudentFeeAssignmentView;
import africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentService;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.FeeItemView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.FeeStructureItemView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.FeeStructureView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.StudentFinancialAccountView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationService;
import africa.growtogether.platform.school.finance.invoice.FinanceInvoiceJdbcRepository.DraftInvoiceLine;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FinanceInvoiceService {

    private final FinanceFeeAssignmentService assignments;
    private final FinanceFoundationService foundation;
    private final FinanceInvoiceJdbcRepository repository;

    public FinanceInvoiceService(
            FinanceFeeAssignmentService assignments,
            FinanceFoundationService foundation,
            FinanceInvoiceJdbcRepository repository
    ) {
        this.assignments = assignments;
        this.foundation = foundation;
        this.repository = repository;
    }

    @Transactional
    public StudentInvoiceView createDraftInvoice(
            UUID tenantId,
            CreateDraftInvoiceRequest request,
            String actor
    ) {

        requireTenant(
                tenantId
        );

        if (request == null) {
            throw new IllegalArgumentException(
                    "request must not be null"
            );
        }

        if (request.feeAssignmentId() == null) {
            throw new IllegalArgumentException(
                    "feeAssignmentId must not be null"
            );
        }

        if (request.invoiceDate() == null) {
            throw new IllegalArgumentException(
                    "invoiceDate must not be null"
            );
        }

        if (request.dueDate() != null
                && request.dueDate()
                        .isBefore(
                                request.invoiceDate()
                        )) {
            throw new IllegalArgumentException(
                    "dueDate must not be before invoiceDate."
            );
        }

        String normalizedActor =
                requireText(
                        actor,
                        "actor"
                );

        String invoiceNumber =
                requireText(
                        request.invoiceNumber(),
                        "invoiceNumber"
                );

        if (invoiceNumber.length() > 100) {
            throw new IllegalArgumentException(
                    "invoiceNumber must not exceed 100 characters."
            );
        }

        if (repository.existsInvoiceNumber(
                tenantId,
                invoiceNumber
        )) {
            throw new IllegalStateException(
                    "Invoice number already exists in this tenant."
            );
        }

        StudentFeeAssignmentView assignment =
                assignments
                        .findStudentFeeAssignment(
                                tenantId,
                                request.feeAssignmentId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Fee assignment is not available in this tenant."
                                )
                        );

        if (!"ACTIVE".equals(
                assignment.assignmentStatus()
        ) || !"ACTIVE".equals(
                assignment.status()
        )) {
            throw new IllegalStateException(
                    "Only an active fee assignment may be invoiced."
            );
        }

        requireDateInsideRange(
                request.invoiceDate(),
                assignment.effectiveFrom(),
                assignment.effectiveTo(),
                "Invoice date is outside the fee assignment effective period."
        );

        FeeStructureView structure =
                foundation
                        .listFeeStructures(
                                tenantId
                        )
                        .stream()
                        .filter(
                                candidate ->
                                        candidate.id()
                                                .equals(
                                                        assignment.feeStructureId()
                                                )
                        )
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Fee structure is not available in this tenant."
                                )
                        );

        if (!"ACTIVE".equals(
                structure.structureStatus()
        ) || !"ACTIVE".equals(
                structure.status()
        )) {
            throw new IllegalStateException(
                    "Only an active fee structure may be invoiced."
            );
        }

        requireDateInsideRange(
                request.invoiceDate(),
                structure.effectiveFrom(),
                structure.effectiveTo(),
                "Invoice date is outside the fee structure effective period."
        );

        StudentFinancialAccountView account =
                foundation
                        .findStudentAccount(
                                tenantId,
                                assignment.studentId(),
                                structure.currencyCode()
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "The student requires an active financial account in the invoice currency."
                                )
                        );

        if (!account.id().equals(
                assignment.studentFinancialAccountId()
        )) {
            throw new IllegalStateException(
                    "Fee assignment financial account does not match the learner financial account."
            );
        }

        if (!assignment.studentId().equals(
                account.studentId()
        )) {
            throw new IllegalStateException(
                    "Fee assignment learner does not match the financial account learner."
            );
        }

        if (assignment.studentEnrollmentId() != null
                && !assignment.studentEnrollmentId()
                        .equals(
                                account.studentEnrollmentId()
                        )) {
            throw new IllegalStateException(
                    "Fee assignment enrollment does not match the financial account enrollment."
            );
        }

        if (!"ACTIVE".equals(
                account.billingStatus()
        ) || !"ACTIVE".equals(
                account.status()
        )) {
            throw new IllegalStateException(
                    "The student financial account is not active."
            );
        }

        if (!structure.currencyCode()
                .equalsIgnoreCase(
                        account.currencyCode()
                )) {
            throw new IllegalStateException(
                    "Fee structure and financial account currencies do not match."
            );
        }

        List<FeeStructureItemView> activeStructureItems =
                foundation
                        .listFeeStructureItems(
                                tenantId,
                                structure.id()
                        )
                        .stream()
                        .filter(
                                item ->
                                        "ACTIVE".equals(
                                                item.status()
                                        )
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        FeeStructureItemView::sequenceNumber
                                )
                        )
                        .toList();

        if (activeStructureItems.isEmpty()) {
            throw new IllegalStateException(
                    "The active fee structure contains no active invoiceable items."
            );
        }

        Map<UUID, FeeItemView> feeItems =
                foundation
                        .listFeeItems(
                                tenantId
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        FeeItemView::id,
                                        Function.identity()
                                )
                        );

        List<DraftInvoiceLine> lines =
                activeStructureItems
                        .stream()
                        .map(
                                structureItem ->
                                        toDraftLine(
                                                structure,
                                                structureItem,
                                                feeItems,
                                                request.dueDate()
                                        )
                        )
                        .toList();

        BigDecimal total =
                lines
                        .stream()
                        .map(
                                DraftInvoiceLine::netAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return repository.createDraftInvoice(
                tenantId,
                invoiceNumber,
                assignment,
                structure,
                account,
                request.invoiceDate(),
                request.dueDate(),
                total,
                lines,
                normalizedActor
        );
    }

    @org.springframework.transaction.annotation.Transactional
    public StudentInvoiceView issueStudentInvoice(
            UUID tenantId,
            UUID invoiceId,
            UUID issuerId,
            String actor
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (invoiceId == null) {
            throw new IllegalArgumentException(
                    "invoiceId must not be null"
            );
        }

        if (issuerId == null) {
            throw new IllegalArgumentException(
                    "issuerId must not be null"
            );
        }

        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException(
                    "actor must not be blank"
            );
        }

        StudentInvoiceView existing =
                repository.findStudentInvoice(
                        tenantId,
                        invoiceId
                ).orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Student invoice is not available in this tenant."
                                )
                );

        if (
                !"DRAFT".equals(
                        existing.invoiceStatus()
                )
                || !"ACTIVE".equals(
                        existing.status()
                )
        ) {
            throw new IllegalStateException(
                    "Only an active draft invoice may be issued."
            );
        }

        return repository.issueStudentInvoice(
                tenantId,
                invoiceId,
                issuerId,
                actor
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Only an active draft invoice may be issued."
                        )
        );
    }

    @Transactional(readOnly = true)
    public Optional<StudentInvoiceView> findStudentInvoice(
            UUID tenantId,
            UUID invoiceId
    ) {

        requireTenant(
                tenantId
        );

        if (invoiceId == null) {
            throw new IllegalArgumentException(
                    "invoiceId must not be null"
            );
        }

        return repository.findStudentInvoice(
                tenantId,
                invoiceId
        );
    }

    @Transactional(readOnly = true)
    public List<StudentInvoiceView> listStudentInvoices(
            UUID tenantId,
            UUID studentId
    ) {

        requireTenant(
                tenantId
        );

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }

        if (!repository.studentExists(
                tenantId,
                studentId
        )) {
            throw new IllegalArgumentException(
                    "studentId is not available in this tenant."
            );
        }

        return repository.listStudentInvoices(
                tenantId,
                studentId
        );
    }

    private static DraftInvoiceLine toDraftLine(
            FeeStructureView structure,
            FeeStructureItemView structureItem,
            Map<UUID, FeeItemView> feeItems,
            LocalDate invoiceDueDate
    ) {

        FeeItemView feeItem =
                Optional.ofNullable(
                        feeItems.get(
                                structureItem.feeItemId()
                        )
                ).orElseThrow(
                        () -> new IllegalStateException(
                                "Fee structure item references a fee item unavailable in this tenant."
                        )
                );

        if (!feeItem.active()
                || !"ACTIVE".equals(
                        feeItem.status()
                )) {
            throw new IllegalStateException(
                    "Only active fee items may be invoiced."
            );
        }

        if (!structure.currencyCode()
                .equalsIgnoreCase(
                        feeItem.currencyCode()
                )) {
            throw new IllegalStateException(
                    "Fee item currency does not match fee structure currency."
            );
        }

        BigDecimal quantity =
                structureItem.quantity();

        BigDecimal unitAmount =
                structureItem.amount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal gross =
                unitAmount
                        .multiply(
                                quantity
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        LocalDate lineDueDate =
                structureItem.dueDate() == null
                        ? invoiceDueDate
                        : structureItem.dueDate();

        return new DraftInvoiceLine(
                feeItem.id(),
                structureItem.id(),
                feeItem.itemName(),
                quantity,
                unitAmount,
                gross,
                gross,
                lineDueDate
        );
    }

    private static void requireDateInsideRange(
            LocalDate value,
            LocalDate from,
            LocalDate to,
            String message
    ) {

        if (from != null
                && value.isBefore(
                        from
                )) {
            throw new IllegalStateException(
                    message
            );
        }

        if (to != null
                && value.isAfter(
                        to
                )) {
            throw new IllegalStateException(
                    message
            );
        }
    }

    private static void requireTenant(
            UUID tenantId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }
    }

    private static String requireText(
            String value,
            String field
    ) {

        if (value == null
                || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value.trim();
    }
}
