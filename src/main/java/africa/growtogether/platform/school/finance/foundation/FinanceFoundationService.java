package africa.growtogether.platform.school.finance.foundation;

import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


@Service
public class FinanceFoundationService {

    private static final Set<String> CATEGORY_TYPES =
            Set.of(
                    "TUITION",
                    "ADMISSION",
                    "REGISTRATION",
                    "BOARDING",
                    "TRANSPORT",
                    "MEALS",
                    "EXAMINATION",
                    "LIBRARY",
                    "LABORATORY",
                    "ICT",
                    "UNIFORM",
                    "ACTIVITY",
                    "MEDICAL",
                    "DEVELOPMENT",
                    "SECURITY",
                    "GRADUATION",
                    "PENALTY",
                    "OTHER"
            );


    private static final Set<String> CHARGE_FREQUENCIES =
            Set.of(
                    "ONCE",
                    "DAILY",
                    "WEEKLY",
                    "MONTHLY",
                    "TERM",
                    "SEMESTER",
                    "ACADEMIC_YEAR",
                    "PER_USE",
                    "CUSTOM"
            );


    private final FinanceFoundationJdbcRepository repository;


    public FinanceFoundationService(
            FinanceFoundationJdbcRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public FeeCategoryView createFeeCategory(
            UUID tenantId,
            CreateFeeCategoryRequest request,
            String actor
    ) {

        requireTenant(tenantId);
        requireActor(actor);
        requireRequest(request);


        String code =
                normalizeCode(
                        request.categoryCode(),
                        "categoryCode"
                );

        String name =
                requiredText(
                        request.categoryName(),
                        "categoryName"
                );

        String type =
                normalizeEnum(
                        request.categoryType(),
                        "categoryType"
                );


        if (!CATEGORY_TYPES.contains(type)) {
            throw new IllegalArgumentException(
                    "Unsupported fee category type."
            );
        }


        if (
                repository.existsFeeCategoryCode(
                        tenantId,
                        code
                )
        ) {
            throw new IllegalStateException(
                    "Fee category code already exists."
            );
        }


        CreateFeeCategoryRequest normalized =
                new CreateFeeCategoryRequest(
                        code,
                        name,
                        trimToNull(
                                request.description()
                        ),
                        type,
                        trimToNull(
                                request.accountingCode()
                        ),
                        defaultBoolean(
                                request.refundable(),
                                false
                        ),
                        defaultBoolean(
                                request.mandatoryByDefault(),
                                true
                        ),
                        defaultBoolean(
                                request.recurring(),
                                true
                        )
                );


        return repository.createFeeCategory(
                tenantId,
                normalized,
                actor
        );
    }


    @Transactional(readOnly = true)
    public List<FeeCategoryView> listFeeCategories(
            UUID tenantId
    ) {

        requireTenant(tenantId);

        return repository.listFeeCategories(
                tenantId
        );
    }


    @Transactional
    public FeeItemView createFeeItem(
            UUID tenantId,
            CreateFeeItemRequest request,
            String actor
    ) {

        requireTenant(tenantId);
        requireActor(actor);
        requireRequest(request);


        if (request.feeCategoryId() == null) {
            throw new IllegalArgumentException(
                    "feeCategoryId must not be null"
            );
        }


        repository.findFeeCategory(
                tenantId,
                request.feeCategoryId()
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Fee category is not available in this tenant."
                )
        );


        String code =
                normalizeCode(
                        request.itemCode(),
                        "itemCode"
                );

        String name =
                requiredText(
                        request.itemName(),
                        "itemName"
                );

        String currency =
                normalizeCurrency(
                        request.currencyCode()
                );

        String frequency =
                normalizeEnum(
                        request.chargeFrequency() == null
                                ? "TERM"
                                : request.chargeFrequency(),
                        "chargeFrequency"
                );


        if (
                !CHARGE_FREQUENCIES.contains(
                        frequency
                )
        ) {
            throw new IllegalArgumentException(
                    "Unsupported charge frequency."
            );
        }


        requireNonNegative(
                request.defaultAmount(),
                "defaultAmount",
                true
        );


        if (
                repository.existsFeeItemCode(
                        tenantId,
                        code
                )
        ) {
            throw new IllegalStateException(
                    "Fee item code already exists."
            );
        }


        CreateFeeItemRequest normalized =
                new CreateFeeItemRequest(
                        request.feeCategoryId(),
                        code,
                        name,
                        trimToNull(
                                request.description()
                        ),
                        currency,
                        request.defaultAmount(),
                        frequency,
                        defaultBoolean(
                                request.quantityAllowed(),
                                false
                        ),
                        defaultBoolean(
                                request.partialPaymentAllowed(),
                                true
                        ),
                        defaultBoolean(
                                request.taxApplicable(),
                                false
                        ),
                        trimToNull(
                                request.taxCode()
                        )
                );


        return repository.createFeeItem(
                tenantId,
                normalized,
                actor
        );
    }


