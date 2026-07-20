package africa.growtogether.platform.eiam.user;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "eiam_user_account")
public class UserAccount extends AuditedTenantEntity {
    @Column(name = "username", nullable = false, length = 100)
    private String username;
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private UserAccountStatus accountStatus = UserAccountStatus.PENDING;
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;
    @Column(name = "locked_until")
    private Instant lockedUntil;
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    protected UserAccount() {}

    public UserAccount(String username, String email, String displayName, String passwordHash) {
        this.username = normalizeUsername(username);
        this.email = normalizeEmail(email);
        this.displayName = displayName.trim();
        this.passwordHash = passwordHash;
    }

    public void updateProfile(String username, String email, String displayName) {
        ensureNotDeactivated(UserLifecycleAction.UPDATE, "A deactivated account cannot be updated.");
        this.username = normalizeUsername(username);
        this.email = normalizeEmail(email);
        this.displayName = displayName.trim();
    }

    public void activate() {
        if (accountStatus == UserAccountStatus.ACTIVE) return;
        if (accountStatus == UserAccountStatus.DEACTIVATED) {
            throw invalidTransition(UserLifecycleAction.ACTIVATE, "A deactivated account cannot be reactivated.");
        }
        if (accountStatus == UserAccountStatus.LOCKED) {
            throw invalidTransition(UserLifecycleAction.ACTIVATE, "A locked account must be unlocked through the security workflow.");
        }
        accountStatus = UserAccountStatus.ACTIVE;
    }

    public void suspend() {
        if (accountStatus == UserAccountStatus.SUSPENDED) return;
        if (accountStatus != UserAccountStatus.ACTIVE) {
            throw invalidTransition(UserLifecycleAction.SUSPEND, "Only an active account can be suspended.");
        }
        accountStatus = UserAccountStatus.SUSPENDED;
    }

    public void deactivate() {
        if (accountStatus == UserAccountStatus.DEACTIVATED) return;
        accountStatus = UserAccountStatus.DEACTIVATED;
    }

    public boolean canAuthenticateAt(Instant now) {
        if (accountStatus != UserAccountStatus.ACTIVE) return false;
        if (lockedUntil != null && lockedUntil.isAfter(now)) return false;
        if (lockedUntil != null && !lockedUntil.isAfter(now)) { lockedUntil = null; failedLoginAttempts = 0; }
        return true;
    }
    public void recordFailedLogin(Instant now, int maxAttempts, long lockSeconds) {
        failedLoginAttempts++;
        if (failedLoginAttempts >= maxAttempts) { lockedUntil = now.plusSeconds(lockSeconds); failedLoginAttempts = 0; }
    }
    public void recordSuccessfulLogin(Instant now) { failedLoginAttempts = 0; lockedUntil = null; lastLoginAt = now; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public Instant getLockedUntil() { return lockedUntil; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void changePasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void clearLoginSecurity() { failedLoginAttempts = 0; lockedUntil = null; }
    public void verifyEmail(Instant now) { if (emailVerifiedAt == null) emailVerifiedAt = now; }
    public boolean isEmailVerified() { return emailVerifiedAt != null; }
    public Instant getEmailVerifiedAt() { return emailVerifiedAt; }
    public void recoverAccount() { clearLoginSecurity(); if (accountStatus == UserAccountStatus.LOCKED || accountStatus == UserAccountStatus.SUSPENDED) accountStatus = UserAccountStatus.ACTIVE; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
    public UserAccountStatus getAccountStatus() { return accountStatus; }

    private void ensureNotDeactivated(UserLifecycleAction action, String message) {
        if (accountStatus == UserAccountStatus.DEACTIVATED) throw invalidTransition(action, message);
    }

    private UserLifecycleException invalidTransition(UserLifecycleAction action, String message) {
        return new UserLifecycleException(accountStatus, action, message);
    }

    private static String normalizeUsername(String value) { return value.trim().toLowerCase(); }
    private static String normalizeEmail(String value) { return value.trim().toLowerCase(); }
}
