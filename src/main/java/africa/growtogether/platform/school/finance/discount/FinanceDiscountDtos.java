package africa.growtogether.platform.school.finance.discount;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public final class FinanceDiscountDtos {

    private FinanceDiscountDtos() {
    }

    public record CreateFeeDiscountSchemeRequest(
            String schemeCode,
            String schemeName,
            String description,
            String discountType,
            BigDecimal discountValue,
            UUID feeCategoryId,
            UUID feeItemId,
            BigDecimal maximumDiscountAmount,
            Map<String, Object> eligibilityRules,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Boolean approvalRequired
    ) {
    }

    public record FeeDiscountSchemeView(
            UUID id,
            UUID tenantId,
            String schemeCode,
            String schemeName,
            String description,
            String discountType,
            BigDecimal discountValue,
            UUID feeCategoryId,
            UUID feeItemId,
            BigDecimal maximumDiscountAmount,
            Map<String, Object> eligibilityRules,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            boolean approvalRequired,
            boolean active,
            String status,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            long version
    ) {
    }
}
