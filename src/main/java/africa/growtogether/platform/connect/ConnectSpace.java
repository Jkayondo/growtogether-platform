package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(
        name = "gt_connect_spaces",
        indexes = {
                @Index(
                        name = "ix_gt_connect_spaces_tenant_type",
                        columnList = "tenant_id,space_type"
                ),
                @Index(
                        name = "ix_gt_connect_spaces_tenant_context",
                        columnList = "tenant_id,context_type,context_reference"
                )
        }
)
public class ConnectSpace extends AuditedTenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "space_type",
            nullable = false,
            length = 30
    )
    private ConnectSpaceType spaceType;

    @Column(
            name = "name",
            length = 200
    )
    private String name;

    @Column(
            name = "context_type",
            length = 60
    )
    private String contextType;

    @Column(
            name = "context_reference",
            length = 160
    )
    private String contextReference;

    protected ConnectSpace() {
    }

    public ConnectSpace(
            UUID tenantId,
            ConnectSpaceType spaceType,
            String name,
            String contextType,
            String contextReference
    ) {
        setTenantId(required(tenantId, "tenantId"));

        if (spaceType == null) {
            throw new IllegalArgumentException("spaceType is required");
        }

        this.spaceType = spaceType;
        this.name = clean(name);
        this.contextType = clean(contextType);
        this.contextReference = clean(contextReference);
    }

    public ConnectSpaceType getSpaceType() {
        return spaceType;
    }

    public String getName() {
        return name;
    }

    public String getContextType() {
        return contextType;
    }

    public String getContextReference() {
        return contextReference;
    }

    private static UUID required(UUID value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}