    @Transactional(readOnly = true)
    public List<FeeItemView> listFeeItems(
            UUID tenantId
    ) {

        requireTenant(tenantId);

        return repository.listFeeItems(
                tenantId
        );
    }


    @Transactional
    public FeeStructureView createFeeStructure(
            UUID tenantId,
            CreateFeeStructureRequest request,
            String actor
    ) {

        requireTenant(tenantId);
        requireActor(actor);
        requireRequest(request);


        String code =
                normalizeCode(
                        request.structureCode(),
                        "structureCode"
                );

        String name =
                requiredText(
                        request.structureName(),
                        "structureName"
                );

        String currency =
                normalizeCurrency(
                        request.currencyCode()
                );


        if (request.academicYearId() == null) {
            throw new IllegalArgumentException(
                    "academicYearId must not be null"
            );
        }


        requireReference(
                "gts_academic_year",
                tenantId,
                request.academicYearId(),
                "academicYearId"
        );

        optionalReference(
                "gts_academic_term",
                tenantId,
                request.academicTermId(),
                "academicTermId"
        );

        optionalReference(
                "gts_campus",
                tenantId,
                request.campusId(),
                "campusId"
        );

        optionalReference(
                "gts_academic_programme",
                tenantId,
                request.academicProgrammeId(),
                "academicProgrammeId"
        );

        optionalReference(
                "gts_study_track",
                tenantId,
                request.studyTrackId(),
                "studyTrackId"
        );

        optionalReference(
                "gts_class_grade",
                tenantId,
                request.classGradeId(),
                "classGradeId"
        );

        optionalReference(
                "gts_stream",
                tenantId,
                request.streamId(),
                "streamId"
        );


        if (request.effectiveFrom() == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom must not be null"
            );
        }


        if (
                request.effectiveTo() != null
                && request.effectiveTo().isBefore(
                        request.effectiveFrom()
                )
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }


        if (
                repository.existsFeeStructureCode(
                        tenantId,
                        code
                )
        ) {
            throw new IllegalStateException(
                    "Fee structure code already exists."
            );
        }


        CreateFeeStructureRequest normalized =
                new CreateFeeStructureRequest(
                        code,
                        name,
                        trimToNull(
                                request.description()
                        ),
                        request.academicYearId(),
                        request.academicTermId(),
                        request.campusId(),
                        request.academicProgrammeId(),
                        request.studyTrackId(),
                        request.classGradeId(),
                        request.streamId(),
                        currency,
                        request.effectiveFrom(),
                        request.effectiveTo()
                );


