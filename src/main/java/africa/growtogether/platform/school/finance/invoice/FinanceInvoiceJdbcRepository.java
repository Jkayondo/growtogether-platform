package africa.growtogether.platform.school.finance.invoice;

import static africa.growtogether.platform.school.finance.invoice.FinanceInvoiceDtos.*;

import africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.StudentFeeAssignmentView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.FeeStructureView;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.StudentFinancialAccountView;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FinanceInvoiceJdbcRepository {

    private final JdbcTemplate jdbc;

    public FinanceInvoiceJdbcRepository(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public record DraftInvoiceLine(
            UUID feeItemId,
            UUID feeStructureItemId,
            String description,
            BigDecimal quantity,
            BigDecimal unitAmount,
            BigDecimal grossAmount,
            BigDecimal netAmount,
            LocalDate dueDate
    ) {
    }


    public record S3DiscountInvoiceLine(
            UUID id,
            UUID feeItemId,
            UUID feeCategoryId,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            String lineStatus,
            String status,
            long version
    ) {
    }

    public record S3DiscountInvoiceSnapshot(
            UUID id,
            UUID studentFinancialAccountId,
            UUID studentId,
            LocalDate invoiceDate,
            String currencyCode,
            BigDecimal subtotalAmount,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount,
            String invoiceStatus,
            String status,
            long version,
            List<S3DiscountInvoiceLine> lines
    ) {
    }

    @Transactional(readOnly = true)
    public boolean existsInvoiceNumber(
            UUID tenantId,
            String invoiceNumber
    ) {
        Boolean result =
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_student_invoice
                            WHERE tenant_id = ?
                              AND invoice_number = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        invoiceNumber
                );

        return Boolean.TRUE.equals(result);
    }

    @Transactional(readOnly = true)
    public boolean studentExists(
            UUID tenantId,
            UUID studentId
    ) {
        Boolean result =
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_student
                            WHERE tenant_id = ?
                              AND id = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        studentId
                );

        return Boolean.TRUE.equals(result);
    }

    @Transactional
    public StudentInvoiceView createDraftInvoice(
            UUID tenantId,
            String invoiceNumber,
            StudentFeeAssignmentView assignment,
            FeeStructureView structure,
            StudentFinancialAccountView account,
            LocalDate invoiceDate,
            LocalDate dueDate,
            BigDecimal totalAmount,
            List<DraftInvoiceLine> lines,
            String actor
    ) {

        UUID invoiceId =
                jdbc.queryForObject(
                        """
                        INSERT INTO gts_student_invoice (
                            tenant_id,
                            invoice_number,
                            student_financial_account_id,
                            student_id,
                            student_enrollment_id,
                            academic_year_id,
                            academic_term_id,
                            fee_structure_id,
                            invoice_date,
                            due_date,
                            currency_code,
                            subtotal_amount,
                            discount_amount,
                            tax_amount,
                            total_amount,
                            paid_amount,
                            outstanding_amount,
                            invoice_status,
                            status,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by,
                            version
                        )
                        VALUES (
                            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                            ?, 0, 0, ?, 0, ?,
                            'DRAFT',
                            'ACTIVE',
                            CURRENT_TIMESTAMP,
                            ?,
                            CURRENT_TIMESTAMP,
                            ?,
                            0
                        )
                        RETURNING id
                        """,
                        UUID.class,
                        tenantId,
                        invoiceNumber,
                        account.id(),
                        assignment.studentId(),
                        assignment.studentEnrollmentId(),
                        structure.academicYearId(),
                        structure.academicTermId(),
                        structure.id(),
                        invoiceDate,
                        dueDate,
                        structure.currencyCode(),
                        totalAmount,
                        totalAmount,
                        totalAmount,
                        actor,
                        actor
                );

        if (invoiceId == null) {
            throw new IllegalStateException(
                    "Draft invoice insert did not return an identifier."
            );
        }

        for (DraftInvoiceLine line : lines) {
            jdbc.update(
                    """
                    INSERT INTO gts_student_invoice_line (
                        tenant_id,
                        invoice_id,
                        fee_item_id,
                        fee_structure_item_id,
                        line_description,
                        quantity,
                        unit_amount,
                        gross_amount,
                        discount_amount,
                        tax_amount,
                        net_amount,
                        due_date,
                        line_status,
                        status,
                        created_at,
                        created_by,
                        updated_at,
                        updated_by,
                        version
                    )
                    VALUES (
                        ?, ?, ?, ?, ?, ?, ?, ?,
                        0, 0, ?, ?,
                        'ACTIVE',
                        'ACTIVE',
                        CURRENT_TIMESTAMP,
                        ?,
                        CURRENT_TIMESTAMP,
                        ?,
                        0
                    )
                    """,
                    tenantId,
                    invoiceId,
                    line.feeItemId(),
                    line.feeStructureItemId(),
                    line.description(),
                    line.quantity(),
                    line.unitAmount(),
                    line.grossAmount(),
                    line.netAmount(),
                    line.dueDate(),
                    actor,
                    actor
            );
        }

        return findStudentInvoice(
                tenantId,
                invoiceId
        ).orElseThrow(
                () -> new IllegalStateException(
                        "Created draft invoice could not be reloaded."
                )
        );
    }

    @org.springframework.transaction.annotation.Transactional
    public Optional<StudentInvoiceView> issueStudentInvoice(
            UUID tenantId,
            UUID invoiceId,
            UUID issuerId,
            String actor
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE gts_student_invoice
                        SET
                            invoice_status = 'ISSUED',
                            issued_at = CURRENT_TIMESTAMP,
                            issued_by = ?,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = ?,
                            version = version + 1
                        WHERE tenant_id = ?
                          AND id = ?
                          AND invoice_status = 'DRAFT'
                          AND status = 'ACTIVE'
                        """,
                        issuerId,
                        actor,
                        tenantId,
                        invoiceId
                );

        if (updated != 1) {
            return Optional.empty();
        }

        return findStudentInvoice(
                tenantId,
                invoiceId
        );
    }

    @Transactional
    public Optional<StudentInvoiceView> cancelStudentInvoice(
            UUID tenantId,
            UUID invoiceId,
            UUID cancellerId,
            String cancellationReason,
            String actor
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE gts_student_invoice
                        SET
                            invoice_status = 'CANCELLED',
                            cancelled_at = CURRENT_TIMESTAMP,
                            cancelled_by = ?,
                            cancellation_reason = ?,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = ?,
                            version = version + 1
                        WHERE tenant_id = ?
                          AND id = ?
                          AND invoice_status IN ('DRAFT', 'ISSUED')
                          AND status = 'ACTIVE'
                          AND paid_amount = 0
                        """,
                        cancellerId,
                        cancellationReason,
                        actor,
                        tenantId,
                        invoiceId
                );

        if (updated != 1) {
            return Optional.empty();
        }

        return findStudentInvoice(
                tenantId,
                invoiceId
        );
    }



    @Transactional(readOnly = true)
    public Optional<S3DiscountInvoiceSnapshot> findDiscountApplicationInvoice(
            UUID tenantId,
            UUID invoiceId
    ) {

        List<S3DiscountInvoiceSnapshot> headers =
                jdbc.query(
                        """
                        SELECT
                            id,
                            student_financial_account_id,
                            student_id,
                            invoice_date,
                            currency_code,
                            subtotal_amount,
                            discount_amount,
                            tax_amount,
                            total_amount,
                            paid_amount,
                            outstanding_amount,
                            invoice_status,
                            status,
                            version
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        (rs, rowNum) ->
                                new S3DiscountInvoiceSnapshot(
                                        rs.getObject("id", UUID.class),
                                        rs.getObject(
                                                "student_financial_account_id",
                                                UUID.class
                                        ),
                                        rs.getObject("student_id", UUID.class),
                                        rs.getObject(
                                                "invoice_date",
                                                LocalDate.class
                                        ),
                                        rs.getString("currency_code"),
                                        rs.getBigDecimal("subtotal_amount"),
                                        rs.getBigDecimal("discount_amount"),
                                        rs.getBigDecimal("tax_amount"),
                                        rs.getBigDecimal("total_amount"),
                                        rs.getBigDecimal("paid_amount"),
                                        rs.getBigDecimal("outstanding_amount"),
                                        rs.getString("invoice_status"),
                                        rs.getString("status"),
                                        rs.getLong("version"),
                                        List.of()
                                ),
                        tenantId,
                        invoiceId
                );

        if (headers.isEmpty()) {
            return Optional.empty();
        }

        S3DiscountInvoiceSnapshot header =
                headers.getFirst();

        List<S3DiscountInvoiceLine> lines =
                jdbc.query(
                        """
                        SELECT
                            line.id,
                            line.fee_item_id,
                            item.fee_category_id,
                            line.gross_amount,
                            line.discount_amount,
                            line.tax_amount,
                            line.line_status,
                            line.status,
                            line.version
                        FROM gts_student_invoice_line line
                        LEFT JOIN gts_fee_item item
                          ON item.id = line.fee_item_id
                         AND item.tenant_id = line.tenant_id
                        WHERE line.tenant_id = ?
                          AND line.invoice_id = ?
                        ORDER BY
                            line.created_at ASC,
                            line.id ASC
                        """,
                        (rs, rowNum) ->
                                new S3DiscountInvoiceLine(
                                        rs.getObject("id", UUID.class),
                                        rs.getObject(
                                                "fee_item_id",
                                                UUID.class
                                        ),
                                        rs.getObject(
                                                "fee_category_id",
                                                UUID.class
                                        ),
                                        rs.getBigDecimal("gross_amount"),
                                        rs.getBigDecimal("discount_amount"),
                                        rs.getBigDecimal("tax_amount"),
                                        rs.getString("line_status"),
                                        rs.getString("status"),
                                        rs.getLong("version")
                                ),
                        tenantId,
                        invoiceId
                );

        return Optional.of(
                new S3DiscountInvoiceSnapshot(
                        header.id(),
                        header.studentFinancialAccountId(),
                        header.studentId(),
                        header.invoiceDate(),
                        header.currencyCode(),
                        header.subtotalAmount(),
                        header.discountAmount(),
                        header.taxAmount(),
                        header.totalAmount(),
                        header.paidAmount(),
                        header.outstandingAmount(),
                        header.invoiceStatus(),
                        header.status(),
                        header.version(),
                        List.copyOf(
                                lines
                        )
                )
        );
    }

    @Transactional
    public void updateDiscountApplicationLine(
            UUID tenantId,
            UUID invoiceId,
            UUID invoiceLineId,
            long expectedVersion,
            BigDecimal discountAmount,
            BigDecimal netAmount,
            String actor
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE gts_student_invoice_line
                        SET
                            discount_amount = ?,
                            net_amount = ?,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = ?,
                            version = version + 1
                        WHERE tenant_id = ?
                          AND invoice_id = ?
                          AND id = ?
                          AND line_status = 'ACTIVE'
                          AND status = 'ACTIVE'
                          AND version = ?
                        """,
                        discountAmount,
                        netAmount,
                        actor,
                        tenantId,
                        invoiceId,
                        invoiceLineId,
                        expectedVersion
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Invoice line changed while the student discount was being applied."
            );
        }
    }

    @Transactional
    public void updateDiscountApplicationInvoice(
            UUID tenantId,
            UUID invoiceId,
            long expectedVersion,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            BigDecimal outstandingAmount,
            String actor
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE gts_student_invoice
                        SET
                            discount_amount = ?,
                            total_amount = ?,
                            outstanding_amount = ?,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = ?,
                            version = version + 1
                        WHERE tenant_id = ?
                          AND id = ?
                          AND invoice_status = 'DRAFT'
                          AND status = 'ACTIVE'
                          AND paid_amount = 0
                          AND version = ?
                        """,
                        discountAmount,
                        totalAmount,
                        outstandingAmount,
                        actor,
                        tenantId,
                        invoiceId,
                        expectedVersion
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Student invoice changed before discount application completed."
            );
        }
    }

    @Transactional(readOnly = true)
    public Optional<StudentInvoiceView> findStudentInvoice(
            UUID tenantId,
            UUID invoiceId
    ) {

        List<StudentInvoiceView> invoices =
                jdbc.query(
                        """
                        SELECT
                            id,
                            tenant_id,
                            invoice_number,
                            student_financial_account_id,
                            student_id,
                            student_enrollment_id,
                            academic_year_id,
                            academic_term_id,
                            fee_structure_id,
                            invoice_date,
                            due_date,
                            currency_code,
                            subtotal_amount,
                            discount_amount,
                            tax_amount,
                            total_amount,
                            paid_amount,
                            outstanding_amount,
                            invoice_status,
                            issued_at,
                            issued_by,
                            status
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        (rs, rowNum) ->
                                new StudentInvoiceView(
                                        rs.getObject("id", UUID.class),
                                        rs.getObject("tenant_id", UUID.class),
                                        rs.getString("invoice_number"),
                                        rs.getObject(
                                                "student_financial_account_id",
                                                UUID.class
                                        ),
                                        rs.getObject("student_id", UUID.class),
                                        rs.getObject(
                                                "student_enrollment_id",
                                                UUID.class
                                        ),
                                        rs.getObject(
                                                "academic_year_id",
                                                UUID.class
                                        ),
                                        rs.getObject(
                                                "academic_term_id",
                                                UUID.class
                                        ),
                                        rs.getObject(
                                                "fee_structure_id",
                                                UUID.class
                                        ),
                                        rs.getObject(
                                                "invoice_date",
                                                LocalDate.class
                                        ),
                                        rs.getObject(
                                                "due_date",
                                                LocalDate.class
                                        ),
                                        rs.getString("currency_code"),
                                        rs.getBigDecimal("subtotal_amount"),
                                        rs.getBigDecimal("discount_amount"),
                                        rs.getBigDecimal("tax_amount"),
                                        rs.getBigDecimal("total_amount"),
                                        rs.getBigDecimal("paid_amount"),
                                        rs.getBigDecimal(
                                                "outstanding_amount"
                                        ),
                                        rs.getString("invoice_status"),
                                        rs.getTimestamp("issued_at") == null
                                                ? null
                                                : rs.getTimestamp("issued_at").toInstant(),
                                        rs.getObject("issued_by", UUID.class),
                                        rs.getString("status"),
                                        List.of()
                                ),
                        tenantId,
                        invoiceId
                );

        if (invoices.isEmpty()) {
            return Optional.empty();
        }

        StudentInvoiceView header =
                invoices.getFirst();

        return Optional.of(
                withLines(
                        header,
                        listInvoiceLines(
                                tenantId,
                                header.id()
                        )
                )
        );
    }

    @Transactional(readOnly = true)
    public List<StudentInvoiceView> listStudentInvoices(
            UUID tenantId,
            UUID studentId
    ) {

        List<UUID> invoiceIds =
                jdbc.query(
                        """
                        SELECT id
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND student_id = ?
                        ORDER BY invoice_date DESC,
                                 invoice_number DESC,
                                 id DESC
                        """,
                        (rs, rowNum) ->
                                rs.getObject(
                                        "id",
                                        UUID.class
                                ),
                        tenantId,
                        studentId
                );

        List<StudentInvoiceView> result =
                new ArrayList<>(
                        invoiceIds.size()
                );

        for (UUID invoiceId : invoiceIds) {
            findStudentInvoice(
                    tenantId,
                    invoiceId
            ).ifPresent(
                    result::add
            );
        }

        return List.copyOf(
                result
        );
    }

    private List<StudentInvoiceLineView> listInvoiceLines(
            UUID tenantId,
            UUID invoiceId
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    invoice_id,
                    fee_item_id,
                    fee_structure_item_id,
                    line_description,
                    quantity,
                    unit_amount,
                    gross_amount,
                    discount_amount,
                    tax_amount,
                    net_amount,
                    due_date,
                    line_status,
                    status
                FROM gts_student_invoice_line
                WHERE tenant_id = ?
                  AND invoice_id = ?
                ORDER BY created_at ASC,
                         id ASC
                """,
                (rs, rowNum) ->
                        new StudentInvoiceLineView(
                                rs.getObject("id", UUID.class),
                                rs.getObject("tenant_id", UUID.class),
                                rs.getObject("invoice_id", UUID.class),
                                rs.getObject("fee_item_id", UUID.class),
                                rs.getObject(
                                        "fee_structure_item_id",
                                        UUID.class
                                ),
                                rs.getString("line_description"),
                                rs.getBigDecimal("quantity"),
                                rs.getBigDecimal("unit_amount"),
                                rs.getBigDecimal("gross_amount"),
                                rs.getBigDecimal("discount_amount"),
                                rs.getBigDecimal("tax_amount"),
                                rs.getBigDecimal("net_amount"),
                                rs.getObject(
                                        "due_date",
                                        LocalDate.class
                                ),
                                rs.getString("line_status"),
                                rs.getString("status")
                        ),
                tenantId,
                invoiceId
        );
    }

    private static StudentInvoiceView withLines(
            StudentInvoiceView source,
            List<StudentInvoiceLineView> lines
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
                source.paidAmount(),
                source.outstandingAmount(),
                source.invoiceStatus(),
                source.issuedAt(),
                source.issuedBy(),
                source.status(),
                List.copyOf(lines)
        );
    }
}
