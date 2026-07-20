package africa.growtogether.platform.ecs;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name="ecs_configuration_versions", indexes={
    @Index(name="ix_ecs_version_value", columnList="configuration_value_id,version_number"),
    @Index(name="ix_ecs_version_created", columnList="created_at")
})
public class ConfigurationVersion {
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @Column(name="configuration_value_id", nullable=false) private UUID configurationValueId;
    @Column(name="definition_id", nullable=false) private UUID definitionId;
    @Column(name="version_number", nullable=false) private long versionNumber;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ConfigurationScope scope;
    @Column(name="country_code",length=2) private String countryCode;
    @Column(name="organization_id") private UUID organizationId;
    @Column(name="tenant_id") private UUID tenantId;
    @Column(name="stored_value",columnDefinition="text") private String storedValue;
    @Column(name="encrypted",nullable=false) private boolean encrypted;
    @Column(name="encryption_iv",length=100) private String encryptionIv;
    @Column(name="encryption_key_id",length=100) private String encryptionKeyId;
    @Column(name="value_hash",nullable=false,length=64) private String valueHash;
    @Column(name="change_reason",length=500) private String changeReason;
    @Column(name="changed_by",nullable=false,length=160) private String changedBy;
    @Column(name="correlation_id",length=100) private String correlationId;
    @Column(name="rollback_from_version") private Long rollbackFromVersion;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;

    protected ConfigurationVersion() {}

    public ConfigurationVersion(ConfigurationValue value,long versionNumber,String changedBy,String correlationId,Long rollbackFromVersion){
        this.configurationValueId=value.getId(); this.definitionId=value.getDefinition().getId(); this.versionNumber=versionNumber;
        this.scope=value.getScope(); this.countryCode=value.getCountryCode(); this.organizationId=value.getOrganizationId(); this.tenantId=value.getTenantId();
        this.storedValue=value.getStoredValue(); this.encrypted=value.isEncrypted(); this.encryptionIv=value.getEncryptionIv(); this.encryptionKeyId=value.getEncryptionKeyId();
        this.valueHash=value.getValueHash(); this.changeReason=value.getChangeReason(); this.changedBy=changedBy; this.correlationId=correlationId; this.rollbackFromVersion=rollbackFromVersion;
        this.createdAt=Instant.now();
    }

    public UUID getId(){return id;} public UUID getConfigurationValueId(){return configurationValueId;} public long getVersionNumber(){return versionNumber;}
    public String getStoredValue(){return storedValue;} public boolean isEncrypted(){return encrypted;} public String getEncryptionIv(){return encryptionIv;}
    public String getEncryptionKeyId(){return encryptionKeyId;} public String getValueHash(){return valueHash;} public String getChangeReason(){return changeReason;}
    public String getChangedBy(){return changedBy;} public String getCorrelationId(){return correlationId;} public Long getRollbackFromVersion(){return rollbackFromVersion;}
    public Instant getCreatedAt(){return createdAt;}
}
