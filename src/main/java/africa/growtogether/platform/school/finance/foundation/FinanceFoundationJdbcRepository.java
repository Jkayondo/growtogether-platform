package africa.growtogether.platform.school.finance.foundation;

import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


@Repository
public class FinanceFoundationJdbcRepository {

    private static final Set<String> TENANT_REFERENCE_TABLES =
            Set.of(
                    "gts_academic_year",
                    "gts_academic_term",
                    "gts_campus",
                    "gts_academic_programme",
                    "gts_study_track",
                    "gts_class_grade",
                    "gts_stream",
                    "gts_student",
                    "gts_student_enrollment"
            );


    private final JdbcTemplate jdbc;


    public FinanceFoundationJdbcRepository(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    @Transactional
    public FeeCategoryView createFeeCategory(
            UUID tenantId,
            CreateFeeCategoryRequest request,
            String actor
    ) {

        UUID id =
                UUID.randomUUID();


        jdbc.update(
                """
                INSERT INTO gts_fee_category (
                    id,
                    tenant_id,
                    category_code,
                    category_name,
                    description,
                    category_type,
                    accounting_code,
                    refundable,
                    mandatory_by_default,
                    recurring,
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
                request.categoryCode(),
                request.categoryName(),
                request.description(),
                request.categoryType(),
                request.accountingCode(),
                request.refundable(),
                request.mandatoryByDefault(),
                request.recurring(),
                actor,
                actor
        );


        return findFeeCategory(
                tenantId,
                id
        ).orElseThrow();
    }


    @Transactional(readOnly = true)
    public Optional<FeeCategoryView> findFeeCategory(
            UUID tenantId,
            UUID id
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    category_code,
                    category_name,
                    description,
                    category_type,
                    accounting_code,
                    refundable,
                    mandatory_by_default,
                    recurring,
                    active,
                    status
                FROM gts_fee_category
                WHERE tenant_id = ?
                  AND id = ?
                """,
                this::mapFeeCategory,
                tenantId,
                id
        ).stream().findFirst();
    }


    @Transactional(readOnly = true)
    public List<FeeCategoryView> listFeeCategories(
            UUID tenantId
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    category_code,
                    category_name,
                    description,
                    category_type,
                    accounting_code,
                    refundable,
                    mandatory_by_default,
                    recurring,
                    active,
                    status
                FROM gts_fee_category
                WHERE tenant_id = ?
                ORDER BY category_code, id
                """,
                this::mapFeeCategory,
                tenantId
        );
    }


    @Transactional(readOnly = true)
    public boolean existsFeeCategoryCode(
            UUID tenantId,
            String code
    ) {

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_fee_category
                            WHERE tenant_id = ?
                              AND category_code = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        code
                )
        );
    }


    @Transactional
    public FeeItemView createFeeItem(
            UUID tenantId,
            CreateFeeItemRequest request,
            String actor
    ) {

        UUID id =
                UUID.randomUUID();


        jdbc.update(
                """
                INSERT INTO gts_fee_item (
                    id,
                    tenant_id,
                    fee_category_id,
                    item_code,
                    item_name,
                    description,
                    currency_code,
                    default_amount,
                    charge_frequency,
                    quantity_allowed,
                    partial_payment_allowed,
                    tax_applicable,
                    tax_code,
                    active,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
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
                request.feeCategoryId(),
                request.itemCode(),
                request.itemName(),
                request.description(),
                request.currencyCode(),
                request.defaultAmount(),
                request.chargeFrequency(),
                request.quantityAllowed(),
                request.partialPaymentAllowed(),
                request.taxApplicable(),
                request.taxCode(),
                actor,
                actor
        );


        return findFeeItem(
                tenantId,
                id
        ).orElseThrow();
    }


    @Transactional(readOnly = true)
    public Optional<FeeItemView> findFeeItem(
            UUID tenantId,
            UUID id
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    fee_category_id,
                    item_code,
                    item_name,
                    description,
                    currency_code,
                    default_amount,
                    charge_frequency,
                    quantity_allowed,
                    partial_payment_allowed,
                    tax_applicable,
                    tax_code,
                    active,
                    status
                FROM gts_fee_item
                WHERE tenant_id = ?
                  AND id = ?
                """,
                this::mapFeeItem,
                tenantId,
                id
        ).stream().findFirst();
    }


    @Transactional(readOnly = true)
    public List<FeeItemView> listFeeItems(
            UUID tenantId
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    fee_category_id,
                    item_code,
                    item_name,
                    description,
                    currency_code,
                    default_amount,
                    charge_frequency,
                    quantity_allowed,
                    partial_payment_allowed,
                    tax_applicable,
                    tax_code,
                    active,
                    status
                FROM gts_fee_item
                WHERE tenant_id = ?
                ORDER BY item_code, id
                """,
                this::mapFeeItem,
                tenantId
        );
    }


