package africa.growtogether.platform.school.finance.discount;

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

    public FinanceStudentDiscountService(
            FinanceStudentDiscountJdbcRepository repository,
            FinanceFoundationJdbcRepository foundationRepository,
            FinanceDiscountService discountService
    ) {
        this.repository = repository;
        this.foundationRepository = foundationRepository;
        this.discountService = discountService;
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
}
