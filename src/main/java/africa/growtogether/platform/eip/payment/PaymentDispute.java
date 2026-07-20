package africa.growtogether.platform.eip.payment;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="eip_payment_disputes",indexes={@Index(name="ix_eip_dispute_status",columnList="tenant_id,dispute_status,created_at")})
public class PaymentDispute extends AuditedTenantEntity {
 @Column(name="payment_transaction_id",nullable=false) private UUID paymentTransactionId; @Column(name="reason_code",nullable=false,length=100) private String reasonCode; @Column(name="description",nullable=false,columnDefinition="text") private String description; @Enumerated(EnumType.STRING) @Column(name="dispute_status",nullable=false,length=30) private DisputeStatus status=DisputeStatus.OPEN; @Column(name="resolved_at") private Instant resolvedAt; @Column(name="resolution_notes",columnDefinition="text") private String resolutionNotes;
 protected PaymentDispute(){} public PaymentDispute(UUID tenantId,UUID tx,String reason,String description){setTenantId(tenantId);paymentTransactionId=tx;reasonCode=reason;this.description=description;} public void resolve(String notes){status=DisputeStatus.RESOLVED;resolutionNotes=notes;resolvedAt=Instant.now();} public UUID id(){return getId();} public DisputeStatus status(){return status;}
}
