package africa.growtogether.platform.eip.payment;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="eip_payment_provider_attempts",indexes={@Index(name="ix_eip_payment_attempt_tx",columnList="tenant_id,payment_transaction_id,attempt_number")})
public class PaymentProviderAttempt extends AuditedTenantEntity {
 @Column(name="payment_transaction_id",nullable=false) private UUID paymentTransactionId; @Column(name="attempt_number",nullable=false) private int attemptNumber;
 @Column(name="connector_code",nullable=false,length=100) private String connectorCode; @Enumerated(EnumType.STRING) @Column(name="attempt_status",nullable=false,length=30) private ProviderAttemptStatus status=ProviderAttemptStatus.CREATED;
 @Column(name="provider_request_id",length=180) private String providerRequestId; @Column(name="provider_response_code",length=100) private String responseCode; @Column(name="provider_response_message",columnDefinition="text") private String responseMessage; @Column(name="submitted_at") private Instant submittedAt; @Column(name="completed_at") private Instant completedAt;
 protected PaymentProviderAttempt(){} public PaymentProviderAttempt(UUID tenantId,UUID tx,int n,String connector){setTenantId(tenantId);paymentTransactionId=tx;if(n<1)throw new IllegalArgumentException("attemptNumber must be positive");attemptNumber=n;connectorCode=connector;}
 public void submitted(String requestId){status=ProviderAttemptStatus.SUBMITTED;providerRequestId=requestId;submittedAt=Instant.now();} public void accepted(String code,String message){status=ProviderAttemptStatus.ACCEPTED;responseCode=code;responseMessage=message;completedAt=Instant.now();} public void failed(String code,String message){status=ProviderAttemptStatus.FAILED;responseCode=code;responseMessage=message;completedAt=Instant.now();}
}
