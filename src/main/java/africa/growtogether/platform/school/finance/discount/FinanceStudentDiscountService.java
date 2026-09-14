package africa.growtogether.platform.school.finance.discount;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.StudentFinancialAccountView;
import africa.growtogether.platform.school.finance.invoice.FinanceInvoiceJdbcRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static africa.growtogether.platform.school.finance.discount.FinanceStudentDiscountDtos.CreateStudentDiscountRequest;
import static africa.growtogether.platform.school.finance.discount.FinanceStudentDiscountDtos.StudentDiscountRequestView;

@Service
public class FinanceStudentDiscountService {

    private static final int MAX_DISCOUNT_REFERENCE_LENGTH = 100;

    private final FinanceStudentDiscountJdbcRepository repository;
    private final FinanceFoundationJdbcRepository foundationRepository;
    private final FinanceDiscountService discountService;
    private final FinanceInvoiceJdbcRepository invoiceRepository;

    public FinanceStudentDiscountService(
            FinanceStudentDiscountJdbcRepository repository,
            FinanceFoundationJdbcRepository foundationRepository,
            FinanceDiscountService discountService,
            FinanceInvoiceJdbcRepository invoiceRepository
    ) {
        this.repository = repository;
        this.foundationRepository = foundationRepository;
        this.discountService = discountService;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public StudentDiscountRequestView createStudentDiscountRequest(
            UUID tenantId,
            CreateStudentDiscountRequest request,
            UUID actorId
    ) {
        requireTenant(
                tenantId
        );

        if (request == null) {
            throw new IllegalArgumentException(
                    "request must not be null"
            );
        }

        if (actorId == null) {
            throw new IllegalArgumentException(
                    "actorId must not be null"
            );
        }

        String discountReference = normalizeRequiredText(
                request.discountReference(),
                "discountReference",
                MAX_DISCOUNT_REFERENCE_LENGTH
        );

        UUID studentId = requireUuid(
                request.studentId(),
                "studentId"
        );

        UUID studentFinancialAccountId = requireUuid(
                request.studentFinancialAccountId(),
                "studentFinancialAccountId"
        );

        UUID discountSchemeId = requireUuid(
                request.discountSchemeId(),
                "discountSchemeId"
        );

        LocalDate effectiveFrom = request.effectiveFrom();

        if (effectiveFrom == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom must not be null"
            );
        }

        if (
                request.effectiveTo() != null
                        && request.effectiveTo().isBefore(
                                effectiveFrom
                        )
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        if (
                repository.existsStudentDiscountReference(
                        tenantId,
                        discountReference
                )
        ) {
            throw new IllegalArgumentException(
                    "discountReference already exists in this tenant."
            );
        }

        if (
                !foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ) {
            throw new IllegalArgumentException(
                    "studentId is not available in this tenant."
            );
        }

        FinanceStudentDiscountJdbcRepository.StudentFinancialAccountScope account =
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        studentFinancialAccountId
                ).orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Student financial account is not available in this tenant."
                                )
                );

        if (
                !studentId.equals(
                        account.studentId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Student financial account does not belong to the requested student."
            );
        }

        if (
                !"ACTIVE".equals(
                        account.billingStatus()
                )
                        || !"ACTIVE".equals(
                                account.status()
                        )
        ) {
            throw new IllegalArgumentException(
                    "The student financial account is not active."
            );
        }

        FinanceDiscountDtos.FeeDiscountSchemeView scheme =
                discountService.getFeeDiscountScheme(
                        tenantId,
                        discountSchemeId
                );

        if (
                !scheme.active()
                        || !"ACTIVE".equals(
                                scheme.status()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Fee discount scheme is not active."
            );
        }

        return repository.createStudentDiscountRequest(
                tenantId,
                discountReference,
                studentId,
                studentFinancialAccountId,
                discountSchemeId,
                effectiveFrom,
                request.effectiveTo(),
                request.evidenceDocumentId(),
                request.workflowInstanceId(),
                actorId,
                actorId.toString()
        );
    }


    @Transactional
    public StudentDiscountRequestView approveStudentDiscountRequest(
            UUID tenantId,
            UUID studentDiscountId,
            UUID approverId,
            String actor
    ) {
        requireTenant(
                tenantId
        );

        UUID decisionId =
                requireUuid(
                        studentDiscountId,
                        "studentDiscountId"
                );

        UUID decisionActorId =
                requireUuid(
                        approverId,
                        "approverId"
                );

        String decisionActor =
                requireActor(
                        actor
                );

        StudentDiscountRequestView request =
                repository.getStudentDiscountRequest(
                        tenantId,
                        decisionId
                ).orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Student discount request is not available in this tenant."
                                )
                );

        if (!"ACTIVE".equals(request.status())) {
            throw new IllegalStateException(
                    "Student discount request is not active."
            );
        }

        if (!"PENDING".equals(request.discountStatus())) {
            throw new IllegalStateException(
                    "Student discount request is not pending."
            );
        }

        if (
                !foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        request.studentId()
                )
        ) {
            throw new IllegalArgumentException(
                    "studentId is not available in this tenant."
            );
        }

        FinanceStudentDiscountJdbcRepository.StudentFinancialAccountScope account =
                repository.findStudentFinancialAccountScope(
                        tenantId,
                        request.studentFinancialAccountId()
                ).orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Student financial account is not available in this tenant."
                                )
                );

        if (
                !request.studentId().equals(
                        account.studentId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Student financial account does not belong to the requested student."
            );
        }

        if (
                !"ACTIVE".equals(
                        account.billingStatus()
                )
                        || !"ACTIVE".equals(
                                account.status()
                        )
        ) {
            throw new IllegalArgumentException(
                    "The student financial account is not active."
            );
        }

        FinanceDiscountDtos.FeeDiscountSchemeView scheme =
                discountService.getFeeDiscountScheme(
                        tenantId,
                        request.discountSchemeId()
                );

        if (
                !scheme.active()
                        || !"ACTIVE".equals(
                                scheme.status()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Fee discount scheme is not active."
            );
        }

        if (scheme.discountValue() == null) {
            throw new IllegalStateException(
                    "Fee discount scheme discountValue is required for approval."
            );
        }

        return repository.approveStudentDiscountRequest(
                tenantId,
                decisionId,
                request.version(),
                scheme.discountValue(),
                decisionActorId,
                decisionActor
        );
    }


    @Transactional
    public StudentDiscountRequestView applyStudentDiscount(
            UUID tenantId,
            UUID studentDiscountId,
            FinanceStudentDiscountDtos.ApplyStudentDiscountRequest request,
            UUID applyingUserId,
            String actor
    ) {

        requireTenant(
                tenantId
        );

        UUID discountId =
                requireUuid(
                        studentDiscountId,
                        "studentDiscountId"
                );

        if (request == null) {
            throw new IllegalArgumentException(
                    "request must not be null"
            );
        }

        UUID invoiceId =
                requireUuid(
                        request.invoiceId(),
                        "invoiceId"
                );

        UUID applicationUserId =
                requireUuid(
                        applyingUserId,
                        "applyingUserId"
                );

        String applicationActor =
                requireActor(
                        actor
                );

        StudentDiscountRequestView discount =
                repository.getStudentDiscountRequest(
                        tenantId,
                        discountId
                ).orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Student discount request is not available in this tenant."
                                )
                );

        if (
                !"ACTIVE".equals(
                        discount.status()
                )
                        || !"APPROVED".equals(
                                discount.discountStatus()
                        )
        ) {
            throw new IllegalStateException(
                    "Only an active approved student discount may be applied."
            );
        }

        if (discount.approvedDiscountValue() == null) {
            throw new IllegalStateException(
                    "Approved discount value is required before billing application."
            );
        }

        if (discount.approvedDiscountAmount() != null) {
            throw new IllegalStateException(
                    "Student discount has already been applied to billing."
            );
        }

        if (
                !foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        discount.studentId()
                )
        ) {
            throw new IllegalArgumentException(
                    "studentId is not available in this tenant."
            );
        }

        FinanceInvoiceJdbcRepository.S3DiscountInvoiceSnapshot invoice =
                invoiceRepository.findDiscountApplicationInvoice(
                        tenantId,
                        invoiceId
                ).orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Student invoice is not available in this tenant."
                                )
                );

        if (
                !"ACTIVE".equals(
                        invoice.status()
                )
                        || !"DRAFT".equals(
                                invoice.invoiceStatus()
                        )
        ) {
            throw new IllegalStateException(
                    "Only an active draft invoice may receive a student discount."
            );
        }

        if (
                invoice.paidAmount() == null
                        || invoice.paidAmount()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) != 0
        ) {
            throw new IllegalStateException(
                    "Only an unpaid draft invoice may receive a student discount."
            );
        }

        if (
                !discount.studentId()
                        .equals(
                                invoice.studentId()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Student discount and invoice learners do not match."
            );
        }

        if (
                !discount.studentFinancialAccountId()
                        .equals(
                                invoice.studentFinancialAccountId()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Student discount and invoice financial accounts do not match."
            );
        }

        requireDateInsideRange(
                invoice.invoiceDate(),
                discount.effectiveFrom(),
                discount.effectiveTo(),
                "Invoice date is outside the student discount effective period."
        );

        StudentFinancialAccountView account =
                foundationRepository.findStudentAccount(
                        tenantId,
                        discount.studentId(),
                        invoice.currencyCode()
                ).orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Student financial account is not available in the invoice currency."
                                )
                );

        if (
                !discount.studentFinancialAccountId()
                        .equals(
                                account.id()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Invoice currency does not match the student discount financial account."
            );
        }

        if (
                !"ACTIVE".equals(
                        account.billingStatus()
                )
                        || !"ACTIVE".equals(
                                account.status()
                        )
        ) {
            throw new IllegalStateException(
                    "The student financial account is not active."
            );
        }

        FinanceDiscountDtos.FeeDiscountSchemeView scheme =
                discountService.getFeeDiscountScheme(
                        tenantId,
                        discount.discountSchemeId()
                );

        if (
                !scheme.active()
                        || !"ACTIVE".equals(
                                scheme.status()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Fee discount scheme is not active."
            );
        }

        requireDateInsideRange(
                invoice.invoiceDate(),
                scheme.effectiveFrom(),
                scheme.effectiveTo(),
                "Invoice date is outside the fee discount scheme effective period."
        );

        if (
                scheme.discountValue() == null
                        || scheme.discountValue()
                                .compareTo(
                                        discount.approvedDiscountValue()
                                ) != 0
        ) {
            throw new IllegalStateException(
                    "Fee discount scheme value has changed since approval."
            );
        }

        String discountType =
                scheme.discountType();

        if (
                !"PERCENTAGE".equals(
                        discountType
                )
                        && !"FIXED_AMOUNT".equals(
                                discountType
                        )
        ) {
            throw new IllegalStateException(
                    "Discount type does not have an authorised FIN-B4-S3 calculation rule."
            );
        }

        List<FinanceInvoiceJdbcRepository.S3DiscountInvoiceLine> eligibleLines =
                invoice.lines()
                        .stream()
                        .filter(
                                line ->
                                        "ACTIVE".equals(
                                                line.lineStatus()
                                        )
                                                && "ACTIVE".equals(
                                                        line.status()
                                                )
                        )
                        .filter(
                                line ->
                                        lineMatchesScheme(
                                                line,
                                                scheme
                                        )
                        )
                        .filter(
                                line ->
                                        remainingDiscountableBase(
                                                line
                                        ).compareTo(
                                                BigDecimal.ZERO
                                        ) > 0
                        )
                        .toList();

        if (eligibleLines.isEmpty()) {
            throw new IllegalStateException(
                    "The invoice contains no eligible positive discountable amount."
            );
        }

        BigDecimal eligibleBase =
                eligibleLines
                        .stream()
                        .map(
                                FinanceStudentDiscountService::remainingDiscountableBase
                        )
                        .reduce(
                                money(
                                        BigDecimal.ZERO
                                ),
                                BigDecimal::add
                        );

        BigDecimal targetAmount;

        if ("PERCENTAGE".equals(discountType)) {
            targetAmount =
                    money(
                            eligibleBase
                                    .multiply(
                                            discount.approvedDiscountValue()
                                    )
                                    .divide(
                                            new BigDecimal(
                                                    "100"
                                            ),
                                            8,
                                            RoundingMode.HALF_UP
                                    )
                    );
        } else {
            targetAmount =
                    money(
                            discount.approvedDiscountValue()
                    );
        }

        if (scheme.maximumDiscountAmount() != null) {
            targetAmount =
                    minimum(
                            targetAmount,
                            money(
                                    scheme.maximumDiscountAmount()
                            )
                    );
        }

        targetAmount =
                minimum(
                        targetAmount,
                        eligibleBase
                );

        targetAmount =
                money(
                        targetAmount
                );

        if (
                targetAmount.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {
            throw new IllegalStateException(
                    "Calculated student discount amount must be greater than zero."
            );
        }

        List<S3LineApplication> lineApplications =
                calculateLineApplications(
                        eligibleLines,
                        discountType,
                        discount.approvedDiscountValue(),
                        targetAmount
                );

        BigDecimal appliedLineTotal =
                lineApplications
                        .stream()
                        .map(
                                S3LineApplication::increment
                        )
                        .reduce(
                                money(
                                        BigDecimal.ZERO
                                ),
                                BigDecimal::add
                        );

        if (
                appliedLineTotal.compareTo(
                        targetAmount
                ) != 0
        ) {
            throw new IllegalStateException(
                    "Invoice-line discount allocation does not equal the approved application amount."
            );
        }

        Map<UUID, BigDecimal> updatedLineDiscounts =
                new HashMap<>();

        for (S3LineApplication application : lineApplications) {
            updatedLineDiscounts.put(
                    application.lineId(),
                    application.newDiscountAmount()
            );
        }

        BigDecimal invoiceDiscountAmount =
                invoice.lines()
                        .stream()
                        .map(
                                line ->
                                        updatedLineDiscounts.getOrDefault(
                                                line.id(),
                                                money(
                                                        line.discountAmount()
                                                )
                                        )
                        )
                        .reduce(
                                money(
                                        BigDecimal.ZERO
                                ),
                                BigDecimal::add
                        );

        BigDecimal invoiceTotalAmount =
                money(
                        invoice.subtotalAmount()
                                .subtract(
                                        invoiceDiscountAmount
                                )
                                .add(
                                        invoice.taxAmount()
                                )
                );

        BigDecimal invoiceOutstandingAmount =
                money(
                        invoiceTotalAmount
                                .subtract(
                                        invoice.paidAmount()
                                )
                );

        if (
                invoiceDiscountAmount.compareTo(
                        BigDecimal.ZERO
                ) < 0
                        || invoiceTotalAmount.compareTo(
                                BigDecimal.ZERO
                        ) < 0
                        || invoiceOutstandingAmount.compareTo(
                                BigDecimal.ZERO
                        ) < 0
        ) {
            throw new IllegalStateException(
                    "Student discount application would produce a negative invoice amount."
            );
        }

        repository.applyStudentDiscountToBilling(
                tenantId,
                discountId,
                discount.version(),
                targetAmount,
                applicationActor
        );

        for (S3LineApplication application : lineApplications) {
            invoiceRepository.updateDiscountApplicationLine(
                    tenantId,
                    invoiceId,
                    application.lineId(),
                    application.expectedVersion(),
                    application.newDiscountAmount(),
                    application.newNetAmount(),
                    applicationActor
            );
        }

        invoiceRepository.updateDiscountApplicationInvoice(
                tenantId,
                invoiceId,
                invoice.version(),
                invoiceDiscountAmount,
                invoiceTotalAmount,
                invoiceOutstandingAmount,
                applicationActor
        );

        repository.insertAppliedStudentDiscountAdjustment(
                tenantId,
                discountId,
                discount.studentFinancialAccountId(),
                invoiceId,
                targetAmount,
                applicationUserId,
                applicationActor
        );

        return repository.getStudentDiscountRequest(
                tenantId,
                discountId
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Applied student discount could not be reloaded."
                        )
        );
    }

    @Transactional
    public StudentDiscountRequestView rejectStudentDiscountRequest(
            UUID tenantId,
            UUID studentDiscountId,
            UUID actorId,
            String actor
    ) {
        requireTenant(
                tenantId
        );

        UUID decisionId =
                requireUuid(
                        studentDiscountId,
                        "studentDiscountId"
                );

        requireUuid(
                actorId,
                "actorId"
        );

        String decisionActor =
                requireActor(
                        actor
                );

        StudentDiscountRequestView request =
                repository.getStudentDiscountRequest(
                        tenantId,
                        decisionId
                ).orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Student discount request is not available in this tenant."
                                )
                );

        if (!"ACTIVE".equals(request.status())) {
            throw new IllegalStateException(
                    "Student discount request is not active."
            );
        }

        if (!"PENDING".equals(request.discountStatus())) {
            throw new IllegalStateException(
                    "Student discount request is not pending."
            );
        }

        return repository.rejectStudentDiscountRequest(
                tenantId,
                decisionId,
                request.version(),
                decisionActor
        );
    }

    @Transactional(readOnly = true)
    public StudentDiscountRequestView getStudentDiscountRequest(
            UUID tenantId,
            UUID studentDiscountId
    ) {
        requireTenant(
                tenantId
        );

        if (studentDiscountId == null) {
            throw new IllegalArgumentException(
                    "studentDiscountId must not be null"
            );
        }

        return repository.getStudentDiscountRequest(
                tenantId,
                studentDiscountId
        ).orElseThrow(
                () ->
                        new IllegalArgumentException(
                                "Student discount request is not available in this tenant."
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<StudentDiscountRequestView> listStudentDiscountRequests(
            UUID tenantId
    ) {
        requireTenant(
                tenantId
        );

        return repository.listStudentDiscountRequests(
                tenantId
        );
    }


    private static String requireActor(
            String actor
    ) {
        if (
                actor == null
                        || actor.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "actor must not be blank"
            );
        }

        return actor;
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

    private static UUID requireUuid(
            UUID value,
            String field
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    field + " must not be null"
            );
        }

        return value;
    }

    private static String normalizeRequiredText(
            String value,
            String field,
            int maxLength
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    field + " must not be null"
            );
        }

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " exceeds maximum length " + maxLength
            );
        }

        return normalized;
    }

    private static List<S3LineApplication> calculateLineApplications(
            List<FinanceInvoiceJdbcRepository.S3DiscountInvoiceLine> lines,
            String discountType,
            BigDecimal approvedValue,
            BigDecimal targetAmount
    ) {

        List<BigDecimal> increments =
                new ArrayList<>(
                        lines.size()
                );

        if ("FIXED_AMOUNT".equals(discountType)) {
            BigDecimal remaining =
                    targetAmount;

            for (FinanceInvoiceJdbcRepository.S3DiscountInvoiceLine line : lines) {
                BigDecimal capacity =
                        remainingDiscountableBase(
                                line
                        );

                BigDecimal increment =
                        minimum(
                                capacity,
                                remaining
                        );

                increment =
                        money(
                                increment
                        );

                increments.add(
                        increment
                );

                remaining =
                        money(
                                remaining.subtract(
                                        increment
                                )
                        );
            }

            if (
                    remaining.compareTo(
                            BigDecimal.ZERO
                    ) != 0
            ) {
                throw new IllegalStateException(
                        "Fixed discount could not be fully allocated to eligible invoice lines."
                );
            }
        } else {
            BigDecimal initialTotal =
                    money(
                            BigDecimal.ZERO
                    );

            for (FinanceInvoiceJdbcRepository.S3DiscountInvoiceLine line : lines) {
                BigDecimal capacity =
                        remainingDiscountableBase(
                                line
                        );

                BigDecimal increment =
                        money(
                                capacity
                                        .multiply(
                                                approvedValue
                                        )
                                        .divide(
                                                new BigDecimal(
                                                        "100"
                                                ),
                                                8,
                                                RoundingMode.HALF_UP
                                        )
                        );

                increment =
                        minimum(
                                increment,
                                capacity
                        );

                increments.add(
                        increment
                );

                initialTotal =
                        money(
                                initialTotal.add(
                                        increment
                                )
                        );
            }

            BigDecimal difference =
                    money(
                            targetAmount.subtract(
                                    initialTotal
                            )
                    );

            if (
                    difference.compareTo(
                            BigDecimal.ZERO
                    ) > 0
            ) {
                for (
                        int index = lines.size() - 1;
                        index >= 0
                                && difference.compareTo(
                                        BigDecimal.ZERO
                                ) > 0;
                        index--
                ) {
                    BigDecimal capacity =
                            money(
                                    remainingDiscountableBase(
                                            lines.get(
                                                    index
                                            )
                                    ).subtract(
                                            increments.get(
                                                    index
                                            )
                                    )
                            );

                    BigDecimal addition =
                            minimum(
                                    capacity,
                                    difference
                            );

                    increments.set(
                            index,
                            money(
                                    increments.get(
                                            index
                                    ).add(
                                            addition
                                    )
                            )
                    );

                    difference =
                            money(
                                    difference.subtract(
                                            addition
                                    )
                            );
                }
            } else if (
                    difference.compareTo(
                            BigDecimal.ZERO
                    ) < 0
            ) {
                BigDecimal excess =
                        difference
                                .abs();

                for (
                        int index = lines.size() - 1;
                        index >= 0
                                && excess.compareTo(
                                        BigDecimal.ZERO
                                ) > 0;
                        index--
                ) {
                    BigDecimal removable =
                            increments.get(
                                    index
                            );

                    BigDecimal deduction =
                            minimum(
                                    removable,
                                    excess
                            );

                    increments.set(
                            index,
                            money(
                                    removable.subtract(
                                            deduction
                                    )
                            )
                    );

                    excess =
                            money(
                                    excess.subtract(
                                            deduction
                                    )
                            );
                }

                difference =
                        excess.negate();
            }

            if (
                    difference.compareTo(
                            BigDecimal.ZERO
                    ) != 0
            ) {
                throw new IllegalStateException(
                        "Percentage discount rounding could not be reconciled."
                );
            }
        }

        List<S3LineApplication> result =
                new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            FinanceInvoiceJdbcRepository.S3DiscountInvoiceLine line =
                    lines.get(
                            index
                    );

            BigDecimal increment =
                    money(
                            increments.get(
                                    index
                            )
                    );

            if (
                    increment.compareTo(
                            BigDecimal.ZERO
                    ) <= 0
            ) {
                continue;
            }

            BigDecimal newDiscount =
                    money(
                            line.discountAmount()
                                    .add(
                                            increment
                                    )
                    );

            if (
                    newDiscount.compareTo(
                            line.grossAmount()
                    ) > 0
            ) {
                throw new IllegalStateException(
                        "Student discount would exceed an invoice line gross amount."
                );
            }

            BigDecimal newNet =
                    money(
                            line.grossAmount()
                                    .subtract(
                                            newDiscount
                                    )
                                    .add(
                                            line.taxAmount()
                                    )
                    );

            result.add(
                    new S3LineApplication(
                            line.id(),
                            line.version(),
                            increment,
                            newDiscount,
                            newNet
                    )
            );
        }

        return List.copyOf(
                result
        );
    }

    private static boolean lineMatchesScheme(
            FinanceInvoiceJdbcRepository.S3DiscountInvoiceLine line,
            FinanceDiscountDtos.FeeDiscountSchemeView scheme
    ) {

        if (scheme.feeItemId() != null) {
            return scheme.feeItemId()
                    .equals(
                            line.feeItemId()
                    );
        }

        if (scheme.feeCategoryId() != null) {
            return scheme.feeCategoryId()
                    .equals(
                            line.feeCategoryId()
                    );
        }

        return true;
    }

    private static BigDecimal remainingDiscountableBase(
            FinanceInvoiceJdbcRepository.S3DiscountInvoiceLine line
    ) {

        BigDecimal remaining =
                money(
                        line.grossAmount()
                                .subtract(
                                        line.discountAmount()
                                )
                );

        return remaining.compareTo(
                BigDecimal.ZERO
        ) < 0
                ? money(
                        BigDecimal.ZERO
                )
                : remaining;
    }

    private static BigDecimal minimum(
            BigDecimal first,
            BigDecimal second
    ) {
        return first.compareTo(
                second
        ) <= 0
                ? first
                : second;
    }

    private static BigDecimal money(
            BigDecimal value
    ) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "monetary value must not be null"
            );
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private static void requireDateInsideRange(
            LocalDate date,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String message
    ) {

        if (
                date == null
                        || effectiveFrom == null
                        || date.isBefore(
                                effectiveFrom
                        )
                        || (
                                effectiveTo != null
                                        && date.isAfter(
                                                effectiveTo
                                        )
                        )
        ) {
            throw new IllegalStateException(
                    message
            );
        }
    }

    private record S3LineApplication(
            UUID lineId,
            long expectedVersion,
            BigDecimal increment,
            BigDecimal newDiscountAmount,
            BigDecimal newNetAmount
    ) {
    }

}
