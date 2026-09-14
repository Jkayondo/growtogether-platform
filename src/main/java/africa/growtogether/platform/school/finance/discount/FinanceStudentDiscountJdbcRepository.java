package africa.growtogether.platform.school.finance.discount;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static africa.growtogether.platform.school.finance.discount.FinanceStudentDiscountDtos.StudentDiscountRequestView;

@Repository
public class FinanceStudentDiscountJdbcRepository {

    private final JdbcTemplate jdbc;

    public FinanceStudentDiscountJdbcRepository(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Transactional
    public StudentDiscountRequestView createStudentDiscountRequest(
            UUID tenantId,
            String discountReference,
            UUID studentId,
            UUID studentFinancialAccountId,
            UUID discountSchemeId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            UUID evidenceDocumentId,
            UUID workflowInstanceId,
            UUID requestedBy,
            String actor
    ) {
        UUID id = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_fee_discount (
                    id,
                    tenant_id,
                    discount_reference,
                    student_id,
                    student_financial_account_id,
                    discount_scheme_id,
                    approved_discount_value,
                    approved_discount_amount,
                    effective_from,
                    effective_to,
                    evidence_document_id,
                    workflow_instance_id,
                    requested_at,
                    requested_by,
                    approved_at,
                    approved_by,
                    discount_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    NULL,
                    NULL,
                    ?,
                    ?,
                    ?,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    NULL,
                    NULL,
                    'PENDING',
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
                discountReference,
                studentId,
                studentFinancialAccountId,
                discountSchemeId,
                effectiveFrom,
                effectiveTo,
                evidenceDocumentId,
                workflowInstanceId,
                requestedBy,
                actor,
                actor
        );

        return getStudentDiscountRequest(
                tenantId,
                id
        ).orElseThrow(
                () -> new IllegalStateException(
                        "Student discount request was not persisted."
                )
        );
    }

    @Transactional(readOnly = true)
    public Optional<StudentDiscountRequestView> getStudentDiscountRequest(
            UUID tenantId,
            UUID studentDiscountId
    ) {
        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    discount_reference,
                    student_id,
                    student_financial_account_id,
                    discount_scheme_id,
                    approved_discount_value,
                    approved_discount_amount,
                    effective_from,
                    effective_to,
                    evidence_document_id,
                    workflow_instance_id,
                    requested_at,
                    requested_by,
                    approved_at,
                    approved_by,
                    discount_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                FROM gts_student_fee_discount
                WHERE tenant_id = ?
                  AND id = ?
                """,
                this::mapStudentDiscountRequest,
                tenantId,
                studentDiscountId
        ).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public List<StudentDiscountRequestView> listStudentDiscountRequests(
            UUID tenantId
    ) {
        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    discount_reference,
                    student_id,
                    student_financial_account_id,
                    discount_scheme_id,
                    approved_discount_value,
                    approved_discount_amount,
                    effective_from,
                    effective_to,
                    evidence_document_id,
                    workflow_instance_id,
                    requested_at,
                    requested_by,
                    approved_at,
                    approved_by,
                    discount_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                FROM gts_student_fee_discount
                WHERE tenant_id = ?
                ORDER BY discount_reference ASC, id ASC
                """,
                this::mapStudentDiscountRequest,
                tenantId
        );
    }

