package africa.growtogether.platform.school.finance.foundation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class FinanceFoundationDtos {

    private FinanceFoundationDtos() {
    }


    public record CreateFeeCategoryRequest(
            String categoryCode,
            String categoryName,
            String description,
            String categoryType,
            String accountingCode,
            Boolean refundable,
            Boolean mandatoryByDefault,
            Boolean recurring
    ) {
    }


    public record FeeCategoryView(
            UUID id,
            UUID tenantId,
            String categoryCode,
            String categoryName,
            String description,
            String categoryType,
            String accountingCode,
            boolean refundable,
            boolean mandatoryByDefault,
            boolean recurring,
            boolean active,
            String status
    ) {
    }


    public record CreateFeeItemRequest(
            UUID feeCategoryId,
            String itemCode,
            String itemName,
            String description,
            String currencyCode,
            BigDecimal defaultAmount,
            String chargeFrequency,
            Boolean quantityAllowed,
            Boolean partialPaymentAllowed,
            Boolean taxApplicable,
            String taxCode
    ) {
    }


    public record FeeItemView(
            UUID id,
            UUID tenantId,
            UUID feeCategoryId,
            String itemCode,
            String itemName,
            String description,
            String currencyCode,
            BigDecimal defaultAmount,
            String chargeFrequency,
            boolean quantityAllowed,
            boolean partialPaymentAllowed,
            boolean taxApplicable,
            String taxCode,
            boolean active,
            String status
    ) {
    }


    public record CreateFeeStructureRequest(
            String structureCode,
            String structureName,
            String description,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID academicProgrammeId,
            UUID studyTrackId,
            UUID classGradeId,
            UUID streamId,
            String currencyCode,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
    }


    public record FeeStructureView(
            UUID id,
            UUID tenantId,
            String structureCode,
            String structureName,
            String description,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID academicProgrammeId,
            UUID studyTrackId,
            UUID classGradeId,
            UUID streamId,
            String currencyCode,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            UUID workflowInstanceId,
            UUID approvedBy,
            String structureStatus,
            String status
    ) {
    }


    public record AddFeeStructureItemRequest(
            UUID feeItemId,
            BigDecimal amount,
            BigDecimal quantity,
            Boolean mandatory,
            Boolean refundable,
            LocalDate dueDate,
            Integer sequenceNumber
    ) {
    }


    public record FeeStructureItemView(
            UUID id,
            UUID tenantId,
            UUID feeStructureId,
            UUID feeItemId,
            BigDecimal amount,
            BigDecimal quantity,
            boolean mandatory,
            boolean refundable,
            LocalDate dueDate,
            int sequenceNumber,
            String status
    ) {
    }


    public record OpenStudentFinancialAccountRequest(
            String accountNumber,
            UUID studentId,
            UUID studentEnrollmentId,
            String currencyCode,
            BigDecimal openingBalance,
            BigDecimal creditLimit
    ) {
    }


    public record StudentFinancialAccountView(
            UUID id,
            UUID tenantId,
            String accountNumber,
            UUID studentId,
            UUID studentEnrollmentId,
            String currencyCode,
            BigDecimal openingBalance,
            BigDecimal currentBalance,
            BigDecimal creditBalance,
            BigDecimal creditLimit,
            String billingStatus,
            String status
    ) {
    }
}
