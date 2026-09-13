package africa.growtogether.platform.school.finance.discount;

import static africa.growtogether.platform.school.finance.discount.FinanceDiscountDtos.*;
import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;
import static org.junit.jupiter.api.Assertions.*;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(
        classMode =
                DirtiesContext.ClassMode.AFTER_CLASS
)
@SpringBootTest
class FinanceDiscountPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_fin_b4_s1"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    @DynamicPropertySource
    static void properties(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.flyway.enabled",
                () -> "true"
        );

        registry.add(
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private FinanceDiscountService discountService;

    @Autowired
    private FinanceFoundationService foundationService;

    @Test
    void discountSchemePersistsReadsListsAndRemainsTenantScoped() {

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

        UUID tenantA =
                createTenant(
                        "FIN_B4_S1_A_"
                                + suffix
                );

        UUID tenantB =
                createTenant(
                        "FIN_B4_S1_B_"
                                + suffix
                );

        String actor =
                UUID.randomUUID()
                        .toString();

        LocalDate effectiveFrom =
                LocalDate.of(
                        2026,
                        1,
                        1
                );

        LocalDate effectiveTo =
                LocalDate.of(
                        2026,
                        12,
                        31
                );

        Map<String, Object> rules =
                Map.<String, Object>of(
                        "tier",
                        "gold",
                        "minimumScore",
                        80
                );

        FeeDiscountSchemeView shared =
                discountService.createFeeDiscountScheme(
                        tenantA,
                        new CreateFeeDiscountSchemeRequest(
                                "SHARED-CODE",
                                "Shared Scholarship",
                                "Tenant-safe scholarship",
                                "SCHOLARSHIP",
                                new BigDecimal(
                                        "25.00"
                                ),
                                null,
                                null,
                                new BigDecimal(
                                        "250000.00"
                                ),
                                rules,
                                effectiveFrom,
                                effectiveTo,
                                false
                        ),
                        actor
                );

        FeeDiscountSchemeView first =
                discountService.createFeeDiscountScheme(
                        tenantA,
                        new CreateFeeDiscountSchemeRequest(
                                "A-FIRST",
                                "First Scheme",
                                null,
                                "FIXED_AMOUNT",
                                new BigDecimal(
                                        "10000.00"
                                ),
                                null,
                                null,
                                null,
                                Map.of(),
                                effectiveFrom,
                                null,
                                true
                        ),
                        actor
                );

        assertEquals(
                tenantA,
                shared.tenantId()
        );

        assertEquals(
                "SHARED-CODE",
                shared.schemeCode()
        );

        assertEquals(
                "SCHOLARSHIP",
                shared.discountType()
        );

        assertEquals(
                rules,
                shared.eligibilityRules()
        );

        assertEquals(
                effectiveFrom,
                shared.effectiveFrom()
        );

        assertEquals(
                effectiveTo,
                shared.effectiveTo()
        );

        assertFalse(
                shared.approvalRequired()
        );

        assertTrue(
                shared.active()
        );

        assertEquals(
                "ACTIVE",
                shared.status()
        );

        assertEquals(
                actor,
                shared.createdBy()
        );

        assertEquals(
                actor,
                shared.updatedBy()
        );

        assertNotNull(
                shared.createdAt()
        );

        assertNotNull(
                shared.updatedAt()
        );

        assertEquals(
                0L,
                shared.version()
        );

        FeeDiscountSchemeView reread =
                discountService.getFeeDiscountScheme(
                        tenantA,
                        shared.id()
                );

        assertEquals(
                shared.id(),
                reread.id()
        );

        assertEquals(
                rules,
                reread.eligibilityRules()
        );

        List<FeeDiscountSchemeView> tenantAList =
                discountService.listFeeDiscountSchemes(
                        tenantA
                );

        assertEquals(
                2,
                tenantAList.size()
        );

        assertEquals(
                List.of(
                        "A-FIRST",
                        "SHARED-CODE"
                ),
                tenantAList.stream()
                        .map(
                                FeeDiscountSchemeView::schemeCode
                        )
                        .toList()
        );

        assertEquals(
                first.id(),
                tenantAList.get(
                        0
                ).id()
        );

        IllegalArgumentException crossTenant =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                discountService.getFeeDiscountScheme(
                                        tenantB,
                                        shared.id()
                                )
                );

        assertEquals(
                "Fee discount scheme is not available in this tenant.",
                crossTenant.getMessage()
        );

        IllegalArgumentException duplicate =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                discountService.createFeeDiscountScheme(
                                        tenantA,
                                        new CreateFeeDiscountSchemeRequest(
                                                "SHARED-CODE",
                                                "Duplicate",
                                                null,
                                                "FIXED_AMOUNT",
                                                BigDecimal.ONE,
                                                null,
                                                null,
                                                null,
                                                Map.of(),
                                                effectiveFrom,
                                                null,
                                                true
                                        ),
                                        actor
                                )
                );

        assertEquals(
                "Fee discount scheme code already exists in this tenant.",
                duplicate.getMessage()
        );

        FeeDiscountSchemeView sameCodeOtherTenant =
                discountService.createFeeDiscountScheme(
                        tenantB,
                        new CreateFeeDiscountSchemeRequest(
                                "SHARED-CODE",
                                "Other Tenant Scheme",
                                null,
                                "WAIVER",
                                BigDecimal.ZERO,
                                null,
                                null,
                                null,
                                Map.of(),
                                effectiveFrom,
                                null,
                                true
                        ),
                        actor
                );

        assertEquals(
                tenantB,
                sameCodeOtherTenant.tenantId()
        );

        assertEquals(
                1,
                discountService.listFeeDiscountSchemes(
                        tenantB
                ).size()
        );

        Integer persisted =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_fee_discount_scheme
                        WHERE tenant_id = ?
                          AND id = ?
                          AND active = TRUE
                          AND status = 'ACTIVE'
                          AND created_by = ?
                          AND updated_by = ?
                          AND version = 0
                        """,
                        Integer.class,
                        tenantA,
                        shared.id(),
                        actor,
                        actor
                );

        assertEquals(
                1,
                persisted
        );
    }

    @Test
    void discountSchemeScopeReferencesRemainTenantSafe() {

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

        UUID tenantA =
                createTenant(
                        "FIN_B4_SCOPE_A_"
                                + suffix
                );

        UUID tenantB =
                createTenant(
                        "FIN_B4_SCOPE_B_"
                                + suffix
                );

        String actor =
                UUID.randomUUID()
                        .toString();

        FeeCategoryView category =
                foundationService.createFeeCategory(
                        tenantA,
                        new CreateFeeCategoryRequest(
                                "FINB4_CAT_"
                                        + suffix,
                                "FIN-B4 Tuition",
                                "FIN-B4 tenant proof",
                                "TUITION",
                                null,
                                false,
                                true,
                                true
                        ),
                        actor
                );

        FeeItemView item =
                foundationService.createFeeItem(
                        tenantA,
                        new CreateFeeItemRequest(
                                category.id(),
                                "FINB4_ITEM_"
                                        + suffix,
                                "FIN-B4 Term Tuition",
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

        LocalDate effectiveFrom =
                LocalDate.of(
                        2026,
                        1,
                        1
                );

        FeeDiscountSchemeView categoryScoped =
                discountService.createFeeDiscountScheme(
                        tenantA,
                        new CreateFeeDiscountSchemeRequest(
                                "CATEGORY-SCOPE-"
                                        + suffix,
                                "Category Scope",
                                null,
                                "PERCENTAGE",
                                new BigDecimal(
                                        "10.00"
                                ),
                                category.id(),
                                null,
                                null,
                                Map.of(),
                                effectiveFrom,
                                null,
                                true
                        ),
                        actor
                );

        FeeDiscountSchemeView itemScoped =
                discountService.createFeeDiscountScheme(
                        tenantA,
                        new CreateFeeDiscountSchemeRequest(
                                "ITEM-SCOPE-"
                                        + suffix,
                                "Item Scope",
                                null,
                                "FIXED_AMOUNT",
                                new BigDecimal(
                                        "15000.00"
                                ),
                                null,
                                item.id(),
                                null,
                                Map.of(),
                                effectiveFrom,
                                null,
                                true
                        ),
                        actor
                );

        assertEquals(
                category.id(),
                categoryScoped.feeCategoryId()
        );

        assertNull(
                categoryScoped.feeItemId()
        );

        assertEquals(
                item.id(),
                itemScoped.feeItemId()
        );

        assertNull(
                itemScoped.feeCategoryId()
        );

        Integer tenantBBefore =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_fee_discount_scheme
                        WHERE tenant_id = ?
                        """,
                        Integer.class,
                        tenantB
                );

        IllegalArgumentException categoryCrossTenant =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                discountService.createFeeDiscountScheme(
                                        tenantB,
                                        new CreateFeeDiscountSchemeRequest(
                                                "CROSS-CATEGORY-"
                                                        + suffix,
                                                "Cross Category",
                                                null,
                                                "FIXED_AMOUNT",
                                                BigDecimal.TEN,
                                                category.id(),
                                                null,
                                                null,
                                                Map.of(),
                                                effectiveFrom,
                                                null,
                                                true
                                        ),
                                        actor
                                )
                );

        assertEquals(
                "feeCategoryId is not available in this tenant.",
                categoryCrossTenant.getMessage()
        );

        IllegalArgumentException itemCrossTenant =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                discountService.createFeeDiscountScheme(
                                        tenantB,
                                        new CreateFeeDiscountSchemeRequest(
                                                "CROSS-ITEM-"
                                                        + suffix,
                                                "Cross Item",
                                                null,
                                                "FIXED_AMOUNT",
                                                BigDecimal.TEN,
                                                null,
                                                item.id(),
                                                null,
                                                Map.of(),
                                                effectiveFrom,
                                                null,
                                                true
                                        ),
                                        actor
                                )
                );

        assertEquals(
                "feeItemId is not available in this tenant.",
                itemCrossTenant.getMessage()
        );

        Integer tenantBAfter =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_fee_discount_scheme
                        WHERE tenant_id = ?
                        """,
                        Integer.class,
                        tenantB
                );

        assertEquals(
                tenantBBefore,
                tenantBAfter
        );

        Integer correctlyScoped =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_fee_discount_scheme
                        WHERE tenant_id = ?
                          AND (
                              fee_category_id = ?
                              OR fee_item_id = ?
                          )
                        """,
                        Integer.class,
                        tenantA,
                        category.id(),
                        item.id()
                );

        assertEquals(
                2,
                correctlyScoped
        );
    }

    private UUID createTenant(
            String code
    ) {

        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();


        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                organizationId,
                code + "_ORG",
                code + " Organisation"
        );


        jdbc.update(
                """
                INSERT INTO eiam_tenant (
                    id,
                    organization_id,
                    code,
                    name,
                    status,
                    created_at,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, 'ACTIVE',
                    CURRENT_TIMESTAMP,
                    0
                )
                """,
                tenantId,
                organizationId,
                code,
                code + " School"
        );


        return tenantId;
    }


    private static String suffix() {

        return UUID.randomUUID()
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
    }
}
