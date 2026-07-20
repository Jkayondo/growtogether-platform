package africa.growtogether.platform.eiam.tenant;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="eiam_tenant")
public class Tenant {
 @Id private UUID id; @Column(name="organization_id",nullable=false) private UUID organizationId;
 @Column(nullable=false,unique=true,length=80) private String code; @Column(nullable=false,length=200) private String name;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private TenantStatus status;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt; @Version private long version;
 protected Tenant() {}
 public Tenant(UUID organizationId,String code,String name){this.id=UUID.randomUUID();this.organizationId=organizationId;this.code=normalize(code);this.name=name.trim();this.status=TenantStatus.PROVISIONING;this.createdAt=Instant.now();}
 public void activate(){if(status==TenantStatus.DEACTIVATED)throw new TenantLifecycleException("A deactivated tenant cannot be reactivated.");status=TenantStatus.ACTIVE;}
 public void suspend(){if(status!=TenantStatus.ACTIVE)throw new TenantLifecycleException("Only an active tenant can be suspended.");status=TenantStatus.SUSPENDED;}
 public void deactivate(){status=TenantStatus.DEACTIVATED;}
 public UUID getId(){return id;} public UUID getOrganizationId(){return organizationId;} public String getCode(){return code;} public String getName(){return name;} public TenantStatus getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
 private static String normalize(String value){return value.trim().toUpperCase().replace(' ','_');}
}
