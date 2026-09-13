package africa.growtogether.platform.school.finance.discount;

import static africa.growtogether.platform.school.finance.discount.FinanceDiscountDtos.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FinanceDiscountJdbcRepository {

    private static final ObjectMapper JSON =
            new ObjectMapper();

    private static final TypeReference<Map<String, Object>> MAP_TYPE =
            new TypeReference<>() {
            };

    private final JdbcTemplate jdbc;

    public FinanceDiscountJdbcRepository(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Transactional
    public FeeDiscountSchemeView createFeeDiscountScheme(
            UUID tenantId,
            CreateFeeDiscountSchemeRequest request,
            String actor
    ) {

        UUID id =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_fee_discount_scheme (
                    id,
                    tenant_id,
                    scheme_code,
                    scheme_name,
                    description,
                    discount_type,
                    discount_value,
                    fee_category_id,
                    fee_item_id,
                    maximum_discount_amount,
                    eligibility_rules,
                    effective_from,
                    effective_to,
                    approval_required,
                    active,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    CAST(? AS jsonb),
                    ?, ?, ?,
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                id,
                tenantId,
                request.schemeCode(),
                request.schemeName(),
                request.description(),
                request.discountType(),
                request.discountValue(),
                request.feeCategoryId(),
                request.feeItemId(),
                request.maximumDiscountAmount(),
                writeJson(
                        request.eligibilityRules()
                ),
                request.effectiveFrom(),
                request.effectiveTo(),
                request.approvalRequired(),
                actor,
                actor
        );

        return getFeeDiscountScheme(
                tenantId,
                id
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Created fee discount scheme could not be reloaded."
                        )
        );
    }

    @Transactional(readOnly = true)
    public Optional<FeeDiscountSchemeView> getFeeDiscountScheme(
            UUID tenantId,
            UUID discountSchemeId
    ) {

        List<FeeDiscountSchemeView> rows =
                jdbc.query(
                        """
                        SELECT
                            id,
                            tenant_id,
                            scheme_code,
                            scheme_name,
                            description,
                            discount_type,
                            discount_value,
                            fee_category_id,
                            fee_item_id,
                            maximum_discount_amount,
                            eligibility_rules::text
                                AS eligibility_rules_json,
                            effective_from,
                            effective_to,
                            approval_required,
                            active,
                            status,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by,
                            version
                        FROM gts_fee_discount_scheme
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        (rs, rowNum) ->
                                map(
                                        rs
                                ),
                        tenantId,
                        discountSchemeId
                );

        return rows.stream()
                .findFirst();
    }

    @Transactional(readOnly = true)
    public List<FeeDiscountSchemeView> listFeeDiscountSchemes(
            UUID tenantId
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    scheme_code,
                    scheme_name,
                    description,
                    discount_type,
                    discount_value,
                    fee_category_id,
                    fee_item_id,
                    maximum_discount_amount,
                    eligibility_rules::text
                        AS eligibility_rules_json,
                    effective_from,
                    effective_to,
                    approval_required,
                    active,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                FROM gts_fee_discount_scheme
                WHERE tenant_id = ?
                ORDER BY scheme_code ASC, id ASC
                """,
                (rs, rowNum) ->
                        map(
                                rs
                        ),
                tenantId
        );
    }

    @Transactional(readOnly = true)
    public boolean existsFeeDiscountSchemeCode(
            UUID tenantId,
            String schemeCode
    ) {

        Boolean exists =
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_fee_discount_scheme
                            WHERE tenant_id = ?
                              AND scheme_code = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        schemeCode
                );

        return Boolean.TRUE.equals(
                exists
        );
    }

    private static FeeDiscountSchemeView map(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        java.sql.Timestamp createdAt =
                rs.getTimestamp(
                        "created_at"
                );

        java.sql.Timestamp updatedAt =
                rs.getTimestamp(
                        "updated_at"
                );

        return new FeeDiscountSchemeView(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "tenant_id",
                        UUID.class
                ),
                rs.getString(
                        "scheme_code"
                ),
                rs.getString(
                        "scheme_name"
                ),
                rs.getString(
                        "description"
                ),
                rs.getString(
                        "discount_type"
                ),
                rs.getBigDecimal(
                        "discount_value"
                ),
                rs.getObject(
                        "fee_category_id",
                        UUID.class
                ),
                rs.getObject(
                        "fee_item_id",
                        UUID.class
                ),
                rs.getBigDecimal(
                        "maximum_discount_amount"
                ),
                readJson(
                        rs.getString(
                                "eligibility_rules_json"
                        )
                ),
                rs.getObject(
                        "effective_from",
                        java.time.LocalDate.class
                ),
                rs.getObject(
                        "effective_to",
                        java.time.LocalDate.class
                ),
                rs.getBoolean(
                        "approval_required"
                ),
                rs.getBoolean(
                        "active"
                ),
                rs.getString(
                        "status"
                ),
                createdAt == null
                        ? null
                        : createdAt.toInstant(),
                rs.getString(
                        "created_by"
                ),
                updatedAt == null
                        ? null
                        : updatedAt.toInstant(),
                rs.getString(
                        "updated_by"
                ),
                rs.getLong(
                        "version"
                )
        );
    }

    private static String writeJson(
            Map<String, Object> value
    ) {

        Map<String, Object> safe =
                value == null
                        ? Map.of()
                        : value;

        try {
            return JSON.writeValueAsString(
                    safe
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    "eligibilityRules must be a valid JSON object",
                    exception
            );
        }
    }

    private static Map<String, Object> readJson(
            String value
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            return Map.of();
        }

        try {
            return JSON.readValue(
                    value,
                    MAP_TYPE
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Stored eligibility rules are not valid JSON.",
                    exception
            );
        }
    }
}
