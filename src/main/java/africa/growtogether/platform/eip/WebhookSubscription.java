package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="eip_webhook_subscriptions",uniqueConstraints=@UniqueConstraint(name="uk_eip_webhook_code",columnNames={"tenant_id","subscription_code"}))
public class WebhookSubscription extends AuditedTenantEntity {
 @Column(name="subscription_code",nullable=false,length=100) private String subscriptionCode;
 @Column(name="event_pattern",nullable=false,length=180) private String eventPattern;
 @Column(name="callback_url",nullable=false,length=600) private String callbackUrl;
 @Column(name="secret_ciphertext",nullable=false,columnDefinition="text") private String secretCiphertext;
 @Column(name="secret_key_id",nullable=false,length=100) private String secretKeyId;
 @Column(name="active",nullable=false) private boolean active=true;
 @Column(name="last_delivery_at") private Instant lastDeliveryAt;
 protected WebhookSubscription(){}
 public WebhookSubscription(UUID tenantId,String code,String pattern,String url,String ciphertext,String keyId){setTenantId(tenantId);subscriptionCode=req(code);eventPattern=req(pattern);callbackUrl=req(url);secretCiphertext=req(ciphertext);secretKeyId=req(keyId);}
 private static String req(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("Required value missing");return v.trim();}
 public UUID id(){return getId();} public String subscriptionCode(){return subscriptionCode;} public String eventPattern(){return eventPattern;} public String callbackUrl(){return callbackUrl;} public boolean active(){return active;} public String secretCiphertext(){return secretCiphertext;} public void markDelivered(){lastDeliveryAt=Instant.now();}
}
