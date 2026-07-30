package africa.growtogether.platform.school.parent.notification.rules;


import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentNotificationRuleService {


    private final ParentNotificationRuleRepository repository;


    public ParentNotificationRuleService(
            ParentNotificationRuleRepository repository
    ) {

        this.repository = repository;
    }


    public ParentNotificationRule create(
            UUID tenantId,
            ParentNotificationRuleType type
    ) {

        return repository.save(
                new ParentNotificationRule(
                        tenantId,
                        type
                )
        );
    }


    public boolean existsEnabledRule(
            UUID tenantId,
            ParentNotificationRuleType type
    ) {

        return repository
                .findByTenantId(tenantId)
                .stream()
                .anyMatch(rule ->
                        rule.isEnabled()
                        &&
                        rule.getRuleType() == type
                );
    }
}
