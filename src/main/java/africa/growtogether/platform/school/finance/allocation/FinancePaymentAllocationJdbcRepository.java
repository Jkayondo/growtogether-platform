
package africa.growtogether.platform.school.finance.allocation;

import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.AllocationResponse;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.CreateRequest;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.CorrectionResponse;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.ReallocateRequest;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FinancePaymentAllocationJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public FinancePaymentAllocationJdbcRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbc =
                new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public AllocationResponse create(
            UUID tenantId,
            UUID paymentId,
            CreateRequest request,
            UUID actorId,
            String actor
    ) {
        PaymentLock payment =
                requirePaymentForUpdate(
                        tenantId,
                        paymentId
                );

        InvoiceLock invoice =
                requireInvoiceForUpdate(
                        tenantId,
                        request.invoiceId()
                );

        if (!Objects.equals(
                payment.studentId(),
                invoice.studentId()
        )) {
            throw new IllegalArgumentException(
                    "Payment and invoice must belong to the same student"
            );
        }

        if (!Objects.equals(
                normalizeCurrency(payment.currencyCode()),
                normalizeCurrency(invoice.currencyCode())
        )) {
            throw new IllegalArgumentException(
                    "Payment and invoice currency must agree"
            );
        }

        validateInvoiceLine(
                tenantId,
                request.invoiceId(),
                request.invoiceLineId()
        );

        validateInstallmentStudentContext(
                tenantId,
                payment.studentId(),
                request.paymentInstallmentId()
        );

        BigDecimal paymentRemaining =
                payment.amount().subtract(
                        activeAllocatedForPayment(
                                tenantId,
                                paymentId
                        )
                );

        BigDecimal invoiceRemaining =
                invoice.outstandingAmount().subtract(
                        activeAllocatedForInvoice(
                                tenantId,
                                request.invoiceId()
                        )
                );

        BigDecimal requested =
                request.allocatedAmount();

        if (requested.compareTo(paymentRemaining) > 0) {
            throw new IllegalArgumentException(
                    "Allocation exceeds remaining payment amount"
            );
        }

        if (requested.compareTo(invoiceRemaining) > 0) {
            throw new IllegalArgumentException(
                    "Allocation exceeds invoice outstanding amount"
            );
        }

        String sql = """
                INSERT INTO gts_payment_allocation (
                    tenant_id,
                    student_payment_id,
                    invoice_id,
                    invoice_line_id,
                    payment_installment_id,
                    allocated_amount,
                    allocated_at,
                    allocated_by,
                    allocation_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by
                ) VALUES (
                    :tenantId,
                    :paymentId,
                    :invoiceId,
                    :invoiceLineId,
                    :paymentInstallmentId,
                    :allocatedAmount,
                    CURRENT_TIMESTAMP,
                    :allocatedBy,
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    :actor,
                    CURRENT_TIMESTAMP,
                    :actor
                )
                RETURNING
                    id,
                    student_payment_id,
                    invoice_id,
                    invoice_line_id,
                    payment_installment_id,
                    allocated_amount,
                    allocated_at,
                    allocated_by,
                    allocation_status,
                    status
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("tenantId", tenantId)
                        .addValue("paymentId", paymentId)
                        .addValue(
                                "invoiceId",
                                request.invoiceId()
                        )
                        .addValue(
                                "invoiceLineId",
                                request.invoiceLineId()
                        )
                        .addValue(
                                "paymentInstallmentId",
                                request.paymentInstallmentId()
                        )
                        .addValue(
                                "allocatedAmount",
                                requested
                        )
                        .addValue(
                                "allocatedBy",
                                actorId
                        )
                        .addValue("actor", actor);

        return requireOne(
                jdbc.query(
                        sql,
                        params,
                        this::mapAllocation
                ),
                "Payment allocation was not persisted"
        );
    }

    public CorrectionResponse reverse(
            UUID tenantId,
            UUID paymentId,
            UUID allocationId,
            String reason,
            UUID actorId,
            String actor
    ) {
        PaymentLock payment =
                requirePaymentForUpdate(
                        tenantId,
                        paymentId
                );

        AllocationLock allocation =
                requireAllocationForUpdate(
                        tenantId,
                        paymentId,
                        allocationId
                );

        requireActiveAllocation(
                allocation
        );

        InvoiceLock invoice =
                requireInvoiceForUpdate(
                        tenantId,
                        allocation.invoiceId()
                );

        validatePaymentInvoiceContext(
                payment,
                invoice
        );

        updateAllocationStatus(
                tenantId,
                paymentId,
                allocationId,
                "REVERSED",
                actor
        );

        return insertCorrection(
                tenantId,
                allocationId,
                "REVERSAL",
                reason,
                null,
                actor
        );
    }

    public CorrectionResponse reallocate(
            UUID tenantId,
            UUID paymentId,
            UUID allocationId,
            ReallocateRequest request,
            String reason,
            UUID actorId,
            String actor
    ) {
        PaymentLock payment =
                requirePaymentForUpdate(
                        tenantId,
                        paymentId
                );

        AllocationLock allocation =
                requireAllocationForUpdate(
                        tenantId,
                        paymentId,
                        allocationId
                );

        requireActiveAllocation(
                allocation
        );

        InvoiceLock originalInvoice;
        InvoiceLock destinationInvoice;

        if (
                allocation.invoiceId().equals(
                        request.invoiceId()
                )
        ) {
            originalInvoice =
                    requireInvoiceForUpdate(
                            tenantId,
                            allocation.invoiceId()
                    );

            destinationInvoice =
                    originalInvoice;
        } else if (
                allocation.invoiceId().compareTo(
                        request.invoiceId()
                ) < 0
        ) {
            originalInvoice =
                    requireInvoiceForUpdate(
                            tenantId,
                            allocation.invoiceId()
                    );

            destinationInvoice =
                    requireInvoiceForUpdate(
                            tenantId,
                            request.invoiceId()
                    );
        } else {
            destinationInvoice =
                    requireInvoiceForUpdate(
                            tenantId,
                            request.invoiceId()
                    );

            originalInvoice =
                    requireInvoiceForUpdate(
                            tenantId,
                            allocation.invoiceId()
                    );
        }

        validatePaymentInvoiceContext(
                payment,
                originalInvoice
        );

        validatePaymentInvoiceContext(
                payment,
                destinationInvoice
        );

        validateInvoiceLine(
                tenantId,
                request.invoiceId(),
                request.invoiceLineId()
        );

        validateInstallmentStudentContext(
                tenantId,
                payment.studentId(),
                request.paymentInstallmentId()
        );

        updateAllocationStatus(
                tenantId,
                paymentId,
                allocationId,
                "REALLOCATED",
                actor
        );

        AllocationResponse replacement =
                create(
                        tenantId,
                        paymentId,
                        new CreateRequest(
                                request.invoiceId(),
                                request.invoiceLineId(),
                                request.paymentInstallmentId(),
                                allocation.allocatedAmount()
                        ),
                        actorId,
                        actor
                );

        return insertCorrection(
                tenantId,
                allocationId,
                "REALLOCATION",
                reason,
                replacement.allocationId(),
                actor
        );
    }

    public CorrectionResponse getCorrection(
            UUID tenantId,
            UUID paymentId,
            UUID allocationId
    ) {
        String sql = """
                SELECT
                    c.id,
                    c.allocation_id,
                    c.correction_type,
                    c.reason,
                    c.replacement_allocation_id,
                    c.created_at,
                    c.created_by
                FROM gts_payment_allocation_correction c
                JOIN gts_payment_allocation a
                  ON a.id = c.allocation_id
                 AND a.tenant_id = c.tenant_id
                WHERE c.tenant_id = :tenantId
                  AND a.student_payment_id = :paymentId
                  AND c.allocation_id = :allocationId
                """;

        return requireOne(
                jdbc.query(
                        sql,
                        Map.of(
                                "tenantId",
                                tenantId,
                                "paymentId",
                                paymentId,
                                "allocationId",
                                allocationId
                        ),
                        this::mapCorrection
                ),
                "Payment allocation correction not found"
        );
    }

    public AllocationResponse get(
            UUID tenantId,
            UUID paymentId,
            UUID allocationId
    ) {
        String sql = """
                SELECT
                    id,
                    student_payment_id,
                    invoice_id,
                    invoice_line_id,
                    payment_installment_id,
                    allocated_amount,
                    allocated_at,
                    allocated_by,
                    allocation_status,
                    status
                FROM gts_payment_allocation
                WHERE tenant_id = :tenantId
                  AND student_payment_id = :paymentId
                  AND id = :allocationId
                """;

        return requireOne(
                jdbc.query(
                        sql,
                        Map.of(
                                "tenantId",
                                tenantId,
                                "paymentId",
                                paymentId,
                                "allocationId",
                                allocationId
                        ),
                        this::mapAllocation
                ),
                "Payment allocation not found"
        );
    }

    public List<AllocationResponse> list(
            UUID tenantId,
            UUID paymentId
    ) {
        String sql = """
                SELECT
                    id,
                    student_payment_id,
                    invoice_id,
                    invoice_line_id,
                    payment_installment_id,
                    allocated_amount,
                    allocated_at,
                    allocated_by,
                    allocation_status,
                    status
                FROM gts_payment_allocation
                WHERE tenant_id = :tenantId
                  AND student_payment_id = :paymentId
                ORDER BY allocated_at, id
                """;

        return jdbc.query(
                sql,
                Map.of(
                        "tenantId",
                        tenantId,
                        "paymentId",
                        paymentId
                ),
                this::mapAllocation
        );
    }

    private AllocationLock requireAllocationForUpdate(
            UUID tenantId,
            UUID paymentId,
            UUID allocationId
    ) {
        String sql = """
                SELECT
                    id,
                    student_payment_id,
                    invoice_id,
                    invoice_line_id,
                    payment_installment_id,
                    allocated_amount,
                    allocation_status,
                    status
                FROM gts_payment_allocation
                WHERE tenant_id = :tenantId
                  AND student_payment_id = :paymentId
                  AND id = :allocationId
                FOR UPDATE
                """;

        return requireOne(
                jdbc.query(
                        sql,
                        Map.of(
                                "tenantId",
                                tenantId,
                                "paymentId",
                                paymentId,
                                "allocationId",
                                allocationId
                        ),
                        (rs, rowNum) ->
                                new AllocationLock(
                                        rs.getObject(
                                                "id",
                                                UUID.class
                                        ),
                                        rs.getObject(
                                                "student_payment_id",
                                                UUID.class
                                        ),
                                        rs.getObject(
                                                "invoice_id",
                                                UUID.class
                                        ),
                                        rs.getObject(
                                                "invoice_line_id",
                                                UUID.class
                                        ),
                                        rs.getObject(
                                                "payment_installment_id",
                                                UUID.class
                                        ),
                                        rs.getBigDecimal(
                                                "allocated_amount"
                                        ),
                                        rs.getString(
                                                "allocation_status"
                                        ),
                                        rs.getString(
                                                "status"
                                        )
                                )
                ),
                "Payment allocation not found"
        );
    }

    private void requireActiveAllocation(
            AllocationLock allocation
    ) {
        if (
                !"ACTIVE".equals(
                        allocation.allocationStatus()
                )
                || !"ACTIVE".equals(
                        allocation.status()
                )
        ) {
            throw new IllegalStateException(
                    "Only an ACTIVE payment allocation may be corrected"
            );
        }
    }

    private void validatePaymentInvoiceContext(
            PaymentLock payment,
            InvoiceLock invoice
    ) {
        if (!Objects.equals(
                payment.studentId(),
                invoice.studentId()
        )) {
            throw new IllegalArgumentException(
                    "Payment and invoice must belong to the same student"
            );
        }

        if (!Objects.equals(
                normalizeCurrency(
                        payment.currencyCode()
                ),
                normalizeCurrency(
                        invoice.currencyCode()
                )
        )) {
            throw new IllegalArgumentException(
                    "Payment and invoice currency must agree"
            );
        }
    }

    private void updateAllocationStatus(
            UUID tenantId,
            UUID paymentId,
            UUID allocationId,
            String allocationStatus,
            String actor
    ) {
        int updated = jdbc.update(
                """
                UPDATE gts_payment_allocation
                SET allocation_status = :allocationStatus,
                    updated_at = CURRENT_TIMESTAMP,
                    updated_by = :actor,
                    version = version + 1
                WHERE tenant_id = :tenantId
                  AND student_payment_id = :paymentId
                  AND id = :allocationId
                  AND allocation_status = 'ACTIVE'
                  AND status = 'ACTIVE'
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "tenantId",
                                tenantId
                        )
                        .addValue(
                                "paymentId",
                                paymentId
                        )
                        .addValue(
                                "allocationId",
                                allocationId
                        )
                        .addValue(
                                "allocationStatus",
                                allocationStatus
                        )
                        .addValue(
                                "actor",
                                actor
                        )
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Payment allocation is no longer ACTIVE"
            );
        }
    }

    private CorrectionResponse insertCorrection(
            UUID tenantId,
            UUID allocationId,
            String correctionType,
            String reason,
            UUID replacementAllocationId,
            String actor
    ) {
        String sql = """
                INSERT INTO gts_payment_allocation_correction (
                    tenant_id,
                    allocation_id,
                    correction_type,
                    reason,
                    replacement_allocation_id,
                    created_at,
                    created_by
                ) VALUES (
                    :tenantId,
                    :allocationId,
                    :correctionType,
                    :reason,
                    :replacementAllocationId,
                    CURRENT_TIMESTAMP,
                    :actor
                )
                RETURNING
                    id,
                    allocation_id,
                    correction_type,
                    reason,
                    replacement_allocation_id,
                    created_at,
                    created_by
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "tenantId",
                                tenantId
                        )
                        .addValue(
                                "allocationId",
                                allocationId
                        )
                        .addValue(
                                "correctionType",
                                correctionType
                        )
                        .addValue(
                                "reason",
                                reason
                        )
                        .addValue(
                                "replacementAllocationId",
                                replacementAllocationId
                        )
                        .addValue(
                                "actor",
                                actor
                        );

        return requireOne(
                jdbc.query(
                        sql,
                        params,
                        this::mapCorrection
                ),
                "Payment allocation correction was not persisted"
        );
    }

    private PaymentLock requirePaymentForUpdate(
            UUID tenantId,
            UUID paymentId
    ) {
        String sql = """
                SELECT
                    student_id,
                    payment_amount,
                    currency_code
                FROM gts_student_payment
                WHERE tenant_id = :tenantId
                  AND id = :paymentId
                FOR UPDATE
                """;

        return requireOne(
                jdbc.query(
                        sql,
                        Map.of(
                                "tenantId",
                                tenantId,
                                "paymentId",
                                paymentId
                        ),
                        (rs, rowNum) ->
                                new PaymentLock(
                                        rs.getObject(
                                                "student_id",
                                                UUID.class
                                        ),
                                        rs.getBigDecimal(
                                                "payment_amount"
                                        ),
                                        rs.getString(
                                                "currency_code"
                                        )
                                )
                ),
                "Student payment not found"
        );
    }

    private InvoiceLock requireInvoiceForUpdate(
            UUID tenantId,
            UUID invoiceId
    ) {
        String sql = """
                SELECT
                    student_id,
                    outstanding_amount,
                    currency_code
                FROM gts_student_invoice
                WHERE tenant_id = :tenantId
                  AND id = :invoiceId
                FOR UPDATE
                """;

        return requireOne(
                jdbc.query(
                        sql,
                        Map.of(
                                "tenantId",
                                tenantId,
                                "invoiceId",
                                invoiceId
                        ),
                        (rs, rowNum) ->
                                new InvoiceLock(
                                        rs.getObject(
                                                "student_id",
                                                UUID.class
                                        ),
                                        rs.getBigDecimal(
                                                "outstanding_amount"
                                        ),
                                        rs.getString(
                                                "currency_code"
                                        )
                                )
                ),
                "Student invoice not found"
        );
    }

    private void validateInvoiceLine(
            UUID tenantId,
            UUID invoiceId,
            UUID invoiceLineId
    ) {
        if (invoiceLineId == null) {
            return;
        }

        String sql = """
                SELECT l.id
                FROM gts_student_invoice_line l
                JOIN gts_student_invoice i
                  ON i.id = l.invoice_id
                WHERE l.id = :invoiceLineId
                  AND i.id = :invoiceId
                  AND i.tenant_id = :tenantId
                AND l.tenant_id = :tenantId
                """;

        List<UUID> matches = jdbc.query(
                sql,
                Map.of(
                        "invoiceLineId",
                        invoiceLineId,
                        "invoiceId",
                        invoiceId,
                        "tenantId",
                        tenantId
                ),
                (rs, rowNum) ->
                        rs.getObject("id", UUID.class)
        );

        if (matches.size() != 1) {
            throw new IllegalArgumentException(
                    "invoiceLineId must belong to the selected invoice"
            );
        }
    }

    private void validateInstallmentStudentContext(
            UUID tenantId,
            UUID studentId,
            UUID installmentId
    ) {
        if (installmentId == null) {
            return;
        }

        String sql = """
                SELECT pa.student_id
                FROM gts_payment_installment pi
                JOIN gts_payment_arrangement pa
                  ON pa.id = pi.payment_arrangement_id
                 AND pa.tenant_id = pi.tenant_id
                WHERE pi.id = :installmentId
                  AND pi.tenant_id = :tenantId
                  AND pa.tenant_id = :tenantId
                """;

        UUID arrangementStudent = requireOne(
                jdbc.query(
                        sql,
                        Map.of(
                                "installmentId",
                                installmentId,
                                "tenantId",
                                tenantId
                        ),
                        (rs, rowNum) ->
                                rs.getObject(
                                        "student_id",
                                        UUID.class
                                )
                ),
                "paymentInstallmentId was not found for the tenant"
        );

        if (!Objects.equals(
                studentId,
                arrangementStudent
        )) {
            throw new IllegalArgumentException(
                    "paymentInstallmentId must belong to the payment/invoice student"
            );
        }
    }

    private BigDecimal activeAllocatedForPayment(
            UUID tenantId,
            UUID paymentId
    ) {
        BigDecimal value = jdbc.queryForObject(
                """
                SELECT COALESCE(
                    SUM(allocated_amount),
                    0
                )
                FROM gts_payment_allocation
                WHERE tenant_id = :tenantId
                  AND student_payment_id = :paymentId
                  AND allocation_status = 'ACTIVE'
                  AND status = 'ACTIVE'
                """,
                Map.of(
                        "tenantId",
                        tenantId,
                        "paymentId",
                        paymentId
                ),
                BigDecimal.class
        );

        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private BigDecimal activeAllocatedForInvoice(
            UUID tenantId,
            UUID invoiceId
    ) {
        BigDecimal value = jdbc.queryForObject(
                """
                SELECT COALESCE(
                    SUM(allocated_amount),
                    0
                )
                FROM gts_payment_allocation
                WHERE tenant_id = :tenantId
                  AND invoice_id = :invoiceId
                  AND allocation_status = 'ACTIVE'
                  AND status = 'ACTIVE'
                """,
                Map.of(
                        "tenantId",
                        tenantId,
                        "invoiceId",
                        invoiceId
                ),
                BigDecimal.class
        );

        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private CorrectionResponse mapCorrection(
            ResultSet rs,
            int rowNum
    ) throws SQLException {
        OffsetDateTime createdAt =
                rs.getObject(
                        "created_at",
                        OffsetDateTime.class
                );

        return new CorrectionResponse(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "allocation_id",
                        UUID.class
                ),
                rs.getString(
                        "correction_type"
                ),
                rs.getString(
                        "reason"
                ),
                rs.getObject(
                        "replacement_allocation_id",
                        UUID.class
                ),
                createdAt == null
                        ? null
                        : createdAt.toInstant(),
                rs.getString(
                        "created_by"
                )
        );
    }

    private AllocationResponse mapAllocation(
            ResultSet rs,
            int rowNum
    ) throws SQLException {
        OffsetDateTime allocatedAt =
                rs.getObject(
                        "allocated_at",
                        OffsetDateTime.class
                );

        return new AllocationResponse(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "student_payment_id",
                        UUID.class
                ),
                rs.getObject(
                        "invoice_id",
                        UUID.class
                ),
                rs.getObject(
                        "invoice_line_id",
                        UUID.class
                ),
                rs.getObject(
                        "payment_installment_id",
                        UUID.class
                ),
                rs.getBigDecimal(
                        "allocated_amount"
                ),
                allocatedAt == null
                        ? null
                        : allocatedAt.toInstant(),
                rs.getObject(
                        "allocated_by",
                        UUID.class
                ),
                rs.getString(
                        "allocation_status"
                ),
                rs.getString(
                        "status"
                )
        );
    }

    private static String normalizeCurrency(
            String value
    ) {
        return value == null
                ? null
                : value.trim()
                        .toUpperCase(Locale.ROOT);
    }

    private static <T> T requireOne(
            List<T> values,
            String message
    ) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return values.get(0);
    }

    private record AllocationLock(
            UUID allocationId,
            UUID paymentId,
            UUID invoiceId,
            UUID invoiceLineId,
            UUID paymentInstallmentId,
            BigDecimal allocatedAmount,
            String allocationStatus,
            String status
    ) {
    }

    private record PaymentLock(
            UUID studentId,
            BigDecimal amount,
            String currencyCode
    ) {
    }

    private record InvoiceLock(
            UUID studentId,
            BigDecimal outstandingAmount,
            String currencyCode
    ) {
    }
}
