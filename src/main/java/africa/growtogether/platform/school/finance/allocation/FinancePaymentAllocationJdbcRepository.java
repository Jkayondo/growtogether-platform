
package africa.growtogether.platform.school.finance.allocation;

import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.AllocationResponse;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.CreateRequest;

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
