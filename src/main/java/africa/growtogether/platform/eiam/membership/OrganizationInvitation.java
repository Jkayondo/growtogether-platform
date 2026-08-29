package africa.growtogether.platform.eiam.membership;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "eiam_organization_invitation")
public class OrganizationInvitation extends AuditedTenantEntity {
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", nullable = false, length = 20)
    private InvitationStatus invitationStatus = InvitationStatus.PENDING;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "accepted_at")
    private Instant acceptedAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;
    @Column(name = "invited_by_user_id")
    private UUID invitedByUserId;

    protected OrganizationInvitation() {}

    public OrganizationInvitation(
            String email,
            String tokenHash,
            Instant expiresAt,
            UUID invitedByUserId
    ) {
        this(
                email,
                null,
                tokenHash,
                expiresAt,
                invitedByUserId
        );
    }

    public OrganizationInvitation(
            String email,
            String phoneNumber,
            String tokenHash,
            Instant expiresAt,
            UUID invitedByUserId
    ) {
        this.email = normalizeOptionalEmail(email);
        this.phoneNumber = normalizeOptionalPhone(phoneNumber);

        requireContactIdentity(
                this.email,
                this.phoneNumber
        );

        this.tokenHash = required(tokenHash, "tokenHash");
        this.expiresAt = required(expiresAt, "expiresAt");
        this.invitedByUserId = invitedByUserId;
    }

    public void assertAcceptable(Instant now) {
        if (invitationStatus == InvitationStatus.ACCEPTED) {
            throw new MembershipException("Invitation has already been accepted.");
        }
        if (invitationStatus == InvitationStatus.REVOKED) {
            throw new MembershipException("Invitation has been revoked.");
        }
        if (!expiresAt.isAfter(now)) {
            invitationStatus = InvitationStatus.EXPIRED;
            throw new MembershipException("Invitation has expired.");
        }
    }

    public void accept(Instant now) {
        assertAcceptable(now);
        invitationStatus = InvitationStatus.ACCEPTED;
        acceptedAt = now;
    }

    public void revoke(Instant now) {
        if (invitationStatus == InvitationStatus.ACCEPTED) {
            throw new MembershipException("An accepted invitation cannot be revoked.");
        }
        if (invitationStatus == InvitationStatus.REVOKED) return;
        invitationStatus = InvitationStatus.REVOKED;
        revokedAt = now;
    }

    public void replaceToken(String tokenHash, Instant expiresAt) {
        if (invitationStatus != InvitationStatus.PENDING && invitationStatus != InvitationStatus.EXPIRED) {
            throw new MembershipException("Only pending or expired invitations can be resent.");
        }
        this.tokenHash = required(tokenHash, "tokenHash");
        this.expiresAt = required(expiresAt, "expiresAt");
        this.invitationStatus = InvitationStatus.PENDING;
        this.acceptedAt = null;
        this.revokedAt = null;
    }

    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public boolean isEmailTarget() { return email != null; }
    public boolean isPhoneTarget() { return phoneNumber != null; }
    public String getTokenHash() { return tokenHash; }
    public InvitationStatus getInvitationStatus() { return invitationStatus; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public UUID getInvitedByUserId() { return invitedByUserId; }

    private static String normalizeOptionalEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeOptionalPhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String phone = value.trim();

        if (!phone.matches("^\\+[1-9][0-9]{5,14}$")) {
            throw new IllegalArgumentException(
                    "phoneNumber must use canonical international format."
            );
        }

        return phone;
    }

    private static void requireContactIdentity(
            String email,
            String phoneNumber
    ) {
        if ((email == null) == (phoneNumber == null)) {
            throw new IllegalArgumentException(
                    "Exactly one invitation contact identity is required."
            );
        }
    }

    private static <T> T required(T value, String name) {
        if (value == null || (value instanceof String text && text.isBlank())) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value;
    }
}
