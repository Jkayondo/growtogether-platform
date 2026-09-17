package africa.growtogether.platform.school.finance.payment;

import static africa.growtogether.platform.school.finance.payment.FinanceStudentPaymentDtos.CreateRequest;
import static africa.growtogether.platform.school.finance.payment.FinanceStudentPaymentDtos.PaymentResponse;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FinanceStudentPaymentJdbcRepository {

    public record FinancialAccountContext(
            UUID accountId,
            UUID studentId,
            String currencyCode
    ) {
    }

    private static final String BASE_SELECT =
            "SELECT p.id AS id, p.tenant_id AS tenant_id, p.student_financial_account_id AS student_financial_account_id, p.student_id AS student_id, p.payment_amount AS amount, p.currency_code AS currency_code, p.payment_method AS payment_method, p.payment_reference AS payment_reference, p.status AS status, p.payment_date AS paid_at, p.eip_payment_transaction_id AS eip_payment_transaction_id, p.created_at AS created_at, p.updated_at AS updated_at FROM gts_student_payment p JOIN gts_student_financial_account a ON a.id = p.student_financial_account_id AND a.tenant_id = p.tenant_id";

    private static final RowMapper<PaymentResponse> PAYMENT_MAPPER =
            (rs, rowNum) -> new PaymentResponse(
                    rs.getObject("id", UUID.class),
            rs.getObject("tenant_id", UUID.class),
            rs.getObject("student_financial_account_id", UUID.class),
            rs.getObject("student_id", UUID.class),
            rs.getBigDecimal("amount"),
            rs.getString("currency_code"),
            rs.getString("payment_method"),
            rs.getString("payment_reference"),
            rs.getString("status"),
            instantValue(rs.getObject("paid_at")),
            rs.getObject("eip_payment_transaction_id", UUID.class),
            instantValue(rs.getObject("created_at")),
            instantValue(rs.getObject("updated_at"))
            );

    private final NamedParameterJdbcTemplate jdbc;

    public FinanceStudentPaymentJdbcRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public Optional<FinancialAccountContext> findFinancialAccountContext(
            UUID tenantId,
            UUID accountId
    ) {
        var params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("accountId", accountId);

        List<FinancialAccountContext> rows = jdbc.query(
                "SELECT id, student_id AS student_id, currency_code AS currency_code FROM gts_student_financial_account WHERE tenant_id = :tenantId AND id = :accountId",
                params,
                (rs, rowNum) -> new FinancialAccountContext(
                        rs.getObject("id", UUID.class),
                        rs.getObject("student_id", UUID.class),
                        rs.getString("currency_code")
                )
        );

        return rows.stream().findFirst();
    }

    public PaymentResponse insert(
            UUID tenantId,
            FinancialAccountContext account,
            CreateRequest request,
            String initialStatus,
            String actor
    ) {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        var params = new MapSqlParameterSource();

        columns.add("tenant_id");
    values.add(":tenantId");
    params.addValue("tenantId", tenantId);

    columns.add("student_financial_account_id");
    values.add(":accountId");
    params.addValue("accountId", account.accountId());

    columns.add("payment_amount");
    values.add(":amount");
    params.addValue("amount", request.amount());

    columns.add("student_id");
    values.add(":studentId");
    params.addValue("studentId", account.studentId());

    columns.add("currency_code");
    values.add(":currencyCode");
    params.addValue("currencyCode", account.currencyCode());

    if (true) {
        columns.add("payment_method");
        values.add(":paymentMethod");
        params.addValue("paymentMethod", request.paymentMethod());
    }

    if (true) {
        columns.add("payment_reference");
        values.add(":paymentReference");
        params.addValue("paymentReference", request.paymentReference());
    }

    if (true) {
        columns.add("payment_date");
        values.add(":paidAt");
        params.addValue(
                    "paidAt",
                    request.paidAt().atOffset(java.time.ZoneOffset.UTC),
                    java.sql.Types.TIMESTAMP_WITH_TIMEZONE
            );
    }

        columns.add("created_at");
        values.add("CURRENT_TIMESTAMP");

        columns.add("created_by");
        values.add(":actor");

        columns.add("updated_at");
        values.add("CURRENT_TIMESTAMP");

        columns.add("updated_by");
        values.add(":actor");

        params.addValue("actor", actor);

        String sql =
                "INSERT INTO gts_student_payment ("
                + String.join(", ", columns)
                + ") VALUES ("
                + String.join(", ", values)
                + ") RETURNING id";

        UUID paymentId = jdbc.queryForObject(
                sql,
                params,
                UUID.class
        );

        if (paymentId == null) {
            throw new IllegalStateException(
                    "Student payment insert returned no id"
            );
        }

        return findById(
                tenantId,
                paymentId
        ).orElseThrow(() -> new IllegalStateException(
                "Student payment inserted but cannot be read"
        ));
    }

    public Optional<PaymentResponse> findById(
            UUID tenantId,
            UUID paymentId
    ) {
        var params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("paymentId", paymentId);

        List<PaymentResponse> rows = jdbc.query(
                BASE_SELECT
                + " WHERE p.tenant_id = :tenantId"
                + " AND p.id = :paymentId",
                params,
                PAYMENT_MAPPER
        );

        return rows.stream().findFirst();
    }

    public List<PaymentResponse> findByFinancialAccount(
            UUID tenantId,
            UUID accountId
    ) {
        var params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("accountId", accountId);

        return jdbc.query(
                BASE_SELECT
                + " WHERE p.tenant_id = :tenantId"
                + " AND p.student_financial_account_id = :accountId"
                + " ORDER BY p.created_at DESC, p.id DESC",
                params,
                PAYMENT_MAPPER
        );
    }

    private static Instant instantValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Instant instant) {
            return instant;
        }

        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }

        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(
                    ZoneOffset.UTC
            );
        }

        if (value instanceof LocalDate localDate) {
            return localDate
                    .atStartOfDay()
                    .toInstant(ZoneOffset.UTC);
        }

        throw new IllegalStateException(
                "Unsupported temporal value: "
                + value.getClass().getName()
        );
    }
}
