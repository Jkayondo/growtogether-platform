package africa.growtogether.platform.school.finance.discount;

import static africa.growtogether.platform.school.finance.discount.FinanceDiscountDtos.*;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationJdbcRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FinanceDiscountService {

    private static final Set<String> ALLOWED_DISCOUNT_TYPES =
            Set.of(
                    "PERCENTAGE",
                    "FIXED_AMOUNT",
                    "SIBLING",
                    "STAFF_CHILD",
                    "EARLY_PAYMENT",
                    "SCHOLARSHIP",
                    "BURSARY",
                    "SPONSORSHIP",
                    "WAIVER",
                    "OTHER"
            );

    private final FinanceDiscountJdbcRepository repository;
    private final FinanceFoundationJdbcRepository foundationRepository;

    public FinanceDiscountService(
            FinanceDiscountJdbcRepository repository,
            FinanceFoundationJdbcRepository foundationRepository
    ) {
        this.repository = repository;
        this.foundationRepository = foundationRepository;
    }

    @Transactional
    public FeeDiscountSchemeView createFeeDiscountScheme(
            UUID tenantId,
            CreateFeeDiscountSchemeRequest request,
            String actor
    ) {

        requireTenant(
                tenantId
        );

        requireActor(
                actor
        );

        requireRequest(
                request
        );

        String schemeCode =
                requiredText(
                        request.schemeCode(),
                        "schemeCode",
                        100
                );

        String schemeName =
                requiredText(
                        request.schemeName(),
                        "schemeName",
                        250
                );

        String description =
                optionalText(
                        request.description(),
                        "description",
                        1200
                );

        String discountType =
                requiredText(
                        request.discountType(),
                        "discountType",
                        40
                ).toUpperCase(
                        Locale.ROOT
                );

        if (
                !ALLOWED_DISCOUNT_TYPES.contains(
                        discountType
                )
        ) {
            throw new IllegalArgumentException(
                    "discountType is not supported"
            );
        }

        requireNonNegative(
                request.discountValue(),
                "discountValue",
                false
        );

        if (
                "PERCENTAGE".equals(
                        discountType
                )
                && request.discountValue()
                        .compareTo(
                                new BigDecimal(
                                        "100"
                                )
                        ) > 0
        ) {
            throw new IllegalArgumentException(
                    "discountValue must not exceed 100 for PERCENTAGE."
            );
        }

        requireNonNegative(
                request.maximumDiscountAmount(),
                "maximumDiscountAmount",
                true
        );

        if (request.effectiveFrom() == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom must not be null"
            );
        }

        if (
                request.effectiveTo() != null
                && request.effectiveTo()
                        .isBefore(
                                request.effectiveFrom()
                        )
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        if (
                repository.existsFeeDiscountSchemeCode(
                        tenantId,
                        schemeCode
                )
        ) {
            throw new IllegalArgumentException(
                    "Fee discount scheme code already exists in this tenant."
            );
        }

        if (request.feeCategoryId() != null) {

            foundationRepository.findFeeCategory(
                    tenantId,
                    request.feeCategoryId()
            ).orElseThrow(
                    () ->
                            new IllegalArgumentException(
                                    "feeCategoryId is not available in this tenant."
                            )
            );
        }

        if (request.feeItemId() != null) {

            foundationRepository.findFeeItem(
                    tenantId,
                    request.feeItemId()
            ).orElseThrow(
                    () ->
                            new IllegalArgumentException(
                                    "feeItemId is not available in this tenant."
                            )
            );
        }

        Map<String, Object> eligibilityRules =
                request.eligibilityRules() == null
                        ? Map.of()
                        : new LinkedHashMap<>(
                                request.eligibilityRules()
                        );

        boolean approvalRequired =
                request.approvalRequired() == null
                        || request.approvalRequired();

        CreateFeeDiscountSchemeRequest normalized =
                new CreateFeeDiscountSchemeRequest(
                        schemeCode,
                        schemeName,
                        description,
                        discountType,
                        request.discountValue(),
                        request.feeCategoryId(),
                        request.feeItemId(),
                        request.maximumDiscountAmount(),
                        eligibilityRules,
                        request.effectiveFrom(),
                        request.effectiveTo(),
                        approvalRequired
                );

        return repository.createFeeDiscountScheme(
                tenantId,
                normalized,
                actor
        );
    }

    @Transactional(readOnly = true)
    public FeeDiscountSchemeView getFeeDiscountScheme(
            UUID tenantId,
            UUID discountSchemeId
    ) {

        requireTenant(
                tenantId
        );

        if (discountSchemeId == null) {
            throw new IllegalArgumentException(
                    "discountSchemeId must not be null"
            );
        }

        return repository.getFeeDiscountScheme(
                tenantId,
                discountSchemeId
        ).orElseThrow(
                () ->
                        new IllegalArgumentException(
                                "Fee discount scheme is not available in this tenant."
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<FeeDiscountSchemeView> listFeeDiscountSchemes(
            UUID tenantId
    ) {

        requireTenant(
                tenantId
        );

        return repository.listFeeDiscountSchemes(
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

    private static void requireActor(
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
    }

    private static void requireRequest(
            Object request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "request must not be null"
            );
        }
    }

    private static String requiredText(
            String value,
            String field,
            int maximumLength
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    field
                            + " must not be blank"
            );
        }

        String normalized =
                value.trim();

        if (
                normalized.length()
                > maximumLength
        ) {
            throw new IllegalArgumentException(
                    field
                            + " must not exceed "
                            + maximumLength
                            + " characters"
            );
        }

        return normalized;
    }

    private static String optionalText(
            String value,
            String field,
            int maximumLength
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            return null;
        }

        String normalized =
                value.trim();

        if (
                normalized.length()
                > maximumLength
        ) {
            throw new IllegalArgumentException(
                    field
                            + " must not exceed "
                            + maximumLength
                            + " characters"
            );
        }

        return normalized;
    }

    private static void requireNonNegative(
            BigDecimal value,
            String field,
            boolean nullable
    ) {

        if (value == null) {

            if (nullable) {
                return;
            }

            throw new IllegalArgumentException(
                    field
                            + " must not be null"
            );
        }

        if (
                value.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    field
                            + " must not be negative"
            );
        }
    }
}
