package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="eip_transformation_rules",uniqueConstraints=@UniqueConstraint(name="uk_eip_transform_code",columnNames={"tenant_id","rule_code"}))
public class TransformationRule extends AuditedTenantEntity {
 @Column(name="rule_code",nullable=false,length=100) private String ruleCode;
 @Column(name="source_content_type",nullable=false,length=100) private String sourceContentType;
 @Column(name="target_content_type",nullable=false,length=100) private String targetContentType;
 @Column(name="mapping_expression",nullable=false,columnDefinition="text") private String mappingExpression;
 @Column(name="active",nullable=false) private boolean active=true;
 protected TransformationRule(){}
 public TransformationRule(UUID tenantId,String code,String source,String target,String expression){setTenantId(tenantId);ruleCode=req(code);sourceContentType=req(source);targetContentType=req(target);mappingExpression=req(expression);}
 private static String req(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("Required value missing");return v.trim();}
 public UUID id(){return getId();} public String ruleCode(){return ruleCode;} public String sourceContentType(){return sourceContentType;} public String targetContentType(){return targetContentType;} public String mappingExpression(){return mappingExpression;}
}
