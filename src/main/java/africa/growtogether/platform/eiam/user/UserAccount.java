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
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "primary_phone_number", length = 32)
    private String primaryPhoneNumber;

    @Column(name = "phone_verified_at")
    private Instant phoneVerifiedAt;

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
        this(
                username,
                email,
                null,
                displayName,
                passwordHash
        );
    }

    public UserAccount(
            String username,
            String email,
            String primaryPhoneNumber,
            String displayName,
            String passwordHash
    ) {
        this.username = normalizeUsername(username);
        this.email = normalizeOptionalEmail(email);
        this.primaryPhoneNumber = normalizeOptionalPhone(primaryPhoneNumber);
        requireContactIdentity(this.email, this.primaryPhoneNumber);
        this.displayName = requireText(displayName, "displayName");
        this.passwordHash = requireText(passwordHash, "passwordHash");
    }

    public void updateProfile(String username, String email, String displayName) {
        updateProfile(
                username,
                email,
                this.primaryPhoneNumber,
                displayName
        );
    }

    public void updateProfile(
            String username,
            String email,
            String primaryPhoneNumber,
            String displayName
    ) {
        ensureNotDeactivated(
                UserLifecycleAction.UPDATE,
                "A deactivated account cannot be updated."
        );

        String normalizedEmail =
                normalizeOptionalEmail(email);

        String normalizedPhone =
                normalizeOptionalPhone(primaryPhoneNumber);

        requireContactIdentity(
                normalizedEmail,
                normalizedPhone
        );

        if (!java.util.Objects.equals(
                this.primaryPhoneNumber,
                normalizedPhone
        )) {
            this.phoneVerifiedAt = null;
        }

        this.username = normalizeUsername(username);
        this.email = normalizedEmail;
        this.primaryPhoneNumber = normalizedPhone;
        this.displayName = requireText(displayName, "displayName");
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
    public void verifyEmail(Instant now) {
        if (email == null) {
            throw new IllegalStateException(
                    "An account without an email address cannot verify email."
            );
        }

        if (emailVerifiedAt == null) {
            emailVerifiedAt = requireInstant(now, "now");
        }
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void verifyPhone(Instant now) {
        if (primaryPhoneNumber == null) {
            throw new IllegalStateException(
                    "An account without a phone number cannot verify phone."
            );
        }

        if (phoneVerifiedAt == null) {
            phoneVerifiedAt = requireInstant(now, "now");
        }
    }

    public boolean isPhoneVerified() {
        return phoneVerifiedAt != null;
    }

    public Instant getPhoneVerifiedAt() {
        return phoneVerifiedAt;
    }
    public void recoverAccount() { clearLoginSecurity(); if (accountStatus == UserAccountStatus.LOCKED || accountStatus == UserAccountStatus.SUSPENDED) accountStatus = UserAccountStatus.ACTIVE; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPrimaryPhoneNumber() { return primaryPhoneNumber; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
    public UserAccountStatus getAccountStatus() { return accountStatus; }

    private void ensureNotDeactivated(UserLifecycleAction action, String message) {
        if (accountStatus == UserAccountStatus.DEACTIVATED) throw invalidTransition(action, message);
    }

    private UserLifecycleException invalidTransition(UserLifecycleAction action, String message) {
        return new UserLifecycleException(accountStatus, action, message);
    }

    private static String normalizeUsername(String value) {
        return requireText(value, "username")
                .toLowerCase(java.util.Locale.ROOT);
    }

    private static String normalizeOptionalEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim()
                .toLowerCase(java.util.Locale.ROOT);
    }

    private static String normalizeOptionalPhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String phone = value.trim();

        if (!phone.matches("^\\+[1-9][0-9]{5,14}$")) {
            throw new IllegalArgumentException(
                    "primaryPhoneNumber must use canonical international format."
            );
        }

        return phone;
    }

    private static void requireContactIdentity(
            String email,
            String phone
    ) {
        if (email == null && phone == null) {
            throw new IllegalArgumentException(
                    "At least one contact identity is required."
            );
        }
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required."
            );
        }

        return value.trim();
    }

    private static Instant requireInstant(
            Instant value,
            String field
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    field + " is required."
            );
        }

        return value;
    }
}
