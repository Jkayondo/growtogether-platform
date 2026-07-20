package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="eip_connector_certifications",uniqueConstraints=@UniqueConstraint(name="uk_eip_connector_cert",columnNames={"tenant_id","connector_id","environment"}))
public class ConnectorCertification extends AuditedTenantEntity {
 @Column(name="connector_id",nullable=false) private UUID connectorId;
 @Column(nullable=false,length=30) private String environment;
 @Column(nullable=false,length=30) private String status="PENDING";
 @Column(name="certified_at") private Instant certifiedAt;
 @Column(name="expires_at") private Instant expiresAt;
 @Column(name="evidence_reference",length=500) private String evidenceReference;
 @Column(name="notes",columnDefinition="text") private String notes;
 protected ConnectorCertification(){}
 public ConnectorCertification(UUID tenantId,UUID connectorId,String environment){setTenantId(tenantId);this.connectorId=connectorId;this.environment=environment.trim().toUpperCase();}
 public void certify(String evidence,Instant expiresAt,String notes){this.status="CERTIFIED";this.certifiedAt=Instant.now();this.evidenceReference=evidence;this.expiresAt=expiresAt;this.notes=notes;}
 public void fail(String notes){this.status="FAILED";this.notes=notes;}
 public UUID id(){return getId();} public UUID connectorId(){return connectorId;} public String environment(){return environment;} public String status(){return status;} public Instant certifiedAt(){return certifiedAt;} public Instant expiresAt(){return expiresAt;} public String evidenceReference(){return evidenceReference;} public String notes(){return notes;}
}
