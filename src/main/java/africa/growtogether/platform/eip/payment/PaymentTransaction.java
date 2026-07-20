package africa.growtogether.platform.eip.payment;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="eip_payment_transactions", indexes={
 @Index(name="ux_eip_payment_idempotency",columnList="tenant_id,idempotency_key",unique=true),
 @Index(name="ix_eip_payment_reference",columnList="tenant_id,merchant_reference"),
 @Index(name="ix_eip_payment_status",columnList="tenant_id,payment_status,created_at")})
public class PaymentTransaction extends AuditedTenantEntity {
 @Column(name="merchant_reference",nullable=false,length=120) private String merchantReference;
 @Column(name="payer_reference",length=160) private String payerReference;
 @Column(name="amount",nullable=false,precision=19,scale=4) private BigDecimal amount;
 @Column(name="currency",nullable=false,length=3) private String currency;
 @Enumerated(EnumType.STRING) @Column(name="payment_channel",nullable=false,length=30) private PaymentChannel channel;
 @Column(name="connector_code",nullable=false,length=100) private String connectorCode;
 @Column(name="idempotency_key",nullable=false,length=180) private String idempotencyKey;
 @Enumerated(EnumType.STRING) @Column(name="payment_status",nullable=false,length=30) private PaymentTransactionStatus status=PaymentTransactionStatus.CREATED;
 @Column(name="provider_reference",length=180) private String providerReference;
 @Column(name="authorized_at") private Instant authorizedAt; @Column(name="completed_at") private Instant completedAt;
 @Column(name="failure_code",length=100) private String failureCode; @Column(name="failure_message",columnDefinition="text") private String failureMessage;
 protected PaymentTransaction(){}
 public PaymentTransaction(UUID tenantId,String merchantReference,String payerReference,BigDecimal amount,String currency,PaymentChannel channel,String connectorCode,String idempotencyKey){setTenantId(tenantId);this.merchantReference=req(merchantReference,"merchantReference");this.payerReference=payerReference; if(amount==null||amount.signum()<=0)throw new IllegalArgumentException("amount must be positive");this.amount=amount;this.currency=req(currency,"currency").toUpperCase();if(this.currency.length()!=3)throw new IllegalArgumentException("currency must be ISO-4217");this.channel=channel==null?PaymentChannel.MOBILE_MONEY:channel;this.connectorCode=req(connectorCode,"connectorCode");this.idempotencyKey=req(idempotencyKey,"idempotencyKey");}
 private static String req(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
 public void submit(){require(PaymentTransactionStatus.CREATED);status=PaymentTransactionStatus.PENDING_PROVIDER;}
 public void authorize(String ref){if(status!=PaymentTransactionStatus.PENDING_PROVIDER&&status!=PaymentTransactionStatus.CREATED)throw new IllegalStateException("Payment is not authorizable");providerReference=req(ref,"providerReference");status=PaymentTransactionStatus.AUTHORIZED;authorizedAt=Instant.now();}
 public void succeed(String ref){if(status!=PaymentTransactionStatus.PENDING_PROVIDER&&status!=PaymentTransactionStatus.AUTHORIZED)throw new IllegalStateException("Payment is not completable");providerReference=req(ref,"providerReference");status=PaymentTransactionStatus.SUCCEEDED;completedAt=Instant.now();failureCode=null;failureMessage=null;}
 public void fail(String code,String message){if(status==PaymentTransactionStatus.SUCCEEDED||status==PaymentTransactionStatus.REFUNDED)throw new IllegalStateException("Completed payment cannot fail");status=PaymentTransactionStatus.FAILED;failureCode=req(code,"failureCode");failureMessage=req(message,"failureMessage");completedAt=Instant.now();}
 public void cancel(){if(status!=PaymentTransactionStatus.CREATED&&status!=PaymentTransactionStatus.PENDING_PROVIDER)throw new IllegalStateException("Payment cannot be cancelled");status=PaymentTransactionStatus.CANCELLED;completedAt=Instant.now();}
 public void reverse(){if(status!=PaymentTransactionStatus.SUCCEEDED)throw new IllegalStateException("Only succeeded payment can be reversed");status=PaymentTransactionStatus.REVERSED;completedAt=Instant.now();}
 private void require(PaymentTransactionStatus expected){if(status!=expected)throw new IllegalStateException("Expected "+expected+" but was "+status);}
 public UUID id(){return getId();} public String merchantReference(){return merchantReference;} public BigDecimal amount(){return amount;} public String currency(){return currency;} public PaymentChannel channel(){return channel;} public String connectorCode(){return connectorCode;} public String idempotencyKey(){return idempotencyKey;} public PaymentTransactionStatus status(){return status;} public String providerReference(){return providerReference;} public Instant completedAt(){return completedAt;} public String failureCode(){return failureCode;}
}
