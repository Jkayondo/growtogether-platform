package africa.growtogether.platform.school.finance.foundation;

import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;


@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(
        classMode =
                DirtiesContext.ClassMode.AFTER_CLASS
)
@SpringBootTest
class FinanceFoundationPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_fin_b1"
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
    private FinanceFoundationService service;


    private UUID tenantA;
    private UUID tenantB;


    @BeforeEach
    void seedTenants() {

        tenantA =
                createTenant(
                        "FIN_B1_A_"
                                + suffix()
                );

        tenantB =
                createTenant(
                        "FIN_B1_B_"
                                + suffix()
                );
    }


    @Test
    void financePermissionMigrationAppliesSuccessfully() {

        Integer applied =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE lower(script)
                              LIKE '%school_finance_foundation_permissions%'
                          AND success = TRUE
                        """,
                        Integer.class
                );


        assertEquals(
                1,
                applied
        );
    }


    @Test
    void allFiveFoundationTablesExist() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name IN (
                              'gts_fee_category',
                              'gts_fee_item',
                              'gts_fee_structure',
                              'gts_fee_structure_item',
                              'gts_student_financial_account'
                          )
                        """,
                        Integer.class
                );


        assertEquals(
                5,
                count
        );
    }


    @Test
    void feeCategoryAndItemRemainTenantScoped() {

        String actor =
                UUID.randomUUID().toString();


        FeeCategoryView category =
                service.createFeeCategory(
                        tenantA,
                        new CreateFeeCategoryRequest(
                                "TUITION",
                                "Tuition",
                                null,
                                "TUITION",
                                null,
                                false,
                                true,
                                true
                        ),
                        actor
                );


        assertEquals(
                tenantA,
                category.tenantId()
        );

        assertEquals(
                1,
                service.listFeeCategories(
                        tenantA
                ).size()
        );

        assertTrue(
                service.listFeeCategories(
                        tenantB
                ).isEmpty()
        );


        FeeItemView item =
                service.createFeeItem(
                        tenantA,
                        new CreateFeeItemRequest(
                                category.id(),
                                "TUITION_TERM",
                                "Term Tuition",
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


        assertEquals(
                tenantA,
                item.tenantId()
        );

        assertEquals(
                1,
                service.listFeeItems(
                        tenantA
                ).size()
        );

        assertTrue(
                service.listFeeItems(
                        tenantB
                ).isEmpty()
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createFeeItem(
                                tenantB,
                                new CreateFeeItemRequest(
                                        category.id(),
                                        "CROSS_TENANT",
                                        "Cross Tenant",
                                        null,
                                        "UGX",
                                        BigDecimal.ONE,
                                        "TERM",
                                        false,
                                        true,
                                        false,
                                        null
                                ),
                                actor
                        )
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