    @Transactional(readOnly = true)
    public boolean existsStudentDiscountReference(
            UUID tenantId,
            String discountReference
    ) {
        Boolean exists = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM gts_student_fee_discount
                    WHERE tenant_id = ?
                      AND discount_reference = ?
                )
                """,
                Boolean.class,
                tenantId,
                discountReference
        );

        return Boolean.TRUE.equals(
                exists
        );
    }

    @Transactional(readOnly = true)

    public StudentDiscountRequestView approveStudentDiscountRequest(
            UUID tenantId,
            UUID studentDiscountId,
            long expectedVersion,
            BigDecimal approvedDiscountValue,
            UUID approverId,
            String actor
    ) {
        int updated =
                jdbc.update(
                        """
                        UPDATE gts_student_fee_discount
                        SET
                            discount_status = 'APPROVED',
                            approved_discount_value = ?,
                            approved_discount_amount = NULL,
                            approved_at = CURRENT_TIMESTAMP,
                            approved_by = ?,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = ?,
                            version = version + 1
                        WHERE tenant_id = ?
                          AND id = ?
                          AND status = 'ACTIVE'
                          AND discount_status = 'PENDING'
                          AND version = ?
                        """,
                        approvedDiscountValue,
                        approverId,
                        actor,
                        tenantId,
                        studentDiscountId,
                        expectedVersion
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Student discount request is not eligible for approval."
            );
        }

        return getStudentDiscountRequest(
                tenantId,
                studentDiscountId
        ).orElseThrow();
    }

    public StudentDiscountRequestView rejectStudentDiscountRequest(
            UUID tenantId,
            UUID studentDiscountId,
            long expectedVersion,
            String actor
    ) {
        int updated =
                jdbc.update(
                        """
                        UPDATE gts_student_fee_discount
                        SET
                            discount_status = 'REJECTED',
                            approved_discount_value = NULL,
                            approved_discount_amount = NULL,
                            approved_at = NULL,
                            approved_by = NULL,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = ?,
                            version = version + 1
                        WHERE tenant_id = ?
                          AND id = ?
                          AND status = 'ACTIVE'
                          AND discount_status = 'PENDING'
                          AND version = ?
                        """,
                        actor,
                        tenantId,
                        studentDiscountId,
                        expectedVersion
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Student discount request is not eligible for rejection."
            );
        }

        return getStudentDiscountRequest(
                tenantId,
                studentDiscountId
        ).orElseThrow();
    }



    @Transactional
    public void applyStudentDiscountToBilling(
            UUID tenantId,
            UUID studentDiscountId,
            long expectedVersion,
            BigDecimal approvedDiscountAmount,
            String actor
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE gts_student_fee_discount
                        SET
                            discount_status = 'ACTIVE',
                            approved_discount_amount = ?,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = ?,
                            version = version + 1
                        WHERE tenant_id = ?
                          AND id = ?
                          AND status = 'ACTIVE'
                          AND discount_status = 'APPROVED'
                          AND approved_discount_amount IS NULL
                          AND version = ?
                        """,
                        approvedDiscountAmount,
                        actor,
                        tenantId,
                        studentDiscountId,
                        expectedVersion
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Student discount request changed before billing application."
            );
        }
    }

    @Transactional
    public void insertAppliedStudentDiscountAdjustment(
            UUID tenantId,
            UUID studentDiscountId,
            UUID studentFinancialAccountId,
            UUID invoiceId,
            BigDecimal adjustmentAmount,
            UUID applyingUserId,
            String actor
    ) {

        int inserted =
                jdbc.update(
                        """
                        INSERT INTO gts_financial_adjustment (
                            tenant_id,
                            adjustment_reference,
                            student_financial_account_id,
                            invoice_id,
                            invoice_line_id,
                            adjustment_type,
                            adjustment_amount,
                            reason,
                            requested_at,
                            requested_by,
                            workflow_instance_id,
                            approved_at,
                            approved_by,
                            applied_at,
                            applied_by,
                            adjustment_status,
                            status,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by,
                            version
                        )
                        VALUES (
                            ?,
                            ?,
                            ?,
                            ?,
                            NULL,
                            'OTHER',
                            ?,
                            'FIN-B4-S3 student discount application',
                            CURRENT_TIMESTAMP,
                            ?,
                            NULL,
                            NULL,
                            NULL,
                            CURRENT_TIMESTAMP,
                            ?,
                            'APPLIED',
                            'ACTIVE',
                            CURRENT_TIMESTAMP,
                            ?,
                            CURRENT_TIMESTAMP,
                            ?,
                            0
                        )
                        """,
                        tenantId,
                        "SFD-" + studentDiscountId,
                        studentFinancialAccountId,
                        invoiceId,
                        adjustmentAmount,
                        applyingUserId,
                        applyingUserId,
                        actor,
                        actor
                );

        if (inserted != 1) {
            throw new IllegalStateException(
                    "Student discount financial adjustment could not be created."
            );
        }
    }

    public Optional<StudentFinancialAccountScope> findStudentFinancialAccountScope(
            UUID tenantId,
            UUID studentFinancialAccountId
    ) {
        return jdbc.query(
                """
                SELECT
                    student_id,
                    billing_status,
                    status
                FROM gts_student_financial_account
                WHERE tenant_id = ?
                  AND id = ?
                """,
                (rs, row) ->
                        new StudentFinancialAccountScope(
                                rs.getObject(
                                        "student_id",
                                        UUID.class
                                ),
                                rs.getString(
                                        "billing_status"
                                ),
                                rs.getString(
                                        "status"
                                )
                        ),
                tenantId,
                studentFinancialAccountId
        ).stream().findFirst();
    }

    private StudentDiscountRequestView mapStudentDiscountRequest(
            ResultSet rs,
            int row
    ) throws SQLException {
        return new StudentDiscountRequestView(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "tenant_id",
                        UUID.class
                ),
                rs.getString(
                        "discount_reference"
                ),
                rs.getObject(
                        "student_id",
                        UUID.class
                ),
                rs.getObject(
                        "student_financial_account_id",
                        UUID.class
                ),
                rs.getObject(
                        "discount_scheme_id",
                        UUID.class
                ),
                rs.getBigDecimal(
                        "approved_discount_value"
                ),
                rs.getBigDecimal(
                        "approved_discount_amount"
                ),
                rs.getObject(
                        "effective_from",
                        LocalDate.class
                ),
                rs.getObject(
                        "effective_to",
                        LocalDate.class
                ),
                rs.getObject(
                        "evidence_document_id",
                        UUID.class
                ),
                rs.getObject(
                        "workflow_instance_id",
                        UUID.class
                ),
                timestampToInstant(
                        rs.getTimestamp(
                                "requested_at"
                        )
                ),
                rs.getObject(
                        "requested_by",
                        UUID.class
                ),
                timestampToInstant(
                        rs.getTimestamp(
                                "approved_at"
                        )
                ),
                rs.getObject(
                        "approved_by",
                        UUID.class
                ),
                rs.getString(
                        "discount_status"
                ),
                rs.getString(
                        "status"
                ),
                timestampToInstant(
                        rs.getTimestamp(
                                "created_at"
                        )
                ),
                rs.getString(
                        "created_by"
                ),
                timestampToInstant(
                        rs.getTimestamp(
                                "updated_at"
                        )
                ),
                rs.getString(
                        "updated_by"
                ),
                rs.getLong(
                        "version"
                )
        );
    }

    private static Instant timestampToInstant(
            Timestamp value
    ) {
        return value == null
                ? null
                : value.toInstant();
    }

    public record StudentFinancialAccountScope(
            UUID studentId,
            String billingStatus,
            String status
    ) {
    }
}