        return repository.createFeeStructure(
                tenantId,
                normalized,
                actor
        );
    }


    @Transactional(readOnly = true)
    public List<FeeStructureView> listFeeStructures(
            UUID tenantId
    ) {

        requireTenant(tenantId);

        return repository.listFeeStructures(
                tenantId
        );
    }


    @Transactional
    public FeeStructureItemView addFeeStructureItem(
            UUID tenantId,
            UUID feeStructureId,
            AddFeeStructureItemRequest request,
            String actor
    ) {

        requireTenant(tenantId);
        requireActor(actor);
        requireRequest(request);


        if (feeStructureId == null) {
            throw new IllegalArgumentException(
                    "feeStructureId must not be null"
            );
        }


        if (request.feeItemId() == null) {
            throw new IllegalArgumentException(
                    "feeItemId must not be null"
            );
        }


        FeeStructureView structure =
                repository.findFeeStructure(
                        tenantId,
                        feeStructureId
                ).orElseThrow(
                        () -> new IllegalArgumentException(
                                "Fee structure is not available in this tenant."
                        )
                );


        if (
                !"DRAFT".equals(
                        structure.structureStatus()
                )
                && !"UNDER_REVIEW".equals(
                        structure.structureStatus()
                )
        ) {
            throw new IllegalStateException(
                    "Fee structure items may only be changed before approval."
            );
        }


        FeeItemView item =
                repository.findFeeItem(
                        tenantId,
                        request.feeItemId()
                ).orElseThrow(
                        () -> new IllegalArgumentException(
                                "Fee item is not available in this tenant."
                        )
                );


        if (
                !structure.currencyCode().equalsIgnoreCase(
                        item.currencyCode()
                )
        ) {
            throw new IllegalArgumentException(
                    "Fee structure and fee item currency must match."
            );
        }


        requireNonNegative(
                request.amount(),
                "amount",
                false
        );


        BigDecimal quantity =
                request.quantity() == null
                        ? BigDecimal.ONE
                        : request.quantity();


        if (
                quantity.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {
            throw new IllegalArgumentException(
                    "quantity must be greater than zero"
            );
        }


        if (
                request.sequenceNumber() == null
                || request.sequenceNumber() <= 0
        ) {
            throw new IllegalArgumentException(
                    "sequenceNumber must be greater than zero"
            );
        }


        AddFeeStructureItemRequest normalized =
                new AddFeeStructureItemRequest(
                        request.feeItemId(),
                        request.amount(),
                        quantity,
                        defaultBoolean(
                                request.mandatory(),
                                true
                        ),
                        defaultBoolean(
                                request.refundable(),
                                false
                        ),
                        request.dueDate(),
                        request.sequenceNumber()
                );


        return repository.addFeeStructureItem(
                tenantId,
                feeStructureId,
                normalized,
                actor
        );
    }


    @Transactional(readOnly = true)
    public List<FeeStructureItemView> listFeeStructureItems(
            UUID tenantId,
            UUID feeStructureId
    ) {

        requireTenant(tenantId);


        repository.findFeeStructure(
                tenantId,
                feeStructureId
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Fee structure is not available in this tenant."
                )
        );


        return repository.listFeeStructureItems(
                tenantId,
                feeStructureId
        );
    }


    @Transactional
    public FeeStructureView approveFeeStructure(
            UUID tenantId,
            UUID feeStructureId,
            UUID approverId,
            String actor
    ) {

        requireTenant(tenantId);
        requireActor(actor);


        if (approverId == null) {
            throw new IllegalArgumentException(
                    "approverId must not be null"
            );
        }


        repository.findFeeStructure(
                tenantId,
                feeStructureId
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Fee structure is not available in this tenant."
                )
        );


        if (
                repository.countStructureItems(
                        tenantId,
                        feeStructureId
                ) <= 0
        ) {
            throw new IllegalStateException(
                    "A fee structure must contain at least one active item before approval."
            );
        }


        return repository.approveFeeStructure(
                tenantId,
                feeStructureId,
                approverId,
                actor
        );
    }


    @Transactional
    public FeeStructureView activateFeeStructure(
            UUID tenantId,
            UUID feeStructureId,
            String actor
    ) {

        requireTenant(tenantId);
        requireActor(actor);


        repository.findFeeStructure(
                tenantId,
                feeStructureId
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Fee structure is not available in this tenant."
                )
        );


        return repository.activateFeeStructure(
                tenantId,
                feeStructureId,
                actor
        );
    }


    @Transactional
    public StudentFinancialAccountView openStudentAccount(
            UUID tenantId,
            OpenStudentFinancialAccountRequest request,
            String actor
    ) {

        requireTenant(tenantId);
        requireActor(actor);
        requireRequest(request);


        String accountNumber =
                normalizeCode(
                        request.accountNumber(),
                        "accountNumber"
                );

        String currency =
                normalizeCurrency(
                        request.currencyCode()
                );


        if (request.studentId() == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }


        requireReference(
                "gts_student",
                tenantId,
                request.studentId(),
                "studentId"
        );


        if (request.studentEnrollmentId() != null) {

            if (
                    !repository.enrollmentBelongsToStudent(
                            tenantId,
                            request.studentEnrollmentId(),
                            request.studentId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Student enrollment is not available for this student in this tenant."
                );
            }
        }


        BigDecimal openingBalance =
                request.openingBalance() == null
                        ? BigDecimal.ZERO
                        : request.openingBalance();


        requireNonNegative(
                openingBalance,
                "openingBalance",
                false
        );

        requireNonNegative(
                request.creditLimit(),
                "creditLimit",
                true
        );


        if (
                repository.existsStudentAccount(
                        tenantId,
                        request.studentId(),
                        currency
                )
        ) {
            throw new IllegalStateException(
                    "The student already has a financial account in this currency."
            );
        }


        if (
                repository.existsAccountNumber(
                        tenantId,
                        accountNumber
                )
        ) {
            throw new IllegalStateException(
                    "Financial account number already exists."
            );
        }


        OpenStudentFinancialAccountRequest normalized =
                new OpenStudentFinancialAccountRequest(
                        accountNumber,
                        request.studentId(),
                        request.studentEnrollmentId(),
                        currency,
                        openingBalance,
                        request.creditLimit()
                );


        return repository.openStudentAccount(
                tenantId,
                normalized,
                actor
        );
    }


    @Transactional(readOnly = true)
    public Optional<StudentFinancialAccountView> findStudentAccount(
            UUID tenantId,
            UUID studentId,
            String currencyCode
    ) {

        requireTenant(tenantId);

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }


        return repository.findStudentAccount(
                tenantId,
                studentId,
                normalizeCurrency(
                        currencyCode
                )
        );
    }


    private void requireReference(
            String table,
            UUID tenantId,
            UUID id,
            String field
    ) {

        if (
                !repository.existsTenantReference(
                        table,
                        tenantId,
                        id
                )
        ) {
            throw new IllegalArgumentException(
                    field
                            + " is not available in this tenant."
            );
        }
    }


    private void optionalReference(
            String table,
            UUID tenantId,
            UUID id,
            String field
    ) {

        if (id != null) {
            requireReference(
                    table,
                    tenantId,
                    id,
                    field
            );
        }
    }


    private static void requireTenant(
            UUID tenantId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }
    }


    private static void requireActor(
            String actor
    ) {

        if (
                actor == null
                || actor.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "actor must not be blank"
            );
        }
    }


    private static void requireRequest(
            Object request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "request must not be null"
            );
        }
    }


    private static String normalizeCode(
            String value,
            String field
    ) {

        return requiredText(
                value,
                field
        )
                .toUpperCase(
                        Locale.ROOT
                )
                .replace(
                        ' ',
                        '_'
                );
    }


    private static String normalizeEnum(
            String value,
            String field
    ) {

        return requiredText(
                value,
                field
        ).toUpperCase(
                Locale.ROOT
        );
    }


    private static String normalizeCurrency(
            String value
    ) {

        String currency =
                normalizeEnum(
                        value,
                        "currencyCode"
                );


        if (currency.length() != 3) {
            throw new IllegalArgumentException(
                    "currencyCode must contain exactly three characters"
            );
        }


        return currency;
    }


    private static String requiredText(
            String value,
            String field
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    field
                            + " must not be blank"
            );
        }


        return value.trim();
    }


    private static String trimToNull(
            String value
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            return null;
        }


        return value.trim();
    }


    private static boolean defaultBoolean(
            Boolean value,
            boolean fallback
    ) {

        return value == null
                ? fallback
                : value;
    }


    private static void requireNonNegative(
            BigDecimal value,
            String field,
            boolean nullable
    ) {

        if (value == null) {

            if (nullable) {
                return;
            }

            throw new IllegalArgumentException(
                    field
                            + " must not be null"
            );
        }


        if (
                value.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    field
                            + " must not be negative"
            );
        }
    }
}
