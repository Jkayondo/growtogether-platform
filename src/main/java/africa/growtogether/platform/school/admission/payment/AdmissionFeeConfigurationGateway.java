package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.school.admission.AdmissionApplication;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class AdmissionFeeConfigurationGateway {

    private static final String APPLICABLE_ADMISSION_FEES_SQL = """
            WITH ranked AS (
                SELECT
                    fi.id AS fee_item_id,
                    fi.item_code,
                    fi.item_name,
                    fi.currency_code AS item_currency_code,
                    fi.partial_payment_allowed,
                    fsi.amount,
                    fsi.quantity,
                    fsi.refundable,
                    fsi.due_date,
                    fs.currency_code AS structure_currency_code,

                    ROW_NUMBER() OVER (
                        PARTITION BY fi.id
                        ORDER BY
                            CASE
                                WHEN fs.stream_id IS NOT NULL
                                THEN 1 ELSE 0
                            END DESC,

                            CASE
                                WHEN fs.class_grade_id IS NOT NULL
                                THEN 1 ELSE 0
                            END DESC,

                            CASE
                                WHEN fs.campus_id IS NOT NULL
                                THEN 1 ELSE 0
                            END DESC,

                            fs.effective_from DESC,
                            fs.id
                    ) AS rn

                FROM gts_fee_category fc

                JOIN gts_fee_item fi
                  ON fi.tenant_id = fc.tenant_id
                 AND fi.fee_category_id = fc.id

                JOIN gts_fee_structure_item fsi
                  ON fsi.tenant_id = fi.tenant_id
                 AND fsi.fee_item_id = fi.id

                JOIN gts_fee_structure fs
                  ON fs.tenant_id = fsi.tenant_id
                 AND fs.id = fsi.fee_structure_id

                WHERE fc.tenant_id = ?
                  AND fc.category_type = 'ADMISSION'
                  AND fc.active = TRUE
                  AND fc.status = 'ACTIVE'

                  AND fi.active = TRUE
                  AND fi.status = 'ACTIVE'

                  AND fsi.mandatory = TRUE
                  AND fsi.status = 'ACTIVE'

                  AND fs.academic_year_id = ?
                  AND fs.academic_term_id IS NULL
                  AND fs.academic_programme_id IS NULL
                  AND fs.study_track_id IS NULL

                  AND (
                        fs.campus_id IS NULL
                        OR fs.campus_id = ?
                  )

                  AND (
                        fs.class_grade_id IS NULL
                        OR fs.class_grade_id = ?
                  )

                  AND (
                        fs.stream_id IS NULL
                        OR fs.stream_id = ?
                  )

                  AND (
                        ? IS NOT NULL
                        OR fs.stream_id IS NULL
                  )

                  AND fs.structure_status = 'ACTIVE'
                  AND fs.status = 'ACTIVE'

                  AND fs.effective_from <= ?
                  AND (
                        fs.effective_to IS NULL
                        OR fs.effective_to >= ?
                  )
            )

            SELECT
                fee_item_id,
                item_code,
                item_name,
                item_currency_code,
                partial_payment_allowed,
                amount,
                quantity,
                refundable,
                due_date,
                structure_currency_code
            FROM ranked
            WHERE rn = 1
            ORDER BY item_code, fee_item_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public AdmissionFeeConfigurationGateway(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<AdmissionFeeConfiguration> resolve(
            UUID tenantId,
            AdmissionApplication application
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (application == null) {
            throw new IllegalArgumentException(
                    "application must not be null"
            );
        }

        if (
                application.getAcademicYearId() == null
                || application.getCampusId() == null
                || application.getDesiredClassGradeId() == null
                || application.getApplicationDate() == null
        ) {
            throw new IllegalStateException(
                    "Admission application scope is incomplete"
            );
        }

        return jdbcTemplate.query(
                APPLICABLE_ADMISSION_FEES_SQL,
                (resultSet, rowNumber) -> {

                    String itemCurrency =
                            resultSet.getString(
                                    "item_currency_code"
                            );

                    String structureCurrency =
                            resultSet.getString(
                                    "structure_currency_code"
                            );

                    if (
                            itemCurrency == null
                            || structureCurrency == null
                            || !itemCurrency.equalsIgnoreCase(
                                    structureCurrency
                            )
                    ) {
                        throw new IllegalStateException(
                                "Admission fee currency configuration mismatch"
                        );
                    }

                    Date dueDate =
                            resultSet.getDate(
                                    "due_date"
                            );

                    return new AdmissionFeeConfiguration(
                            resultSet.getObject(
                                    "fee_item_id",
                                    UUID.class
                            ),
                            resultSet.getString(
                                    "item_code"
                            ),
                            resultSet.getString(
                                    "item_name"
                            ),
                            itemCurrency.toUpperCase(),
                            resultSet.getBigDecimal(
                                    "amount"
                            ),
                            resultSet.getBigDecimal(
                                    "quantity"
                            ),
                            resultSet.getBoolean(
                                    "partial_payment_allowed"
                            ),
                            resultSet.getBoolean(
                                    "refundable"
                            ),
                            dueDate == null
                                    ? null
                                    : dueDate.toLocalDate()
                    );
                },
                tenantId,
                application.getAcademicYearId(),
                application.getCampusId(),
                application.getDesiredClassGradeId(),
                application.getDesiredStreamId(),
                application.getDesiredStreamId(),
                application.getApplicationDate(),
                application.getApplicationDate()
        );
    }
}
