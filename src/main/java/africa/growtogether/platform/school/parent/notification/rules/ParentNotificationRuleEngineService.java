package africa.growtogether.platform.school.parent.notification.rules;


import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentNotificationRuleEngineService {


    private final ParentNotificationRuleService ruleService;


    public ParentNotificationRuleEngineService(
            ParentNotificationRuleService ruleService
    ) {

        this.ruleService = ruleService;
    }


    public boolean shouldNotify(
            UUID tenantId,
            ParentNotificationRuleType type
    ) {

        return ruleService.existsEnabledRule(
                tenantId,
                type
        );
    }
}
