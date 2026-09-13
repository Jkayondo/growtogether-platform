package africa.growtogether.platform.school.finance.discount;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class FinanceStudentDiscountDtos {

    private FinanceStudentDiscountDtos() {
    }

    public record CreateStudentDiscountRequest(
            String discountReference,
            UUID studentId,
            UUID studentFinancialAccountId,
            UUID discountSchemeId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            UUID evidenceDocumentId,
            UUID workflowInstanceId
    ) {
    }

    public record StudentDiscountRequestView(
            UUID id,
            UUID tenantId,
            String discountReference,
            UUID studentId,
            UUID studentFinancialAccountId,
            UUID discountSchemeId,
            BigDecimal approvedDiscountValue,
            BigDecimal approvedDiscountAmount,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            UUID evidenceDocumentId,
            UUID workflowInstanceId,
            Instant requestedAt,
            UUID requestedBy,
            Instant approvedAt,
            UUID approvedBy,
            String discountStatus,
            String status,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            long version
    ) {
    }
}
