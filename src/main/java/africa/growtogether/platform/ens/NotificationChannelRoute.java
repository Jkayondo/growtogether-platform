package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ens_notification_channel_routes")
public class NotificationChannelRoute extends AuditedTenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "connector_id", nullable = false)
    private UUID connectorId;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "failover_enabled", nullable = false)
    private boolean failoverEnabled = true;

    protected NotificationChannelRoute() {
    }

    public NotificationChannelRoute(
            UUID tenantId,
            NotificationChannel channel,
            UUID connectorId,
            int priority
    ) {
        setTenantId(Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        ));

        this.channel = Objects.requireNonNull(
                channel,
                "channel must not be null"
        );

        this.connectorId = Objects.requireNonNull(
                connectorId,
                "connectorId must not be null"
        );

        setPriority(priority);
    }

    public void setPriority(int priority) {
        if (priority < 1) {
            throw new IllegalArgumentException(
                    "priority must be greater than zero"
            );
        }

        this.priority = priority;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void enableFailover() {
        this.failoverEnabled = true;
    }

    public void disableFailover() {
        this.failoverEnabled = false;
    }

    public UUID id() {
        return getId();
    }

    public NotificationChannel channel() {
        return channel;
    }

    public UUID connectorId() {
        return connectorId;
    }

    public int priority() {
        return priority;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean failoverEnabled() {
        return failoverEnabled;
    }
}
