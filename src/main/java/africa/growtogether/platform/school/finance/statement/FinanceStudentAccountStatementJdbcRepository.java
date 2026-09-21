package africa.growtogether.platform.school.finance.statement;

import static africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementDtos.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class FinanceStudentAccountStatementJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public FinanceStudentAccountStatementJdbcRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public record AccountScope(
            UUID accountId,
            UUID tenantId,
            UUID studentId,
            String currencyCode,
            String accountNumber,
            String billingStatus,
            String status
    ) {
    }

    public record SummaryNumbers(
            BigDecimal totalBilled,
            BigDecimal totalPaid,
            BigDecimal totalOutstanding,
            BigDecimal arrearsOutstanding,
            int invoiceCount,
            int overdueInvoiceCount
    ) {
    }

    @Transactional(readOnly = true)
    public Optional<AccountScope> findAccountScope(
            UUID tenantId,
            UUID studentId,
            String currencyCode
    ) {

        List<AccountScope> rows = jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    student_id,
                    currency_code,
                    account_number,
                    billing_status,
                    status
                FROM gts_student_financial_account
                WHERE tenant_id = :tenantId
                  AND student_id = :studentId
                  AND currency_code = :currencyCode
                """,
                base(tenantId, studentId, currencyCode),
                (rs, rowNum) ->
                        new AccountScope(
                                rs.getObject("id", UUID.class),
                                rs.getObject("tenant_id", UUID.class),
                                rs.getObject("student_id", UUID.class),
                                rs.getString("currency_code"),
                                rs.getString("account_number"),
                                rs.getString("billing_status"),
                                rs.getString("status")
                        )
        );

        return rows.stream().findFirst();
    }

    @Transactional(readOnly = true)
    public SummaryNumbers loadSummaryNumbers(
            UUID tenantId,
            UUID studentId,
            String currencyCode,
            LocalDate asOfDate
    ) {

        MapSqlParameterSource parameters =
                base(tenantId, studentId, currencyCode)
                        .addValue("asOfDate", asOfDate);

        SummaryNumbers result = jdbc.queryForObject(
                """
                SELECT
                    COALESCE(SUM(total_amount), 0) AS total_billed,
                    COALESCE(SUM(paid_amount), 0) AS total_paid,
                    COALESCE(SUM(outstanding_amount), 0) AS total_outstanding,
                    COALESCE(
                        SUM(
                            CASE
                                WHEN due_date IS NOT NULL
                                 AND due_date < CAST(:asOfDate AS DATE)
                                 AND outstanding_amount > 0
                                THEN outstanding_amount
                                ELSE 0
                            END
                        ),
                        0
                    ) AS arrears_outstanding,
                    COUNT(*) AS invoice_count,
                    COUNT(*) FILTER (
                        WHERE due_date IS NOT NULL
                          AND due_date < CAST(:asOfDate AS DATE)
                          AND outstanding_amount > 0
                    ) AS overdue_invoice_count
                FROM gts_student_invoice
                WHERE tenant_id = :tenantId
                  AND student_id = :studentId
                  AND currency_code = :currencyCode
                  AND status = 'ACTIVE'
                  AND invoice_status NOT IN ('DRAFT', 'CANCELLED')
                """,
                parameters,
                (rs, rowNum) ->
                        new SummaryNumbers(
                                rs.getBigDecimal("total_billed"),
                                rs.getBigDecimal("total_paid"),
                                rs.getBigDecimal("total_outstanding"),
                                rs.getBigDecimal("arrears_outstanding"),
                                rs.getInt("invoice_count"),
                                rs.getInt("overdue_invoice_count")
                        )
        );

        if (result == null) {
            return new SummaryNumbers(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    0
            );
        }

        return result;
    }

    @Transactional(readOnly = true)
    public BigDecimal loadUnallocatedPaymentAmount(
            UUID tenantId,
            UUID studentId,
            String currencyCode
    ) {

        BigDecimal result = jdbc.queryForObject(
                """
                WITH active_allocations AS (
                    SELECT
                        tenant_id,
                        student_payment_id,
                        SUM(allocated_amount) AS allocated_amount
                    FROM gts_payment_allocation
                    WHERE tenant_id = :tenantId
                      AND allocation_status = 'ACTIVE'
                      AND status = 'ACTIVE'
                    GROUP BY tenant_id, student_payment_id
                )
                SELECT COALESCE(
                    SUM(
                        GREATEST(
                            p.payment_amount
                            - COALESCE(a.allocated_amount, 0),
                            0
                        )
                    ),
                    0
                )
                FROM gts_student_payment p
                LEFT JOIN active_allocations a
                  ON a.tenant_id = p.tenant_id
                 AND a.student_payment_id = p.id
                WHERE p.tenant_id = :tenantId
                  AND p.student_id = :studentId
                  AND p.currency_code = :currencyCode
                  AND p.status = 'ACTIVE'
                  AND p.payment_status IN (
                      'RECEIVED',
                      'VERIFIED',
                      'ALLOCATED',
                      'COMPLETED'
                  )
                """,
                base(tenantId, studentId, currencyCode),
                BigDecimal.class
        );

        return result == null
                ? BigDecimal.ZERO
                : result;
    }

    @Transactional(readOnly = true)
    public List<ArrearsItem> listArrears(
            UUID tenantId,
            UUID studentId,
            String currencyCode,
            LocalDate asOfDate
    ) {

        MapSqlParameterSource parameters =
                base(tenantId, studentId, currencyCode)
                        .addValue("asOfDate", asOfDate);

        return jdbc.query(
                """
                SELECT
                    id,
                    invoice_number,
                    invoice_date,
                    due_date,
                    outstanding_amount,
                    invoice_status
                FROM gts_student_invoice
                WHERE tenant_id = :tenantId
                  AND student_id = :studentId
                  AND currency_code = :currencyCode
                  AND status = 'ACTIVE'
                  AND invoice_status NOT IN ('DRAFT', 'CANCELLED')
                  AND due_date IS NOT NULL
                  AND due_date < CAST(:asOfDate AS DATE)
                  AND outstanding_amount > 0
                ORDER BY due_date ASC, invoice_number ASC, id ASC
                """,
                parameters,
                (rs, rowNum) -> {
                    LocalDate dueDate =
                            rs.getObject(
                                    "due_date",
                                    LocalDate.class
                            );

                    return new ArrearsItem(
                            rs.getObject("id", UUID.class),
                            rs.getString("invoice_number"),
                            rs.getObject(
                                    "invoice_date",
                                    LocalDate.class
                            ),
                            dueDate,
                            rs.getBigDecimal(
                                    "outstanding_amount"
                            ),
                            ChronoUnit.DAYS.between(
                                    dueDate,
                                    asOfDate
                            ),
                            rs.getString("invoice_status")
                    );
                }
        );
    }

    @Transactional(readOnly = true)
    public List<StatementEntry> listStatementEntries(
            UUID tenantId,
            UUID studentId,
            String currencyCode,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        MapSqlParameterSource parameters =
                base(tenantId, studentId, currencyCode)
                        .addValue("fromDate", fromDate)
                        .addValue("toDate", toDate);

        return jdbc.query(
                """
                SELECT
                    effective_date,
                    occurred_at,
                    entry_type,
                    source_id,
                    reference,
                    amount,
                    balance_effect,
                    related_invoice_id,
                    related_payment_id,
                    lifecycle_status
                FROM (
                    SELECT
                        i.invoice_date AS effective_date,
                        i.issued_at AS occurred_at,
                        'INVOICE' AS entry_type,
                        i.id AS source_id,
                        i.invoice_number AS reference,
                        i.total_amount AS amount,
                        'DEBIT' AS balance_effect,
                        i.id AS related_invoice_id,
                        CAST(NULL AS UUID) AS related_payment_id,
                        i.invoice_status AS lifecycle_status
                    FROM gts_student_invoice i
                    WHERE i.tenant_id = :tenantId
                      AND i.student_id = :studentId
                      AND i.currency_code = :currencyCode
                      AND i.status = 'ACTIVE'
                      AND i.invoice_status <> 'DRAFT'

                    UNION ALL

                    SELECT
                        CAST(p.payment_date AS DATE),
                        p.payment_date,
                        'PAYMENT',
                        p.id,
                        p.payment_reference,
                        p.payment_amount,
                        CASE
                            WHEN p.payment_status IN (
                                'RECEIVED',
                                'VERIFIED',
                                'ALLOCATED',
                                'COMPLETED'
                            )
                            THEN 'CREDIT'
                            ELSE 'INFORMATIONAL'
                        END,
                        CAST(NULL AS UUID),
                        p.id,
                        p.payment_status
                    FROM gts_student_payment p
                    WHERE p.tenant_id = :tenantId
                      AND p.student_id = :studentId
                      AND p.currency_code = :currencyCode
                      AND p.status = 'ACTIVE'

                    UNION ALL

                    SELECT
                        CAST(a.allocated_at AS DATE),
                        a.allocated_at,
                        'ALLOCATION',
                        a.id,
                        CAST(a.id AS VARCHAR),
                        a.allocated_amount,
                        'INFORMATIONAL',
                        a.invoice_id,
                        a.student_payment_id,
                        a.allocation_status
                    FROM gts_payment_allocation a
                    JOIN gts_student_payment p
                      ON p.tenant_id = a.tenant_id
                     AND p.id = a.student_payment_id
                    WHERE a.tenant_id = :tenantId
                      AND p.student_id = :studentId
                      AND p.currency_code = :currencyCode
                      AND a.status = 'ACTIVE'

                    UNION ALL

                    SELECT
                        CAST(c.created_at AS DATE),
                        c.created_at,
                        CASE
                            WHEN c.correction_type = 'REVERSAL'
                            THEN 'ALLOCATION_REVERSAL'
                            ELSE 'ALLOCATION_REALLOCATION'
                        END,
                        c.id,
                        CAST(c.id AS VARCHAR),
                        a.allocated_amount,
                        'INFORMATIONAL',
                        a.invoice_id,
                        a.student_payment_id,
                        c.correction_type
                    FROM gts_payment_allocation_correction c
                    JOIN gts_payment_allocation a
                      ON a.tenant_id = c.tenant_id
                     AND a.id = c.allocation_id
                    JOIN gts_student_payment p
                      ON p.tenant_id = a.tenant_id
                     AND p.id = a.student_payment_id
                    WHERE c.tenant_id = :tenantId
                      AND p.student_id = :studentId
                      AND p.currency_code = :currencyCode

                    UNION ALL

                    SELECT
                        CAST(r.receipt_date AS DATE),
                        r.receipt_date,
                        'RECEIPT',
                        r.id,
                        r.receipt_number,
                        r.receipt_amount,
                        'INFORMATIONAL',
                        CAST(NULL AS UUID),
                        r.student_payment_id,
                        r.receipt_status
                    FROM gts_payment_receipt r
                    WHERE r.tenant_id = :tenantId
                      AND r.student_id = :studentId
                      AND r.currency_code = :currencyCode
                      AND r.status = 'ACTIVE'
                ) statement_entry
                WHERE (
                    CAST(:fromDate AS DATE) IS NULL
                    OR effective_date >= CAST(:fromDate AS DATE)
                )
                  AND (
                    CAST(:toDate AS DATE) IS NULL
                    OR effective_date <= CAST(:toDate AS DATE)
                )
                ORDER BY
                    effective_date ASC,
                    entry_type ASC,
                    source_id ASC
                """,
                parameters,
                (rs, rowNum) ->
                        new StatementEntry(
                                rs.getObject(
                                        "effective_date",
                                        LocalDate.class
                                ),
                                rs.getTimestamp("occurred_at") == null
                                        ? null
                                        : rs.getTimestamp(
                                                "occurred_at"
                                        ).toInstant(),
                                rs.getString("entry_type"),
                                rs.getObject(
                                        "source_id",
                                        UUID.class
                                ),
                                rs.getString("reference"),
                                rs.getBigDecimal("amount"),
                                rs.getString("balance_effect"),
                                rs.getObject(
                                        "related_invoice_id",
                                        UUID.class
                                ),
                                rs.getObject(
                                        "related_payment_id",
                                        UUID.class
                                ),
                                rs.getString(
                                        "lifecycle_status"
                                )
                        )
        );
    }

    private MapSqlParameterSource base(
            UUID tenantId,
            UUID studentId,
            String currencyCode
    ) {
        return new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("studentId", studentId)
                .addValue("currencyCode", currencyCode);
    }
}
