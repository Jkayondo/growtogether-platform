package africa.growtogether.platform.eaif;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
@Entity @Table(name="eaif_models", uniqueConstraints=@UniqueConstraint(name="uq_eaif_model_code",columnNames={"tenant_id","model_code"}))
public class AiModel extends AuditedTenantEntity {
 @Column(name="model_code",nullable=false,length=100) private String code;
 @Column(name="provider_code",nullable=false,length=80) private String providerCode;
 @Column(name="provider_model",nullable=false,length=180) private String providerModel;
 @Enumerated(EnumType.STRING) @Column(name="capability",nullable=false,length=40) private AiEnums.Capability capability;
 @Column(name="max_input_tokens") private Integer maxInputTokens;
 @Column(name="max_output_tokens") private Integer maxOutputTokens;
 @Column(name="enabled",nullable=false) private boolean enabled=true;
 protected AiModel(){}
 public AiModel(java.util.UUID tenantId,String code,String providerCode,String providerModel,AiEnums.Capability capability){setTenantId(tenantId);this.code=req(code).toUpperCase();this.providerCode=req(providerCode).toUpperCase();this.providerModel=req(providerModel);this.capability=java.util.Objects.requireNonNull(capability);}
 private static String req(String s){if(s==null||s.isBlank())throw new IllegalArgumentException("value is required");return s.trim();}
 public String code(){return code;} public String providerCode(){return providerCode;} public AiEnums.Capability capability(){return capability;}
}
