package africa.growtogether.platform.ecs;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name="ecs_configuration_values",uniqueConstraints=@UniqueConstraint(name="uk_ecs_value_scope",columnNames={"definition_id","scope","country_code","organization_id","tenant_id"}))
public class ConfigurationValue {
 @Id @GeneratedValue @UuidGenerator private UUID id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="definition_id",nullable=false) private ConfigurationDefinition definition;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ConfigurationScope scope;
 @Column(name="country_code",length=2) private String countryCode;
 @Column(name="organization_id") private UUID organizationId;
 @Column(name="tenant_id") private UUID tenantId;
 @Column(name="stored_value",nullable=false,columnDefinition="text") private String storedValue;
 @Column(name="encrypted",nullable=false) private boolean encrypted;
 @Column(name="encryption_iv",length=100) private String encryptionIv;
 @Column(name="encryption_key_id",length=100) private String encryptionKeyId;
 @Column(name="value_hash",nullable=false,length=64) private String valueHash;
 @Column(name="change_reason",length=500) private String changeReason;
 @Column(nullable=false) private boolean active=true;
 @Version private long version;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 protected ConfigurationValue(){}
 public ConfigurationValue(ConfigurationDefinition d,ConfigurationScope s,String cc,UUID org,UUID tenant){definition=Objects.requireNonNull(d);scope=s;countryCode=cc==null?null:cc.trim().toUpperCase(Locale.ROOT);organizationId=org;tenantId=tenant;validateIdentity();}
 public void writePlain(String value,String reason,String hash){storedValue=value;encrypted=false;encryptionIv=null;encryptionKeyId=null;valueHash=hash;changeReason=reason;active=true;}
 public void writeEncrypted(ConfigurationCryptoService.EncryptedValue value,String reason){storedValue=value.ciphertext();encrypted=true;encryptionIv=value.iv();encryptionKeyId=value.keyId();valueHash=value.valueHash();changeReason=reason;active=true;}
 public void restore(String storedValue,boolean encrypted,String iv,String keyId,String hash,String reason){this.storedValue=storedValue;this.encrypted=encrypted;this.encryptionIv=iv;this.encryptionKeyId=keyId;this.valueHash=hash;this.changeReason=reason;this.active=true;}
 private void validateIdentity(){if(!definition.allows(scope))throw new ConfigurationException("Definition does not allow scope "+scope+".");if(scope==ConfigurationScope.PLATFORM&&(countryCode!=null||organizationId!=null||tenantId!=null))throw new ConfigurationException("Platform values cannot have scope identifiers.");if(scope==ConfigurationScope.COUNTRY&&(countryCode==null||organizationId!=null||tenantId!=null))throw new ConfigurationException("Country scope requires only countryCode.");if(scope==ConfigurationScope.ORGANIZATION&&(organizationId==null||tenantId!=null))throw new ConfigurationException("Organization scope requires organizationId.");if(scope==ConfigurationScope.TENANT&&tenantId==null)throw new ConfigurationException("Tenant scope requires tenantId.");}
 @PrePersist void create(){createdAt=updatedAt=Instant.now();}
 @PreUpdate void touch(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public ConfigurationDefinition getDefinition(){return definition;} public ConfigurationScope getScope(){return scope;} public String getCountryCode(){return countryCode;} public UUID getOrganizationId(){return organizationId;} public UUID getTenantId(){return tenantId;} public String getStoredValue(){return storedValue;} public boolean isEncrypted(){return encrypted;} public String getEncryptionIv(){return encryptionIv;} public String getEncryptionKeyId(){return encryptionKeyId;} public String getValueHash(){return valueHash;} public String getChangeReason(){return changeReason;} public boolean isActive(){return active;} public long getVersion(){return version;}
}
