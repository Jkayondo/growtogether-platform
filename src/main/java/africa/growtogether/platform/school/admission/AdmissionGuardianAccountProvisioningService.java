package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.eiam.membership.AcceptInvitationCommand;
import africa.growtogether.platform.eiam.membership.CreateRoleCodeInvitationCommand;
import africa.growtogether.platform.eiam.membership.InvitationAcceptanceEvidence;
import africa.growtogether.platform.eiam.membership.InvitationView;
import africa.growtogether.platform.eiam.membership.MembershipService;

import africa.growtogether.platform.school.admission.payment.AdmissionOnboardingGateDecision;
import africa.growtogether.platform.school.admission.payment.AdmissionOnboardingGateService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AdmissionGuardianAccountProvisioningService {

    private static final String PARENT_ROLE_CODE =
            "PARENT";

    private static final Pattern CANONICAL_PHONE =
            Pattern.compile(
                    "^\\+[1-9][0-9]{5,14}$"
            );

    /*
     * This deliberately provides only a conservative admission-stage
     * sanity check. EIAM remains the authoritative identity lifecycle.
     */
    private static final Pattern BASIC_EMAIL =
            Pattern.compile(
                    "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
            );

    private final AdmissionGuardianRepository guardians;

    private final AdmissionGuardianAccountProvisioningRepository
            provisionings;

    private final AdmissionOnboardingGateService onboardingGate;

    private final MembershipService memberships;

    public AdmissionGuardianAccountProvisioningService(
            AdmissionGuardianRepository guardians,
            AdmissionGuardianAccountProvisioningRepository provisionings,
            AdmissionOnboardingGateService onboardingGate,
            MembershipService memberships
    ) {

        this.guardians =
                guardians;

        this.provisionings =
                provisionings;

        this.onboardingGate =
                onboardingGate;

        this.memberships =
                memberships;
    }

    /*
     * Admission-stage parent identity provisioning only.
     *
     * IMPORTANT:
     * This method does NOT create the permanent Guardian record.
     * Admission -> Guardian conversion belongs to A12.10.
     *
     * The EIAM acceptance token is returned only transiently so that
     * B8 can deliver it through ENS. It is never persisted in the
     * admission provisioning evidence record.
     */
    @Transactional
    public AdmissionGuardianAccountProvisioningResult provisionParentAccount(
            UUID admissionGuardianId
    ) {

        if (admissionGuardianId == null) {
            throw new IllegalArgumentException(
                    "admissionGuardianId must not be null"
            );
        }

        UUID tenantId =
                activeTenant();

        AdmissionGuardian guardian =
                guardians
                        .findByTenantIdAndId(
                                tenantId,
                                admissionGuardianId
                        )
                        .orElseThrow(
                                () ->
                                        new AdmissionGuardianAccountProvisioningException(
                                                "Admission guardian was not found for the active tenant"
                                        )
                        );

        if (
                guardian.getStatus()
                        != EntityStatus.ACTIVE
        ) {
            throw new AdmissionGuardianAccountProvisioningException(
                    "Only an active admission guardian can receive a parent account"
            );
        }

        /*
         * Fail before creating another EIAM invitation.
         *
         * V156 also enforces this invariant in PostgreSQL. The
         * application check gives a clear error while the DB unique
         * constraint protects concurrent requests.
         */
        if (
                provisionings
                        .existsByTenantIdAndAdmissionGuardianId(
                                tenantId,
                                admissionGuardianId
                        )
        ) {
            throw new AdmissionGuardianAccountProvisioningException(
                    "Parent account provisioning already exists for this admission guardian"
            );
        }

        AdmissionOnboardingGateDecision gate =
                onboardingGate.evaluate(
                        tenantId,
                        guardian.getAdmissionApplicationId()
                );

        if (!gate.unlocked()) {
            throw new AdmissionGuardianAccountProvisioningException(
                    "Parent account provisioning is blocked by outstanding admission onboarding obligations"
            );
        }

        SelectedIdentity identity =
                selectIdentity(
                        guardian
                );

        String email =
                identity.type()
                                == AdmissionGuardianContactIdentityType.EMAIL
                        ? identity.value()
                        : null;

        String phoneNumber =
                identity.type()
                                == AdmissionGuardianContactIdentityType.PHONE
                        ? identity.value()
                        : null;

        InvitationView invitation =
                memberships.createInvitationByRoleCodes(
                        new CreateRoleCodeInvitationCommand(
                                email,
                                phoneNumber,
                                Set.of(
                                        PARENT_ROLE_CODE
                                ),
                                null
                        )
                );

        validateInvitationEvidence(
                identity,
                invitation
        );

        AdmissionGuardianAccountProvisioning provisioning =
                AdmissionGuardianAccountProvisioning.secureInvitation(
                        admissionGuardianId,
                        identity.type(),
                        identity.value(),
                        invitation.id()
                );

        provisioning.setTenantId(
                tenantId
        );

        /*
         * saveAndFlush is intentional.
         *
         * The V156 integrity constraints are therefore checked before
         * the one-time acceptance token leaves this transaction.
         * Any failure rolls back both this evidence record and the EIAM
         * invitation created in the same transaction.
         */
        AdmissionGuardianAccountProvisioning saved =
                provisionings.saveAndFlush(
                        provisioning
                );

        return new AdmissionGuardianAccountProvisioningResult(
                saved.getId(),
                admissionGuardianId,
                invitation.id(),
                identity.type(),
                identity.value(),
                invitation.expiresAt(),
                invitation.acceptanceToken()
        );
    }

    /*
     * Completes admission-stage parent account activation.
     *
     * EIAM remains authoritative for validating and consuming the
     * one-time invitation token and for creating/reusing the user
     * account and tenant membership.
     *
     * V156 is activated only after the accepted EIAM invitation is
     * proven to be the exact invitation recorded for this admission
     * guardian provisioning.
     *
     * This does NOT create the permanent Guardian record.
     * That remains an A12.10 responsibility.
     */
    @Transactional
    public AdmissionGuardianAccountActivationResult
    completeParentAccountActivation(
            UUID provisioningId,
            AcceptInvitationCommand command
    ) {

        if (provisioningId == null) {
            throw new IllegalArgumentException(
                    "provisioningId must not be null"
            );
        }

        if (command == null) {
            throw new IllegalArgumentException(
                    "acceptance command must not be null"
            );
        }

        UUID tenantId =
                activeTenant();

        AdmissionGuardianAccountProvisioning provisioning =
                provisionings
                        .findByTenantIdAndId(
                                tenantId,
                                provisioningId
                        )
                        .orElseThrow(
                                () ->
                                        new AdmissionGuardianAccountProvisioningException(
                                                "Guardian account provisioning was not found for the active tenant"
                                        )
                        );

        if (provisioning.isCancelled()) {
            throw new AdmissionGuardianAccountProvisioningException(
                    "Cancelled guardian account provisioning cannot be completed"
            );
        }

        /*
         * A consumed EIAM token cannot safely be replayed merely to
         * prove the same user again. Completion is therefore a
         * one-time service operation even though the underlying domain
         * transition remains idempotent for the same EIAM user.
         */
        if (provisioning.isActivated()) {
            throw new AdmissionGuardianAccountProvisioningException(
                    "Guardian account provisioning is already activated"
            );
        }

        if (!provisioning.isPendingActivation()) {
            throw new AdmissionGuardianAccountProvisioningException(
                    "Guardian account provisioning is not pending activation"
            );
        }

        if (
                provisioning.getProvisioningMethod()
                        != AdmissionGuardianProvisioningMethod.SECURE_INVITATION
        ) {
            throw new IllegalStateException(
                    "Pending activation completion requires secure invitation provisioning"
            );
        }

        UUID expectedInvitationId =
                provisioning.getInvitationId();

        if (expectedInvitationId == null) {
            throw new IllegalStateException(
                    "Guardian account provisioning has no EIAM invitation evidence"
            );
        }

        InvitationAcceptanceEvidence evidence =
                memberships.acceptWithEvidence(
                        command
                );

        if (
                evidence == null
                || evidence.invitationId() == null
                || evidence.membership() == null
        ) {
            throw new IllegalStateException(
                    "EIAM did not return complete invitation acceptance evidence"
            );
        }

        /*
         * Critical correlation boundary.
         *
         * A valid token for another invitation must never activate this
         * admission guardian provisioning. A mismatch throws inside the
         * enclosing transaction so the EIAM acceptance is rolled back
         * together with this school operation.
         */
        if (
                !expectedInvitationId.equals(
                        evidence.invitationId()
                )
        ) {
            throw new AdmissionGuardianAccountProvisioningException(
                    "Accepted EIAM invitation does not belong to this guardian account provisioning"
            );
        }

        UUID eiamUserId =
                evidence.membership()
                        .userId();

        UUID membershipId =
                evidence.membership()
                        .id();

        if (eiamUserId == null) {
            throw new IllegalStateException(
                    "EIAM acceptance evidence has no resulting user identifier"
            );
        }

        if (membershipId == null) {
            throw new IllegalStateException(
                    "EIAM acceptance evidence has no resulting membership identifier"
            );
        }

        provisioning.activate(
                eiamUserId,
                Instant.now()
        );

        AdmissionGuardianAccountProvisioning saved =
                provisionings.saveAndFlush(
                        provisioning
                );

        return new AdmissionGuardianAccountActivationResult(
                saved.getId(),
                saved.getAdmissionGuardianId(),
                saved.getInvitationId(),
                membershipId,
                saved.getEiamUserId(),
                saved.getProvisioningStatus(),
                saved.getActivatedAt()
        );
    }


    private static SelectedIdentity selectIdentity(
            AdmissionGuardian guardian
    ) {

        String phone =
                trimToNull(
                        guardian.getPhoneNumber()
                );

        if (
                phone != null
                && CANONICAL_PHONE
                        .matcher(
                                phone
                        )
                        .matches()
        ) {
            return new SelectedIdentity(
                    AdmissionGuardianContactIdentityType.PHONE,
                    phone
            );
        }

        String email =
                trimToNull(
                        guardian.getEmail()
                );

        if (email != null) {

            String normalizedEmail =
                    email.toLowerCase(
                            Locale.ROOT
                    );

            if (
                    normalizedEmail.length() <= 255
                    && BASIC_EMAIL
                            .matcher(
                                    normalizedEmail
                            )
                            .matches()
            ) {
                return new SelectedIdentity(
                        AdmissionGuardianContactIdentityType.EMAIL,
                        normalizedEmail
                );
            }
        }

        /*
         * IMPROVEMENT:
         * Do not guess a country code from a local-format phone number.
         * Shared phone localization belongs to the later country-aware
         * normalization capability.
         */
        throw new AdmissionGuardianAccountProvisioningException(
                "Admission guardian requires a canonical international phone number or valid email before parent account provisioning"
        );
    }

    private static void validateInvitationEvidence(
            SelectedIdentity identity,
            InvitationView invitation
    ) {

        if (
                invitation == null
                || invitation.id() == null
        ) {
            throw new IllegalStateException(
                    "EIAM did not return durable invitation evidence"
            );
        }

        if (
                invitation.acceptanceToken() == null
                || invitation.acceptanceToken().isBlank()
        ) {
            throw new IllegalStateException(
                    "EIAM did not return the one-time invitation acceptance token"
            );
        }

        String returnedIdentity =
                identity.type()
                                == AdmissionGuardianContactIdentityType.EMAIL
                        ? invitation.email()
                        : invitation.phoneNumber();

        if (
                !identity.value().equals(
                        returnedIdentity
                )
        ) {
            throw new IllegalStateException(
                    "EIAM invitation identity does not match the selected admission guardian identity"
            );
        }
    }

    private static String trimToNull(
            String value
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }

    private static UUID activeTenant() {

        return RequestContextHolder
                .current()
                .map(
                        context ->
                                context.tenantId()
                )
                .filter(
                        value ->
                                value != null
                                && !value.isBlank()
                )
                .map(
                        UUID::fromString
                )
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "An active tenant is required."
                                )
                );
    }

    private record SelectedIdentity(
            AdmissionGuardianContactIdentityType type,
            String value
    ) {
    }
}
