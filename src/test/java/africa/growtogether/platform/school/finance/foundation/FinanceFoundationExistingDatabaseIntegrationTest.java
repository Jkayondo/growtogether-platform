package africa.growtogether.platform.school.finance.foundation;

import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@SpringBootTest
class FinanceFoundationExistingDatabaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;


    @Autowired
    private FinanceFoundationService service;


    @Test
    void existingDatabaseUpgradeSupportsCompleteFoundationLifecycle() {

        Map<String, Object> fixture =
                jdbc.queryForMap(
                        """
                        SELECT
                            sp.tenant_id,
                            ay.id AS academic_year_id,
                            s.id AS student_id
                        FROM gts_school_profile sp
                        JOIN gts_academic_year ay
                          ON ay.tenant_id = sp.tenant_id
                        JOIN gts_student s
                          ON s.tenant_id = sp.tenant_id
                        ORDER BY
                            sp.tenant_id,
                            ay.start_date DESC,
                            s.id
                        LIMIT 1
                        """
                );


        UUID tenantId =
                (UUID) fixture.get(
                        "tenant_id"
                );

        UUID academicYearId =
                (UUID) fixture.get(
                        "academic_year_id"
                );

        UUID studentId =
                (UUID) fixture.get(
                        "student_id"
                );


        UUID otherTenantId =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_tenant
                        WHERE id <> ?
                        ORDER BY id
                        LIMIT 1
                        """,
                        UUID.class,
                        tenantId
                );


        String suffix =
                UUID.randomUUID()
                        .toString()
                        .replace(
                                "-",
                                ""
                        )
                        .substring(
                                0,
                                8
                        )
                        .toUpperCase();


        String actor =
                UUID.randomUUID()
                        .toString();


        FeeCategoryView category =
                service.createFeeCategory(
                        tenantId,
                        new CreateFeeCategoryRequest(
                                "FINB1_CAT_" + suffix,
                                "FIN-B1 Tuition",
                                "Existing-database verification",
                                "TUITION",
                                null,
                                false,
                                true,
                                true
                        ),
                        actor
                );


        FeeItemView item =
                service.createFeeItem(
                        tenantId,
                        new CreateFeeItemRequest(
                                category.id(),
                                "FINB1_ITEM_" + suffix,
                                "FIN-B1 Term Tuition",
                                null,
                                "UGX",
                                new BigDecimal(
                                        "500000.00"
                                ),
                                "TERM",
                                false,
                                true,
                                false,
                                null
                        ),
                        actor
                );


        FeeStructureView structure =
                service.createFeeStructure(
                        tenantId,
                        new CreateFeeStructureRequest(
                                "FINB1_FS_" + suffix,
                                "FIN-B1 Fee Structure",
                                null,
                                academicYearId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "UGX",
                                LocalDate.now(),
                                null
                        ),
                        actor
                );


        assertEquals(
                "DRAFT",
                structure.structureStatus()
        );


        FeeStructureItemView structureItem =
                service.addFeeStructureItem(
                        tenantId,
                        structure.id(),
                        new AddFeeStructureItemRequest(
                                item.id(),
                                new BigDecimal(
                                        "500000.00"
                                ),
                                BigDecimal.ONE,
                                true,
                                false,
                                null,
                                1
                        ),
                        actor
                );


        assertEquals(
                tenantId,
                structureItem.tenantId()
        );


        UUID approverId =
                UUID.randomUUID();


        FeeStructureView approved =
                service.approveFeeStructure(
                        tenantId,
                        structure.id(),
                        approverId,
                        actor
                );


        assertEquals(
                "APPROVED",
                approved.structureStatus()
        );


        FeeStructureView active =
                service.activateFeeStructure(
                        tenantId,
                        structure.id(),
                        actor
                );


        assertEquals(
                "ACTIVE",
                active.structureStatus()
        );


        StudentFinancialAccountView account =
                service.openStudentAccount(
                        tenantId,
                        new OpenStudentFinancialAccountRequest(
                                "FINB1_ACC_" + suffix,
                                studentId,
                                null,
                                "UGX",
                                BigDecimal.ZERO,
                                null
                        ),
                        actor
                );


        assertEquals(
                tenantId,
                account.tenantId()
        );

        assertEquals(
                studentId,
                account.studentId()
        );

        assertEquals(
                "UGX",
                account.currencyCode()
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        account.currentBalance()
                )
        );


        assertTrue(
                service.listFeeCategories(
                        otherTenantId
                ).stream()
                        .noneMatch(
                                value ->
                                        value.id().equals(
                                                category.id()
                                        )
                        )
        );


        assertTrue(
                service.listFeeItems(
                        otherTenantId
                ).stream()
                        .noneMatch(
                                value ->
                                        value.id().equals(
                                                item.id()
                                        )
                        )
        );


        assertTrue(
                service.listFeeStructures(
                        otherTenantId
                ).stream()
                        .noneMatch(
                                value ->
                                        value.id().equals(
                                                structure.id()
                                        )
                        )
        );


        assertTrue(
                service.findStudentAccount(
                        otherTenantId,
                        studentId,
                        "UGX"
                ).isEmpty()
        );


        Integer permissionCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND code IN (
                              'school.finance.read',
                              'school.finance.manage',
                              'school.finance.approve'
                          )
                        """,
                        Integer.class,
                        tenantId
                );


        assertEquals(
                3,
                permissionCount
        );


        Integer persisted =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_fee_structure fs
                        JOIN gts_fee_structure_item fsi
                          ON fsi.tenant_id = fs.tenant_id
                         AND fsi.fee_structure_id = fs.id
                        JOIN gts_student_financial_account a
                          ON a.tenant_id = fs.tenant_id
                        WHERE fs.tenant_id = ?
                          AND fs.id = ?
                          AND fs.structure_status = 'ACTIVE'
                          AND fs.approved_at IS NOT NULL
                          AND fsi.id = ?
                          AND a.id = ?
                        """,
                        Integer.class,
                        tenantId,
                        structure.id(),
                        structureItem.id(),
                        account.id()
                );


        assertEquals(
                1,
                persisted
        );
    }


    @Test
    void crossTenantAcademicYearCannotBeUsedForFeeStructure() {

        List<Map<String, Object>> rows =
                jdbc.queryForList(
                        """
                        SELECT
                            tenant_id,
                            id AS academic_year_id
                        FROM gts_academic_year
                        ORDER BY tenant_id, id
                        """
                );


        Map<String, Object> first =
                rows.get(0);


        UUID sourceTenant =
                (UUID) first.get(
                        "tenant_id"
                );

        UUID foreignAcademicYear =
                rows.stream()
                        .filter(
                                row ->
                                        !sourceTenant.equals(
                                                row.get(
                                                        "tenant_id"
                                                )
                                        )
                        )
                        .map(
                                row ->
                                        (UUID) row.get(
                                                "academic_year_id"
                                        )
                        )
                        .findFirst()
                        .orElse(null);


        if (foreignAcademicYear == null) {
            return;
        }


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createFeeStructure(
                                sourceTenant,
                                new CreateFeeStructureRequest(
                                        "CROSS_"
                                                + UUID.randomUUID(),
                                        "Cross Tenant Structure",
                                        null,
                                        foreignAcademicYear,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        "UGX",
                                        LocalDate.now(),
                                        null
                                ),
                                UUID.randomUUID()
                                        .toString()
                        )
        );
    }
}