    @Transactional(readOnly = true)
    public boolean existsFeeItemCode(
            UUID tenantId,
            String code
    ) {

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_fee_item
                            WHERE tenant_id = ?
                              AND item_code = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        code
                )
        );
    }


    @Transactional
    public FeeStructureView createFeeStructure(
            UUID tenantId,
            CreateFeeStructureRequest request,
            String actor
    ) {

        UUID id =
                UUID.randomUUID();


        jdbc.update(
                """
                INSERT INTO gts_fee_structure (
                    id,
                    tenant_id,
                    structure_code,
                    structure_name,
                    description,
                    academic_year_id,
                    academic_term_id,
                    campus_id,
                    academic_programme_id,
                    study_track_id,
                    class_grade_id,
                    stream_id,
                    currency_code,
                    effective_from,
                    effective_to,
                    workflow_instance_id,
                    approved_at,
                    approved_by,
                    structure_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    NULL,
                    NULL,
                    NULL,
                    'DRAFT',
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
                request.structureCode(),
                request.structureName(),
                request.description(),
                request.academicYearId(),
                request.academicTermId(),
                request.campusId(),
                request.academicProgrammeId(),
                request.studyTrackId(),
                request.classGradeId(),
                request.streamId(),
                request.currencyCode(),
                request.effectiveFrom(),
                request.effectiveTo(),
                actor,
                actor
        );


        return findFeeStructure(
                tenantId,
                id
        ).orElseThrow();
    }


    @Transactional(readOnly = true)
    public Optional<FeeStructureView> findFeeStructure(
            UUID tenantId,
            UUID id
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    structure_code,
                    structure_name,
                    description,
                    academic_year_id,
                    academic_term_id,
                    campus_id,
                    academic_programme_id,
                    study_track_id,
                    class_grade_id,
                    stream_id,
                    currency_code,
                    effective_from,
                    effective_to,
                    workflow_instance_id,
                    approved_by,
                    structure_status,
                    status
                FROM gts_fee_structure
                WHERE tenant_id = ?
                  AND id = ?
                """,
                this::mapFeeStructure,
                tenantId,
                id
        ).stream().findFirst();
    }


    @Transactional(readOnly = true)
    public List<FeeStructureView> listFeeStructures(
            UUID tenantId
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    structure_code,
                    structure_name,
                    description,
                    academic_year_id,
                    academic_term_id,
                    campus_id,
                    academic_programme_id,
                    study_track_id,
                    class_grade_id,
                    stream_id,
                    currency_code,
                    effective_from,
                    effective_to,
                    workflow_instance_id,
                    approved_by,
                    structure_status,
                    status
                FROM gts_fee_structure
                WHERE tenant_id = ?
                ORDER BY effective_from DESC,
                         structure_code,
                         id
                """,
                this::mapFeeStructure,
                tenantId
        );
    }


    @Transactional(readOnly = true)
    public boolean existsFeeStructureCode(
            UUID tenantId,
            String code
    ) {

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_fee_structure
                            WHERE tenant_id = ?
                              AND structure_code = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        code
                )
        );
    }


    @Transactional
    public FeeStructureItemView addFeeStructureItem(
            UUID tenantId,
            UUID feeStructureId,
            AddFeeStructureItemRequest request,
            String actor
    ) {

        UUID id =
                UUID.randomUUID();


        jdbc.update(
                """
                INSERT INTO gts_fee_structure_item (
                    id,
                    tenant_id,
                    fee_structure_id,
                    fee_item_id,
                    amount,
                    quantity,
                    mandatory,
                    refundable,
                    due_date,
                    sequence_number,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
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
                feeStructureId,
                request.feeItemId(),
                request.amount(),
                request.quantity(),
                request.mandatory(),
                request.refundable(),
                request.dueDate(),
                request.sequenceNumber(),
                actor,
                actor
        );


        return findFeeStructureItem(
                tenantId,
                id
        ).orElseThrow();
    }


    @Transactional(readOnly = true)
    public Optional<FeeStructureItemView> findFeeStructureItem(
            UUID tenantId,
            UUID id
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    fee_structure_id,
                    fee_item_id,
                    amount,
                    quantity,
                    mandatory,
                    refundable,
                    due_date,
                    sequence_number,
                    status
                FROM gts_fee_structure_item
                WHERE tenant_id = ?
                  AND id = ?
                """,
                this::mapFeeStructureItem,
                tenantId,
                id
        ).stream().findFirst();
    }


    @Transactional(readOnly = true)
    public List<FeeStructureItemView> listFeeStructureItems(
            UUID tenantId,
            UUID feeStructureId
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    fee_structure_id,
                    fee_item_id,
                    amount,
                    quantity,
                    mandatory,
                    refundable,
                    due_date,
                    sequence_number,
                    status
                FROM gts_fee_structure_item
                WHERE tenant_id = ?
                  AND fee_structure_id = ?
                ORDER BY sequence_number, id
                """,
                this::mapFeeStructureItem,
                tenantId,
                feeStructureId
        );
    }


    @Transactional(readOnly = true)
    public long countStructureItems(
            UUID tenantId,
            UUID feeStructureId
    ) {

        Long count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_fee_structure_item
                        WHERE tenant_id = ?
                          AND fee_structure_id = ?
                          AND status = 'ACTIVE'
                        """,
                        Long.class,
                        tenantId,
                        feeStructureId
                );


        return count == null
                ? 0L
                : count;
    }


    @Transactional
    public FeeStructureView approveFeeStructure(
            UUID tenantId,
            UUID feeStructureId,
            UUID approverId,
            String actor
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE gts_fee_structure
                        SET
                            structure_status = 'APPROVED',
                            approved_at = CURRENT_TIMESTAMP,
                            approved_by = ?,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = ?,
                            version = version + 1
                        WHERE tenant_id = ?
                          AND id = ?
                          AND structure_status IN (
                              'DRAFT',
                              'UNDER_REVIEW'
                          )
                          AND status = 'ACTIVE'
                        """,
                        approverId,
                        actor,
                        tenantId,
                        feeStructureId
                );


        if (updated != 1) {
            throw new IllegalStateException(
                    "Fee structure is not eligible for approval."
            );
        }


        return findFeeStructure(
                tenantId,
                feeStructureId
        ).orElseThrow();
    }


    @Transactional
    public FeeStructureView activateFeeStructure(
            UUID tenantId,
            UUID feeStructureId,
            String actor
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE gts_fee_structure
                        SET
                            structure_status = 'ACTIVE',
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = ?,
                            version = version + 1
                        WHERE tenant_id = ?
                          AND id = ?
                          AND structure_status = 'APPROVED'
                          AND approved_at IS NOT NULL
                          AND status = 'ACTIVE'
                        """,
                        actor,
                        tenantId,
                        feeStructureId
                );


        if (updated != 1) {
            throw new IllegalStateException(
                    "Only an approved fee structure may be activated."
            );
        }


        return findFeeStructure(
                tenantId,
                feeStructureId
        ).orElseThrow();
    }


    @Transactional
    public StudentFinancialAccountView openStudentAccount(
            UUID tenantId,
            OpenStudentFinancialAccountRequest request,
            String actor
    ) {

        UUID id =
                UUID.randomUUID();


        jdbc.update(
                """
                INSERT INTO gts_student_financial_account (
                    id,
                    tenant_id,
                    account_number,
                    student_id,
                    student_enrollment_id,
                    currency_code,
                    opening_balance,
                    current_balance,
                    credit_balance,
                    credit_limit,
                    billing_status,
                    opened_at,
                    closed_at,
                    closed_by,
                    closure_reason,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, 0, ?,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    NULL,
                    NULL,
                    NULL,
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
                request.accountNumber(),
                request.studentId(),
                request.studentEnrollmentId(),
                request.currencyCode(),
                request.openingBalance(),
                request.openingBalance(),
                request.creditLimit(),
                actor,
                actor
        );


        return findStudentAccount(
                tenantId,
                request.studentId(),
                request.currencyCode()
        ).orElseThrow();
    }


    @Transactional(readOnly = true)
    public Optional<StudentFinancialAccountView> findStudentAccount(
            UUID tenantId,
            UUID studentId,
            String currencyCode
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    account_number,
                    student_id,
                    student_enrollment_id,
                    currency_code,
                    opening_balance,
                    current_balance,
                    credit_balance,
                    credit_limit,
                    billing_status,
                    status
                FROM gts_student_financial_account
                WHERE tenant_id = ?
                  AND student_id = ?
                  AND currency_code = ?
                """,
                this::mapStudentAccount,
                tenantId,
                studentId,
                currencyCode
        ).stream().findFirst();
    }


    @Transactional(readOnly = true)
    public boolean existsStudentAccount(
            UUID tenantId,
            UUID studentId,
            String currencyCode
    ) {

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_student_financial_account
                            WHERE tenant_id = ?
                              AND student_id = ?
                              AND currency_code = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        studentId,
                        currencyCode
                )
        );
    }


    @Transactional(readOnly = true)
    public boolean existsAccountNumber(
            UUID tenantId,
            String accountNumber
    ) {

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_student_financial_account
                            WHERE tenant_id = ?
                              AND account_number = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        accountNumber
                )
        );
    }


    @Transactional(readOnly = true)
    public boolean existsTenantReference(
            String table,
            UUID tenantId,
            UUID id
    ) {

        if (!TENANT_REFERENCE_TABLES.contains(table)) {
            throw new IllegalArgumentException(
                    "Unsupported tenant reference table."
            );
        }


        Boolean exists =
                jdbc.queryForObject(
                        "SELECT EXISTS ("
                                + "SELECT 1 FROM "
                                + table
                                + " WHERE tenant_id = ? AND id = ?"
                                + ")",
                        Boolean.class,
                        tenantId,
                        id
                );


        return Boolean.TRUE.equals(exists);
    }


    @Transactional(readOnly = true)
    public boolean enrollmentBelongsToStudent(
            UUID tenantId,
            UUID enrollmentId,
            UUID studentId
    ) {

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_student_enrollment
                            WHERE tenant_id = ?
                              AND id = ?
                              AND student_id = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        enrollmentId,
                        studentId
                )
        );
    }


    private FeeCategoryView mapFeeCategory(
            ResultSet rs,
            int row
    ) throws SQLException {

        return new FeeCategoryView(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "tenant_id",
                        UUID.class
                ),
                rs.getString(
                        "category_code"
                ),
                rs.getString(
                        "category_name"
                ),
                rs.getString(
                        "description"
                ),
                rs.getString(
                        "category_type"
                ),
                rs.getString(
                        "accounting_code"
                ),
                rs.getBoolean(
                        "refundable"
                ),
                rs.getBoolean(
                        "mandatory_by_default"
                ),
                rs.getBoolean(
                        "recurring"
                ),
                rs.getBoolean(
                        "active"
                ),
                rs.getString(
                        "status"
                )
        );
    }


    private FeeItemView mapFeeItem(
            ResultSet rs,
            int row
    ) throws SQLException {

        return new FeeItemView(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "tenant_id",
                        UUID.class
                ),
                rs.getObject(
                        "fee_category_id",
                        UUID.class
                ),
                rs.getString(
                        "item_code"
                ),
                rs.getString(
                        "item_name"
                ),
                rs.getString(
                        "description"
                ),
                rs.getString(
                        "currency_code"
                ),
                rs.getBigDecimal(
                        "default_amount"
                ),
                rs.getString(
                        "charge_frequency"
                ),
                rs.getBoolean(
                        "quantity_allowed"
                ),
                rs.getBoolean(
                        "partial_payment_allowed"
                ),
                rs.getBoolean(
                        "tax_applicable"
                ),
                rs.getString(
                        "tax_code"
                ),
                rs.getBoolean(
                        "active"
                ),
                rs.getString(
                        "status"
                )
        );
    }


    private FeeStructureView mapFeeStructure(
            ResultSet rs,
            int row
    ) throws SQLException {

        return new FeeStructureView(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "tenant_id",
                        UUID.class
                ),
                rs.getString(
                        "structure_code"
                ),
                rs.getString(
                        "structure_name"
                ),
                rs.getString(
                        "description"
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
                        "campus_id",
                        UUID.class
                ),
                rs.getObject(
                        "academic_programme_id",
                        UUID.class
                ),
                rs.getObject(
                        "study_track_id",
                        UUID.class
                ),
                rs.getObject(
                        "class_grade_id",
                        UUID.class
                ),
                rs.getObject(
                        "stream_id",
                        UUID.class
                ),
                rs.getString(
                        "currency_code"
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
                        "workflow_instance_id",
                        UUID.class
                ),
                rs.getObject(
                        "approved_by",
                        UUID.class
                ),
                rs.getString(
                        "structure_status"
                ),
                rs.getString(
                        "status"
                )
        );
    }


    private FeeStructureItemView mapFeeStructureItem(
            ResultSet rs,
            int row
    ) throws SQLException {

        return new FeeStructureItemView(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "tenant_id",
                        UUID.class
                ),
                rs.getObject(
                        "fee_structure_id",
                        UUID.class
                ),
                rs.getObject(
                        "fee_item_id",
                        UUID.class
                ),
                rs.getBigDecimal(
                        "amount"
                ),
                rs.getBigDecimal(
                        "quantity"
                ),
                rs.getBoolean(
                        "mandatory"
                ),
                rs.getBoolean(
                        "refundable"
                ),
                rs.getObject(
                        "due_date",
                        LocalDate.class
                ),
                rs.getInt(
                        "sequence_number"
                ),
                rs.getString(
                        "status"
                )
        );
    }


    private StudentFinancialAccountView mapStudentAccount(
            ResultSet rs,
            int row
    ) throws SQLException {

        BigDecimal creditLimit =
                rs.getBigDecimal(
                        "credit_limit"
                );


        return new StudentFinancialAccountView(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "tenant_id",
                        UUID.class
                ),
                rs.getString(
                        "account_number"
                ),
                rs.getObject(
                        "student_id",
                        UUID.class
                ),
                rs.getObject(
                        "student_enrollment_id",
                        UUID.class
                ),
                rs.getString(
                        "currency_code"
                ),
                rs.getBigDecimal(
                        "opening_balance"
                ),
                rs.getBigDecimal(
                        "current_balance"
                ),
                rs.getBigDecimal(
                        "credit_balance"
                ),
                creditLimit,
                rs.getString(
                        "billing_status"
                ),
                rs.getString(
                        "status"
                )
        );
    }
}
