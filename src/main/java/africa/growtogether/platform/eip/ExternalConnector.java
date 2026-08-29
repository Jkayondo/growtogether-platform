package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="eip_external_connectors",uniqueConstraints=@UniqueConstraint(name="uk_eip_connector_code",columnNames={"tenant_id","connector_code"}))
public class ExternalConnector extends AuditedTenantEntity {
 @Column(name="connector_code",nullable=false,length=100) private String connectorCode;
 @Column(name="connector_type",nullable=false,length=80) private String connectorType;
 @Column(name="base_url",nullable=false,length=600) private String baseUrl;
 @Column(name="auth_type",nullable=false,length=50) private String authType;
 @Column(name="credential_ciphertext",columnDefinition="text") private String credentialCiphertext;
 @Column(name="credential_key_id",length=100) private String credentialKeyId;
 @Column(name="provider_configuration",columnDefinition="text") private String providerConfiguration;
 @Column(name="active",nullable=false) private boolean active=true;
 protected ExternalConnector(){}

 public ExternalConnector(
     UUID tenantId,
     String code,
     String type,
     String url,
     String auth,
     String cipher,
     String keyId
 ){
  this(
      tenantId,
      code,
      type,
      url,
      auth,
      cipher,
      keyId,
      null
  );
 }

 public ExternalConnector(
     UUID tenantId,
     String code,
     String type,
     String url,
     String auth,
     String cipher,
     String keyId,
     String providerConfiguration
 ){
  setTenantId(tenantId);
  connectorCode=req(code);
  connectorType=req(type);
  baseUrl=req(url);
  authType=req(auth);
  credentialCiphertext=cipher;
  credentialKeyId=keyId;
  this.providerConfiguration=optional(providerConfiguration);
 }
 private static String req(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("Required value missing");return v.trim();}
 private static String optional(String v){return v==null||v.isBlank()?null:v.trim();}
 public UUID id(){return getId();} public String connectorCode(){return connectorCode;} public String connectorType(){return connectorType;} public String baseUrl(){return baseUrl;} public String authType(){return authType;} public String providerConfiguration(){return providerConfiguration;} public boolean active(){return active;}

 void replaceCredential(String cipher,String keyId){
  if(cipher==null||cipher.isBlank())throw new IllegalArgumentException("Credential ciphertext is required");
  if(keyId==null||keyId.isBlank())throw new IllegalArgumentException("Credential key id is required");
  credentialCiphertext=cipher;
  credentialKeyId=keyId.trim();
 }

 String credentialCiphertext(){
  return credentialCiphertext;
 }

 String credentialKeyId(){
  return credentialKeyId;
 }
}
