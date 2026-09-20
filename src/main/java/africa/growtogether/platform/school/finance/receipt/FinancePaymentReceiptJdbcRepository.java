package africa.growtogether.platform.school.finance.receipt;

import static africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.ReceiptResponse;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FinancePaymentReceiptJdbcRepository {

    private static final Set<String> RECEIPTABLE_PAYMENT_STATUSES =
            Set.of(
                    "RECEIVED",
                    "VERIFIED",
                    "ALLOCATED",
                    "COMPLETED"
            );

    private static final String RECEIPT_SELECT = """
            SELECT
                r.id AS receipt_id,
                r.student_payment_id,
                r.student_id,
                r.receipt_number,
                r.receipt_date,
                r.currency_code,
                r.receipt_amount,
                r.issued_at,
                r.issued_by,
                r.eds_receipt_document_id,
                r.delivery_channel,
                r.delivered_at,
                r.receipt_status,
                r.status,
                p.payment_reference,
                p.payment_method,
                p.provider_name,
                p.external_transaction_reference,
                p.eip_payment_transaction_id
            FROM gts_payment_receipt r
            JOIN gts_student_payment p
              ON p.tenant_id = r.tenant_id
             AND p.id = r.student_payment_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public FinancePaymentReceiptJdbcRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ReceiptResponse> findByPayment(
            UUID tenantId,
            UUID paymentId
    ) {

        List<ReceiptResponse> rows =
                jdbcTemplate.query(
                        RECEIPT_SELECT
                        + """
                          WHERE r.tenant_id = ?
                            AND r.student_payment_id = ?
                          """,
                        this::mapReceipt,
                        tenantId,
                        paymentId
                );

        return rows.stream().findFirst();
    }

    public Optional<ReceiptResponse> findById(
            UUID tenantId,
            UUID receiptId
    ) {

        List<ReceiptResponse> rows =
                jdbcTemplate.query(
                        RECEIPT_SELECT
                        + """
                          WHERE r.tenant_id = ?
                            AND r.id = ?
                          """,
                        this::mapReceipt,
                        tenantId,
                        receiptId
                );

        return rows.stream().findFirst();
    }

    public PaymentFacts lockReceiptablePayment(
            UUID tenantId,
            UUID paymentId
    ) {

        List<PaymentFacts> rows =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            student_id,
                            payment_reference,
                            payment_date,
                            currency_code,
                            payment_amount,
                            payment_method,
                            provider_name,
                            external_transaction_reference,
                            eip_payment_transaction_id,
                            payment_status,
                            status
                        FROM gts_student_payment
                        WHERE tenant_id = ?
                          AND id = ?
                        FOR UPDATE
                        """,
                        this::mapPayment,
                        tenantId,
                        paymentId
                );

        PaymentFacts payment =
                rows.stream()
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Student payment was not "
                                                + "found for tenant"
                                        )
                        );

        if (!"ACTIVE".equals(payment.status())) {
            throw new IllegalStateException(
                    "Only an active student payment "
                    + "may receive a receipt"
            );
        }

        if (!RECEIPTABLE_PAYMENT_STATUSES.contains(
                payment.paymentStatus()
        )) {
            throw new IllegalStateException(
                    "Student payment is not receiptable in status "
                    + payment.paymentStatus()
            );
        }

        return payment;
    }

    public ReceiptResponse insert(
            UUID tenantId,
            PaymentFacts payment,
            String receiptNumber,
            UUID actorId,
            Instant issuedAt
    ) {

        UUID receiptId =
                jdbcTemplate.queryForObject(
                        """
                        INSERT INTO gts_payment_receipt (
                            tenant_id,
                            receipt_number,
                            student_payment_id,
                            student_id,
                            receipt_date,
                            currency_code,
                            receipt_amount,
                            issued_at,
                            issued_by,
                            receipt_status,
                            status,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by
                        )
                        VALUES (
                            ?, ?, ?, ?, ?, ?, ?, ?, ?,
                            'ISSUED',
                            'ACTIVE',
                            ?, ?, ?, ?
                        )
                        RETURNING id
                        """,
                        UUID.class,
                        tenantId,
                        receiptNumber,
                        payment.paymentId(),
                        payment.studentId(),
                        Timestamp.from(payment.paymentDate()),
                        payment.currencyCode(),
                        payment.paymentAmount(),
                        Timestamp.from(issuedAt),
                        actorId,
                        Timestamp.from(issuedAt),
                        actorId.toString(),
                        Timestamp.from(issuedAt),
                        actorId.toString()
                );

        if (receiptId == null) {
            throw new IllegalStateException(
                    "Receipt insertion did not return an ID"
            );
        }

        return findById(
                tenantId,
                receiptId
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Issued receipt could not be reloaded"
                        )
        );
    }

    public void appendIssuedHistory(
            UUID tenantId,
            UUID receiptId,
            UUID actorId,
            Instant issuedAt,
            String receiptNumber
    ) {

        jdbcTemplate.update(
                """
                INSERT INTO gts_finance_history (
                    tenant_id,
                    entity_type,
                    entity_id,
                    event_type,
                    event_description,
                    effective_at,
                    event_by,
                    created_at,
                    created_by
                )
                VALUES (
                    ?,
                    'RECEIPT',
                    ?,
                    'RECEIPT_ISSUED',
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
                """,
                tenantId,
                receiptId,
                "Receipt issued: " + receiptNumber,
                Timestamp.from(issuedAt),
                actorId,
                Timestamp.from(issuedAt),
                actorId.toString()
        );
    }

    public ReceiptResponse lockReceiptForDocument(
            UUID tenantId,
            UUID receiptId
    ) {

        List<UUID> rows =
                jdbcTemplate.query(
                        """
                        SELECT id
                        FROM gts_payment_receipt
                        WHERE tenant_id = ?
                          AND id = ?
                          AND status = 'ACTIVE'
                        FOR UPDATE
                        """,
                        (
                                rs,
                                rowNum
                        ) ->
                                rs.getObject(
                                        "id",
                                        UUID.class
                                ),
                        tenantId,
                        receiptId
                );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Active receipt was not found"
            );
        }

        return findById(
                tenantId,
                receiptId
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Locked receipt could not be reloaded"
                        )
        );
    }

    public ReceiptResponse attachDocument(
            UUID tenantId,
            UUID receiptId,
            UUID documentId,
            UUID actorId,
            Instant updatedAt
    ) {

        if (documentId == null) {
            throw new IllegalArgumentException(
                    "documentId must not be null"
            );
        }

        int updated =
                jdbcTemplate.update(
                        """
                        UPDATE gts_payment_receipt
                        SET eds_receipt_document_id = ?,
                            updated_at = ?,
                            updated_by = ?
                        WHERE tenant_id = ?
                          AND id = ?
                          AND status = 'ACTIVE'
                          AND eds_receipt_document_id IS NULL
                        """,
                        documentId,
                        Timestamp.from(updatedAt),
                        actorId.toString(),
                        tenantId,
                        receiptId
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Receipt document link was not persisted"
            );
        }

        return findById(
                tenantId,
                receiptId
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Receipt could not be reloaded "
                                + "after document linkage"
                        )
        );
    }

    private PaymentFacts mapPayment(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        return new PaymentFacts(
                rs.getObject("id", UUID.class),
                rs.getObject("student_id", UUID.class),
                rs.getString("payment_reference"),
                requiredInstant(rs, "payment_date"),
                rs.getString("currency_code"),
                rs.getBigDecimal("payment_amount"),
                rs.getString("payment_method"),
                rs.getString("provider_name"),
                rs.getString("external_transaction_reference"),
                rs.getObject(
                        "eip_payment_transaction_id",
                        UUID.class
                ),
                rs.getString("payment_status"),
                rs.getString("status")
        );
    }

    private ReceiptResponse mapReceipt(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        return new ReceiptResponse(
                rs.getObject("receipt_id", UUID.class),
                rs.getObject(
                        "student_payment_id",
                        UUID.class
                ),
                rs.getObject("student_id", UUID.class),
                rs.getString("receipt_number"),
                requiredInstant(rs, "receipt_date"),
                rs.getString("currency_code"),
                rs.getBigDecimal("receipt_amount"),
                requiredInstant(rs, "issued_at"),
                rs.getObject("issued_by", UUID.class),
                rs.getObject(
                        "eds_receipt_document_id",
                        UUID.class
                ),
                rs.getString("delivery_channel"),
                nullableInstant(rs, "delivered_at"),
                rs.getString("receipt_status"),
                rs.getString("status"),
                rs.getString("payment_reference"),
                rs.getString("payment_method"),
                rs.getString("provider_name"),
                rs.getString(
                        "external_transaction_reference"
                ),
                rs.getObject(
                        "eip_payment_transaction_id",
                        UUID.class
                )
        );
    }

    private static Instant requiredInstant(
            ResultSet rs,
            String column
    ) throws SQLException {

        Timestamp value =
                rs.getTimestamp(column);

        if (value == null) {
            throw new IllegalStateException(
                    column + " must not be null"
            );
        }

        return value.toInstant();
    }

    private static Instant nullableInstant(
            ResultSet rs,
            String column
    ) throws SQLException {

        Timestamp value =
                rs.getTimestamp(column);

        return value == null
                ? null
                : value.toInstant();
    }

    public record PaymentFacts(
            UUID paymentId,
            UUID studentId,
            String paymentReference,
            Instant paymentDate,
            String currencyCode,
            BigDecimal paymentAmount,
            String paymentMethod,
            String providerName,
            String externalTransactionReference,
            UUID eipPaymentTransactionId,
            String paymentStatus,
            String status
    ) {
    }
}
