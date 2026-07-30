package africa.growtogether.platform.school.parent.consent;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "parent_engagement_consent_preferences")
public class ParentEngagementConsentPreference
        extends AuditedTenantEntity {


    @Column(name = "parent_id",
            nullable = false)
    private UUID parentId;


    @Enumerated(EnumType.STRING)
    @Column(name = "channel",
            nullable = false,
            length = 40)
    private ParentEngagementConsentChannel channel;


    @Enumerated(EnumType.STRING)
    @Column(name = "consent_status",
            nullable = false,
            length = 30)
    private ParentEngagementConsentStatus status;


    @Column(name = "consent_timestamp")
    private Instant consentTimestamp;


    protected ParentEngagementConsentPreference() {
    }


    public ParentEngagementConsentPreference(
            UUID tenantId,
            UUID parentId,
            ParentEngagementConsentChannel channel
    ) {

        setTenantId(tenantId);

        this.parentId = parentId;
        this.channel = channel;
        this.status = ParentEngagementConsentStatus.PENDING;
    }


    public void grantConsent() {

        this.status = ParentEngagementConsentStatus.GRANTED;
        this.consentTimestamp = Instant.now();
    }


    public void revokeConsent() {

        this.status = ParentEngagementConsentStatus.REVOKED;
        this.consentTimestamp = Instant.now();
    }


    public boolean isGranted() {

        return status ==
                ParentEngagementConsentStatus.GRANTED;
    }


    public UUID getParentId() {

        return parentId;
    }


    public ParentEngagementConsentChannel getChannel() {

        return channel;
    }


    public ParentEngagementConsentStatus getConsentStatus() {

        return status;
    }
}
