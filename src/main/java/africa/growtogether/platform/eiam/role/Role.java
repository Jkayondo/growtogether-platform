package africa.growtogether.platform.eiam.role;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "eiam_role")
public class Role extends AuditedTenantEntity {
    @Column(nullable = false, length = 100)
    private String code;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(name = "system_role", nullable = false)
    private boolean systemRole;

    protected Role() {}

    public Role(String code, String name, String description, boolean systemRole) {
        this.code = normalizeCode(code);
        this.name = name.trim();
        this.description = normalizeDescription(description);
        this.systemRole = systemRole;
    }

    public void update(String code, String name, String description) {
        String normalizedCode = normalizeCode(code);
        if (systemRole && (!this.code.equals(normalizedCode) || !this.name.equals(name.trim()))) {
            throw new RoleLifecycleException("System roles cannot be renamed or recoded.");
        }
        this.code = normalizedCode;
        this.name = name.trim();
        this.description = normalizeDescription(description);
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isSystemRole() { return systemRole; }

    private static String normalizeCode(String value) {
        return value.trim().toUpperCase().replace(' ', '_');
    }
    private static String normalizeDescription(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
