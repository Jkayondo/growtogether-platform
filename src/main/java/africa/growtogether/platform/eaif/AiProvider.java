package africa.growtogether.platform.eaif;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
@Entity @Table(name="eaif_providers", uniqueConstraints=@UniqueConstraint(name="uq_eaif_provider_code",columnNames={"tenant_id","provider_code"}))
public class AiProvider extends AuditedTenantEntity {
 @Column(name="provider_code",nullable=false,length=80) private String code;
 @Column(name="display_name",nullable=false,length=160) private String name;
 @Enumerated(EnumType.STRING) @Column(name="provider_type",nullable=false,length=40) private AiEnums.ProviderType type;
 @Column(name="endpoint_url",length=500) private String endpointUrl;
 @Column(name="credential_reference",length=300) private String credentialReference;
 @Column(name="enabled",nullable=false) private boolean enabled=true;
 protected AiProvider(){}
 public AiProvider(java.util.UUID tenantId,String code,String name,AiEnums.ProviderType type){setTenantId(tenantId);this.code=req(code).toUpperCase();this.name=req(name);this.type=java.util.Objects.requireNonNull(type);}
 private static String req(String s){if(s==null||s.isBlank())throw new IllegalArgumentException("value is required");return s.trim();}
 public String code(){return code;} public String name(){return name;} public AiEnums.ProviderType type(){return type;} public boolean enabled(){return enabled;}
}
