package africa.growtogether.platform.eiam.mfa;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="eiam_mfa_profile",uniqueConstraints=@UniqueConstraint(name="uq_mfa_profile_tenant_user",columnNames={"tenant_id","user_id"}))
public class MfaProfile extends AuditedTenantEntity {
 @Column(name="user_id",nullable=false) private UUID userId; @Column(name="encrypted_totp_secret",nullable=false,length=500) private String encryptedTotpSecret; @Column(name="enabled",nullable=false) private boolean enabled; @Column(name="enrolled_at") private Instant enrolledAt; @Column(name="last_verified_at") private Instant lastVerifiedAt;
 protected MfaProfile(){} public MfaProfile(UUID userId,String secret){this.userId=userId;this.encryptedTotpSecret=secret;}
 public void enable(Instant now){enabled=true;enrolledAt=now;lastVerifiedAt=now;} public void disable(){enabled=false;encryptedTotpSecret="DISABLED";} public void verified(Instant now){lastVerifiedAt=now;}
 public UUID getUserId(){return userId;} public String getEncryptedTotpSecret(){return encryptedTotpSecret;} public boolean isEnabled(){return enabled;} public Instant getEnrolledAt(){return enrolledAt;} public Instant getLastVerifiedAt(){return lastVerifiedAt;}
}
