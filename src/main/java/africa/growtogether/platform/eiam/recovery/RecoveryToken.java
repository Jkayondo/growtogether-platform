package africa.growtogether.platform.eiam.recovery;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eiam_recovery_token")
public class RecoveryToken extends AuditedTenantEntity {
    @Column(name="user_id", nullable=false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(name="purpose", nullable=false, length=32) private RecoveryTokenPurpose purpose;
    @Column(name="token_hash", nullable=false, unique=true, length=64) private String tokenHash;
    @Column(name="expires_at", nullable=false) private Instant expiresAt;
    @Column(name="consumed_at") private Instant consumedAt;
    @Column(name="invalidated_at") private Instant invalidatedAt;
    protected RecoveryToken() {}
    public RecoveryToken(UUID userId, RecoveryTokenPurpose purpose, String tokenHash, Instant expiresAt) {
        this.userId=userId; this.purpose=purpose; this.tokenHash=tokenHash; this.expiresAt=expiresAt;
    }
    public boolean usableAt(Instant now) { return consumedAt==null && invalidatedAt==null && expiresAt.isAfter(now); }
    public void consume(Instant now) { if(!usableAt(now)) throw new InvalidRecoveryTokenException(); consumedAt=now; }
    public void invalidate(Instant now) { if(consumedAt==null && invalidatedAt==null) invalidatedAt=now; }
    public UUID getUserId(){return userId;} public RecoveryTokenPurpose getPurpose(){return purpose;} public String getTokenHash(){return tokenHash;}
}
