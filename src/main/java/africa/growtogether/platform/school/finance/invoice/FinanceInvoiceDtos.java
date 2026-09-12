package africa.growtogether.platform.school.finance.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class FinanceInvoiceDtos {

    private FinanceInvoiceDtos() {
    }

    public record CreateDraftInvoiceRequest(
            String invoiceNumber,
            UUID feeAssignmentId,
            LocalDate invoiceDate,
            LocalDate dueDate
    ) {
    }

    public record StudentInvoiceLineView(
            UUID id,
            UUID tenantId,
            UUID invoiceId,
            UUID feeItemId,
            UUID feeStructureItemId,
            String lineDescription,
            BigDecimal quantity,
            BigDecimal unitAmount,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal netAmount,
            LocalDate dueDate,
            String lineStatus,
            String status
    ) {
    }

    public record StudentInvoiceView(
            UUID id,
            UUID tenantId,
            String invoiceNumber,
            UUID studentFinancialAccountId,
            UUID studentId,
            UUID studentEnrollmentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID feeStructureId,
            LocalDate invoiceDate,
            LocalDate dueDate,
            String currencyCode,
            BigDecimal subtotalAmount,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount,
            String invoiceStatus,
            String status,
            List<StudentInvoiceLineView> lines
    ) {
    }
}
