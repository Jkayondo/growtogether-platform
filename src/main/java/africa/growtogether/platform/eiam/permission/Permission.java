package africa.growtogether.platform.eiam.permission;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "eiam_permission")
public class Permission extends AuditedTenantEntity {
    @Column(nullable = false, length = 150)
    private String code;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(nullable = false, length = 100)
    private String module;
    @Column(length = 500)
    private String description;
    @Column(name = "system_permission", nullable = false)
    private boolean systemPermission;

    protected Permission() {}

    public Permission(String code, String name, String module, String description, boolean systemPermission) {
        this.code = normalizeCode(code);
        this.name = name.trim();
        this.module = module.trim().toUpperCase();
        this.description = normalize(description);
        this.systemPermission = systemPermission;
    }

    public void update(String code, String name, String module, String description) {
        String normalizedCode = normalizeCode(code);
        if (systemPermission && !this.code.equals(normalizedCode)) {
            throw new PermissionLifecycleException("System permission codes cannot be changed.");
        }
        this.code = normalizedCode;
        this.name = name.trim();
        this.module = module.trim().toUpperCase();
        this.description = normalize(description);
    }

    public boolean isAssignable() { return getStatus() == EntityStatus.ACTIVE; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getModule() { return module; }
    public String getDescription() { return description; }
    public boolean isSystemPermission() { return systemPermission; }

    private static String normalizeCode(String value) { return value.trim().toLowerCase().replace(' ', '.'); }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
