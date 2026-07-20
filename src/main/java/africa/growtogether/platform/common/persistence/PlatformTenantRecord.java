package africa.growtogether.platform.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "platform_tenant_record")
public class PlatformTenantRecord extends AuditedTenantEntity {
    @Column(name = "record_key", nullable = false, length = 100)
    private String recordKey;

    @Column(name = "record_value", nullable = false, length = 500)
    private String recordValue;

    protected PlatformTenantRecord() {
    }

    public PlatformTenantRecord(String recordKey, String recordValue) {
        this.recordKey = recordKey;
        this.recordValue = recordValue;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public String getRecordValue() {
        return recordValue;
    }

    public void setRecordValue(String recordValue) {
        this.recordValue = recordValue;
    }
}
