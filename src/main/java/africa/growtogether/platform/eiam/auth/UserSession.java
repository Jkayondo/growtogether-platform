package africa.growtogether.platform.eiam.auth;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="eiam_user_session")
public class UserSession extends AuditedTenantEntity {
 @Column(name="user_id",nullable=false) private UUID userId;
 @Column(name="refresh_token_hash",nullable=false,unique=true,length=64) private String refreshTokenHash;
 @Column(name="expires_at",nullable=false) private Instant expiresAt;
 @Column(name="last_used_at",nullable=false) private Instant lastUsedAt;
 @Column(name="revoked_at") private Instant revokedAt;
 @Column(name="revoke_reason",length=100) private String revokeReason;
 @Column(name="mfa_verified",nullable=false) private boolean mfaVerified;
 @Column(name="authentication_assurance",nullable=false,length=20) private String authenticationAssurance="AAL1";
 @Column(name="trusted_device",nullable=false) private boolean trustedDevice;
 protected UserSession(){}
 public UserSession(UUID userId,String hash,Instant expiresAt,Instant now){this.userId=userId;this.refreshTokenHash=hash;this.expiresAt=expiresAt;this.lastUsedAt=now;}
 public boolean usableAt(Instant now){return revokedAt==null && expiresAt.isAfter(now);}
 public void rotate(String hash,Instant expiresAt,Instant now){if(!usableAt(now)) throw new AuthenticationException("Refresh session is no longer valid."); this.refreshTokenHash=hash;this.expiresAt=expiresAt;this.lastUsedAt=now;}
 public void revoke(String reason,Instant now){if(revokedAt==null){revokedAt=now;revokeReason=reason;}}
 public void markMfaVerified(boolean trusted){this.mfaVerified=true;this.trustedDevice=trusted;this.authenticationAssurance="AAL2";}
 public boolean isMfaVerified(){return mfaVerified;} public String getAuthenticationAssurance(){return authenticationAssurance;} public boolean isTrustedDevice(){return trustedDevice;}
 public UUID getUserId(){return userId;} public String getRefreshTokenHash(){return refreshTokenHash;} public Instant getExpiresAt(){return expiresAt;} public Instant getRevokedAt(){return revokedAt;}
}
