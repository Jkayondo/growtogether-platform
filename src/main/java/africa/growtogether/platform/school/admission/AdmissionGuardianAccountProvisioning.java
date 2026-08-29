package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "gts_admission_guardian_account_provisioning"
)
public class AdmissionGuardianAccountProvisioning
        extends AuditedTenantEntity {

    @Column(
            name = "admission_guardian_id",
            nullable = false
    )
    private UUID admissionGuardianId;

    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "provisioning_method",
            nullable = false,
            length = 40
    )
    private AdmissionGuardianProvisioningMethod provisioningMethod;

    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "contact_identity_type",
            length = 20
    )
    private AdmissionGuardianContactIdentityType contactIdentityType;

    @Column(
            name = "contact_identity",
            length = 255
    )
    private String contactIdentity;

    @Column(
            name = "invitation_id"
    )
    private UUID invitationId;

    @Column(
            name = "eiam_user_id"
    )
    private UUID eiamUserId;

    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "provisioning_status",
            nullable = false,
            length = 30
    )
    private AdmissionGuardianProvisioningStatus provisioningStatus;

    @Column(
            name = "activated_at"
    )
    private Instant activatedAt;

    @Column(
            name = "cancelled_at"
    )
    private Instant cancelledAt;

    protected AdmissionGuardianAccountProvisioning() {
    }

    private AdmissionGuardianAccountProvisioning(
            UUID admissionGuardianId,
            AdmissionGuardianProvisioningMethod provisioningMethod,
            AdmissionGuardianContactIdentityType contactIdentityType,
            String contactIdentity,
            UUID invitationId,
            UUID eiamUserId,
            AdmissionGuardianProvisioningStatus provisioningStatus,
            Instant activatedAt
    ) {

        this.admissionGuardianId =
                requireUuid(
                        admissionGuardianId,
                        "admissionGuardianId"
                );

        this.provisioningMethod =
                Objects.requireNonNull(
                        provisioningMethod,
                        "provisioningMethod must not be null"
                );

        this.contactIdentityType =
                contactIdentityType;

        this.contactIdentity =
                contactIdentity;

        this.invitationId =
                invitationId;

        this.eiamUserId =
                eiamUserId;

        this.provisioningStatus =
                Objects.requireNonNull(
                        provisioningStatus,
                        "provisioningStatus must not be null"
                );

        this.activatedAt =
                activatedAt;

        validateState();
    }

    public static AdmissionGuardianAccountProvisioning secureInvitation(
            UUID admissionGuardianId,
            AdmissionGuardianContactIdentityType contactIdentityType,
            String contactIdentity,
            UUID invitationId
    ) {

        AdmissionGuardianContactIdentityType identityType =
                Objects.requireNonNull(
                        contactIdentityType,
                        "contactIdentityType must not be null"
                );

        String normalizedIdentity =
                normalizeContactIdentity(
                        identityType,
                        contactIdentity
                );

        return new AdmissionGuardianAccountProvisioning(
                admissionGuardianId,
                AdmissionGuardianProvisioningMethod.SECURE_INVITATION,
                identityType,
                normalizedIdentity,
                requireUuid(
                        invitationId,
                        "invitationId"
                ),
                null,
                AdmissionGuardianProvisioningStatus.PENDING_ACTIVATION,
                null
        );
    }

    public static AdmissionGuardianAccountProvisioning existingIdentityReuse(
            UUID admissionGuardianId,
            UUID eiamUserId,
            Instant activatedAt
    ) {

        return new AdmissionGuardianAccountProvisioning(
                admissionGuardianId,
                AdmissionGuardianProvisioningMethod.EXISTING_IDENTITY_REUSE,
                null,
                null,
                null,
                requireUuid(
                        eiamUserId,
                        "eiamUserId"
                ),
                AdmissionGuardianProvisioningStatus.ACTIVATED,
                requireInstant(
                        activatedAt,
                        "activatedAt"
                )
        );
    }

    public void activate(
            UUID userId,
            Instant now
    ) {

        UUID targetUserId =
                requireUuid(
                        userId,
                        "userId"
                );

        Instant activationTime =
                requireInstant(
                        now,
                        "now"
                );

        if (
                provisioningStatus
                        == AdmissionGuardianProvisioningStatus.CANCELLED
        ) {
            throw new IllegalStateException(
                    "Cancelled guardian account provisioning cannot be activated"
            );
        }

        if (
                provisioningStatus
                        == AdmissionGuardianProvisioningStatus.ACTIVATED
        ) {

            if (
                    Objects.equals(
                            eiamUserId,
                            targetUserId
                    )
            ) {
                return;
            }

            throw new IllegalStateException(
                    "Guardian account provisioning is already activated for another EIAM user"
            );
        }

        if (
                provisioningMethod
                        != AdmissionGuardianProvisioningMethod.SECURE_INVITATION
        ) {
            throw new IllegalStateException(
                    "Only secure invitation provisioning may transition from pending activation"
            );
        }

        this.eiamUserId =
                targetUserId;

        this.provisioningStatus =
                AdmissionGuardianProvisioningStatus.ACTIVATED;

        this.activatedAt =
                activationTime;

        this.cancelledAt =
                null;

        validateState();
    }

    public void cancel(
            Instant now
    ) {

        Instant cancellationTime =
                requireInstant(
                        now,
                        "now"
                );

        if (
                provisioningStatus
                        == AdmissionGuardianProvisioningStatus.ACTIVATED
        ) {
            throw new IllegalStateException(
                    "Activated guardian account provisioning cannot be cancelled"
            );
        }

        if (
                provisioningStatus
                        == AdmissionGuardianProvisioningStatus.CANCELLED
        ) {
            return;
        }

        this.provisioningStatus =
                AdmissionGuardianProvisioningStatus.CANCELLED;

        this.cancelledAt =
                cancellationTime;

        validateState();
    }

    public boolean isPendingActivation() {

        return provisioningStatus
                == AdmissionGuardianProvisioningStatus.PENDING_ACTIVATION;
    }

    public boolean isActivated() {

        return provisioningStatus
                == AdmissionGuardianProvisioningStatus.ACTIVATED;
    }

    public boolean isCancelled() {

        return provisioningStatus
                == AdmissionGuardianProvisioningStatus.CANCELLED;
    }

    private void validateState() {

        if (
                provisioningMethod
                        == AdmissionGuardianProvisioningMethod.SECURE_INVITATION
        ) {

            if (
                    invitationId == null
                    || contactIdentityType == null
                    || contactIdentity == null
                    || contactIdentity.isBlank()
            ) {
                throw new IllegalStateException(
                        "Secure invitation provisioning requires invitation and contact identity evidence"
                );
            }
        }

        if (
                provisioningMethod
                        == AdmissionGuardianProvisioningMethod.EXISTING_IDENTITY_REUSE
        ) {

            if (
                    eiamUserId == null
                    || provisioningStatus
                            != AdmissionGuardianProvisioningStatus.ACTIVATED
                    || activatedAt == null
            ) {
                throw new IllegalStateException(
                        "Existing identity reuse must be activated with EIAM user evidence"
                );
            }
        }

        if (
                provisioningStatus
                        == AdmissionGuardianProvisioningStatus.PENDING_ACTIVATION
                && (
                        provisioningMethod
                                != AdmissionGuardianProvisioningMethod.SECURE_INVITATION
                        || invitationId == null
                )
        ) {
            throw new IllegalStateException(
                    "Pending activation requires secure invitation evidence"
            );
        }

        if (
                provisioningStatus
                        == AdmissionGuardianProvisioningStatus.ACTIVATED
                && (
                        eiamUserId == null
                        || activatedAt == null
                )
        ) {
            throw new IllegalStateException(
                    "Activated guardian account provisioning requires EIAM user and activation evidence"
            );
        }

        if (
                provisioningStatus
                        == AdmissionGuardianProvisioningStatus.CANCELLED
                && cancelledAt == null
        ) {
            throw new IllegalStateException(
                    "Cancelled guardian account provisioning requires cancellation evidence"
            );
        }
    }

    private static String normalizeContactIdentity(
            AdmissionGuardianContactIdentityType identityType,
            String value
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "contactIdentity must not be blank"
            );
        }

        String normalized =
                value.trim();

        if (
                identityType
                        == AdmissionGuardianContactIdentityType.EMAIL
        ) {

            return normalized.toLowerCase(
                    Locale.ROOT
            );
        }

        if (
                !normalized.matches(
                        "^\\+[1-9][0-9]{5,14}$"
                )
        ) {
            throw new IllegalArgumentException(
                    "PHONE contact identity must use canonical international format"
            );
        }

        return normalized;
    }

    private static UUID requireUuid(
            UUID value,
            String name
    ) {

        if (value == null) {
            throw new IllegalArgumentException(
                    name + " must not be null"
            );
        }

        return value;
    }

    private static Instant requireInstant(
            Instant value,
            String name
    ) {

        if (value == null) {
            throw new IllegalArgumentException(
                    name + " must not be null"
            );
        }

        return value;
    }

    public UUID getAdmissionGuardianId() {
        return admissionGuardianId;
    }

    public AdmissionGuardianProvisioningMethod getProvisioningMethod() {
        return provisioningMethod;
    }

    public AdmissionGuardianContactIdentityType getContactIdentityType() {
        return contactIdentityType;
    }

    public String getContactIdentity() {
        return contactIdentity;
    }

    public UUID getInvitationId() {
        return invitationId;
    }

    public UUID getEiamUserId() {
        return eiamUserId;
    }

    public AdmissionGuardianProvisioningStatus getProvisioningStatus() {
        return provisioningStatus;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }
}
