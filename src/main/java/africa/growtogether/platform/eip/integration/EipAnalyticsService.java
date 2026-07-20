package africa.growtogether.platform.eip.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eip.*;
import africa.growtogether.platform.eip.payment.*;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operational analytics contract consumed by the future Enterprise Analytics Platform. */
@Service
public class EipAnalyticsService {
    private final IntegrationMessageRepository messages;
    private final PaymentTransactionRepository payments;
    private final EnterpriseIdentityContext identity;
    public EipAnalyticsService(IntegrationMessageRepository messages, PaymentTransactionRepository payments,
                               EnterpriseIdentityContext identity) {
        this.messages = messages; this.payments = payments; this.identity = identity;
    }
    @Transactional(readOnly = true)
    public Map<String,Object> snapshot() {
        var tenant = identity.tenantId();
        var result = new LinkedHashMap<String,Object>();
        result.put("tenantId", tenant);
        result.put("messages", messages.findAll().stream().filter(x -> tenant.equals(x.getTenantId())).count());
        result.put("deadLetters", messages.findAll().stream().filter(x -> tenant.equals(x.getTenantId()) && x.messageStatus() == IntegrationMessageStatus.DEAD_LETTER).count());
        result.put("payments", payments.findAll().stream().filter(x -> tenant.equals(x.getTenantId())).count());
        return result;
    }
}
