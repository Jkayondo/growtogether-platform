package africa.growtogether.platform.school.progression;


import africa.growtogether.platform.school.academic.progression.PromotionDecision;
import africa.growtogether.platform.school.academic.progression.PromotionDecisionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.UUID;
import java.time.Instant;
import java.sql.Timestamp;


import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class PromotionDecisionLifecycleIntegrationTest {


    @Autowired
    private PromotionDecisionService service;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID organizationId;

    private UUID tenantId;

    private UUID promotionRuleId;


    @BeforeEach
    void createFixture() {

        organizationId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, ?)
                """,
                organizationId,
                "PROMOTION-" + shortId(organizationId),
                "Promotion Decision Test Organization",
                Timestamp.from(Instant.now())
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
                VALUES (?, ?, ?, ?, 'ACTIVE', ?, 0)
                """,
                tenantId,
                organizationId,
                "PROMOTION-" + shortId(tenantId),
                "Promotion Decision Test Tenant",
                Timestamp.from(Instant.now())
        );


        promotionRuleId = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_promotion_rule (
                    id,
                    tenant_id,
                    rule_code,
                    rule_name,
                    rule_status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?, ?, ?, 0)
                """,
                promotionRuleId,
                tenantId,
                "DEFAULT-PROMOTION-RULE",
                "Default Promotion Rule",
                Timestamp.from(Instant.now()),
                "test",
                Timestamp.from(Instant.now()),
                "test"
        );

    }


    @Test
    void promotionDecisionLifecycleCanBeControlled() {

        UUID learnerId =
                UUID.randomUUID();

        PromotionDecision decision =
                service.create(
                        tenantId,
                        learnerId,
                        promotionRuleId
                );


        assertNotNull(decision);


        assertEquals(
                "PENDING_REVIEW",
                decision.getDecisionStatus()
        );


        decision =
                service.recommend(decision);


        assertEquals(
                "RECOMMENDED",
                decision.getDecisionStatus()
        );


        decision =
                service.approve(decision);


        assertEquals(
                "APPROVED",
                decision.getDecisionStatus()
        );


        decision =
                service.promote(decision);


        assertEquals(
                "PROMOTED",
                decision.getDecisionStatus()
        );

    }


    @Test
    void learnerPromotionDecisionsCanBeRetrieved() {

        UUID learnerId =
                UUID.randomUUID();

        service.create(
                tenantId,
                learnerId,
                promotionRuleId
        );


        assertFalse(
                service
                        .findByLearner(
                                tenantId,
                                learnerId
                        )
                        .isEmpty()
        );

    }


    
    private String shortId(UUID id) {

        return id.toString()
                .substring(0, 8);

    }

}
