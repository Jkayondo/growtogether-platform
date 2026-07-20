package africa.growtogether.platform.eaif;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
@Entity @Table(name="eaif_prompt_templates", uniqueConstraints=@UniqueConstraint(name="uq_eaif_prompt_version",columnNames={"tenant_id","template_code","template_version"}))
public class PromptTemplate extends AuditedTenantEntity {
 @Column(name="template_code",nullable=false,length=100) private String code;
 @Column(name="template_version",nullable=false) private int templateVersion;
 @Column(name="system_prompt",columnDefinition="text") private String systemPrompt;
 @Column(name="user_template",nullable=false,columnDefinition="text") private String userTemplate;
 @Column(name="variables_json",nullable=false,columnDefinition="text") private String variablesJson="[]";
 @Column(name="active",nullable=false) private boolean active=true;
 protected PromptTemplate(){}
 public PromptTemplate(java.util.UUID tenantId,String code,int version,String userTemplate){setTenantId(tenantId);if(version<1)throw new IllegalArgumentException("version must be positive");this.code=req(code).toUpperCase();this.templateVersion=version;this.userTemplate=req(userTemplate);}
 private static String req(String s){if(s==null||s.isBlank())throw new IllegalArgumentException("value is required");return s.trim();}
 public String code(){return code;} public int templateVersion(){return templateVersion;} public String userTemplate(){return userTemplate;}
}
