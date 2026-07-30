package africa.growtogether.platform.school.parent.notification.rules;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "parent_notification_rules")
public class ParentNotificationRule
        extends AuditedTenantEntity {


    @Enumerated(EnumType.STRING)
    @Column(
            name = "rule_type",
            nullable = false,
            length = 50
    )
    private ParentNotificationRuleType ruleType;


    @Column(
            name = "enabled",
            nullable = false
    )
    private boolean enabled;


    protected ParentNotificationRule() {
    }


    public ParentNotificationRule(
            UUID tenantId,
            ParentNotificationRuleType ruleType
    ) {

        setTenantId(tenantId);

        this.ruleType = ruleType;
        this.enabled = true;
    }


    public boolean isEnabled() {
        return enabled;
    }


    public ParentNotificationRuleType getRuleType() {
        return ruleType;
    }


    public void disable() {
        this.enabled = false;
    }
}
