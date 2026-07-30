package africa.growtogether.platform.school.parent.notification.rules;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentNotificationRuleRepository
        extends JpaRepository<ParentNotificationRule, UUID> {


    List<ParentNotificationRule>
    findByTenantId(
            UUID tenantId
    );


    List<ParentNotificationRule>
    findByRuleType(
            ParentNotificationRuleType ruleType
    );
}
