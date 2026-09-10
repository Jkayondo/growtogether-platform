package africa.growtogether.platform.school.progression;


import africa.growtogether.platform.school.academic.progression.PromotionRule;
import africa.growtogether.platform.school.academic.progression.PromotionRuleRepository;
import africa.growtogether.platform.school.academic.progression.PromotionRuleService;


import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class PromotionRuleIntegrationTest {


    @Test
    void promotionRuleLifecycleCanBeManaged() {


        PromotionRuleRepository repository = null;


        UUID tenantId =
                UUID.randomUUID();


        PromotionRule rule =
                new PromotionRule(
                        tenantId,
                        "P6_TO_P7_STANDARD",
                        "Primary Six to Primary Seven Promotion"
                );


        assertEquals(
                "P6_TO_P7_STANDARD",
                rule.getRuleCode()
        );


        assertEquals(
                "Primary Six to Primary Seven Promotion",
                rule.getRuleName()
        );


        assertEquals(
                "ACTIVE",
                rule.getRuleStatus()
        );


        rule.deactivate();


        assertEquals(
                "INACTIVE",
                rule.getRuleStatus()
        );


        rule.activate();


        assertEquals(
                "ACTIVE",
                rule.getRuleStatus()
        );

    }

}
