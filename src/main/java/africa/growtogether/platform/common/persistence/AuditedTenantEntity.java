package africa.growtogether.platform.common.persistence;

import africa.growtogether.platform.common.web.RequestContextHolder;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditedTenantEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 150)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 150)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;

    @PrePersist
    @PreUpdate
    void enforceTenantScope() {
        UUID contextualTenantId = RequestContextHolder.current()
            .map(context -> context.tenantId())
            .filter(value -> value != null && !value.isBlank())
            .map(UUID::fromString)
            .orElse(null);

        if (tenantId == null && contextualTenantId == null) {
            throw new TenantScopeViolationException("A tenant context is required for tenant-scoped persistence.");
        }
        if (tenantId == null) {
            tenantId = contextualTenantId;
            return;
        }
        if (contextualTenantId != null && !Objects.equals(tenantId, contextualTenantId)) {
            throw new TenantScopeViolationException("Entity tenant does not match the active tenant context.");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        if (this.tenantId != null && !this.tenantId.equals(tenantId)) {
            throw new TenantScopeViolationException("Tenant ID is immutable after assignment.");
        }
        this.tenantId = tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public long getVersion() {
        return version;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }
}
