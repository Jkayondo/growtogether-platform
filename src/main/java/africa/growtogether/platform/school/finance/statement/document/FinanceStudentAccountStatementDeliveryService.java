package africa.growtogether.platform.school.finance.statement.document;

import static africa.growtogether.platform.school.finance.statement.document.FinanceStudentAccountStatementDocumentDtos.StatementDeliveryRequest;
import static africa.growtogether.platform.school.finance.statement.document.FinanceStudentAccountStatementDocumentDtos.StatementDeliveryResponse;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import africa.growtogether.platform.eds.DocumentReference;
import africa.growtogether.platform.eds.DocumentReferenceService;

import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationDtos.SendCommand;
import africa.growtogether.platform.ens.NotificationDtos.View;
import africa.growtogether.platform.ens.NotificationService;

import africa.growtogether.platform.school.guardian.Guardian;
import africa.growtogether.platform.school.guardian.GuardianRepository;

import africa.growtogether.platform.school.relationship.StudentGuardianRelationship;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationshipRepository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceStudentAccountStatementDeliveryService {

    static final String DEFINITION_CODE =
            "GT_SCHOOL_FINANCIAL_STATEMENT";

    static final String SOURCE_SERVICE =
            "gt-school-finance";

    static final String DELIVERY_ACCEPTED =
            "ENS_REQUEST_ACCEPTED";

    private final EnterpriseIdentityContext identity;
    private final DocumentReferenceService references;
    private final StudentGuardianRelationshipRepository relationships;
    private final GuardianRepository guardians;
    private final NotificationService notifications;

    public FinanceStudentAccountStatementDeliveryService(
            EnterpriseIdentityContext identity,
            DocumentReferenceService references,
            StudentGuardianRelationshipRepository relationships,
            GuardianRepository guardians,
            NotificationService notifications
    ) {
        this.identity = identity;
        this.references = references;
        this.relationships = relationships;
        this.guardians = guardians;
        this.notifications = notifications;
    }

    /**
     * Queues a guardian notification for an already-generated,
     * governed GT financial statement.
     *
     * The API never supplies an email address, phone number or
     * arbitrary destination. The recipient is derived from the
     * verified tenant-scoped guardian relationship and Guardian
     * record.
     *
     * This method queues an ENS request only. It does not claim
     * provider SENT or DELIVERED state and it does not invoke an
     * external provider directly.
     */
    @Transactional
    public StatementDeliveryResponse deliver(
            UUID tenantId,
            UUID studentId,
            UUID statementReferenceId,
            StatementDeliveryRequest request
    ) {

        requireTenant(
                tenantId
        );

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }

        if (statementReferenceId == null) {
            throw new IllegalArgumentException(
                    "statementReferenceId must not be null"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "delivery request must not be null"
            );
        }

        UUID guardianId =
                request.guardianId();

        NotificationChannel channel =
                request.channel();

        String recipient =
                resolveAuthorizedRecipient(
                        tenantId,
                        studentId,
                        guardianId,
                        channel
                );

        /*
         * Validate the requested immutable statement reference only
         * after the guardian has passed relationship/communication
         * authorization. This avoids exposing document existence to
         * an unauthorized guardian selection.
         */
        requireStatementBelongsToStudent(
                studentId,
                statementReferenceId
        );

        String sourceReference =
                "financial-statement:"
                + statementReferenceId;

        View notification =
                notifications.sendForTenant(
                        tenantId,
                        new SendCommand(
                                DEFINITION_CODE,
                                recipient,
                                channel,
                                null,
                                "GT School learner financial statement",
                                body(
                                        studentId,
                                        statementReferenceId
                                ),
                                SOURCE_SERVICE,
                                sourceReference
                        )
                );

        validateEnsResult(
                notification,
                tenantId,
                recipient,
                channel,
                sourceReference
        );

        return new StatementDeliveryResponse(
                statementReferenceId,
                studentId,
                guardianId,
                notification.id(),
                channel,
                sourceReference,
                DELIVERY_ACCEPTED
        );
    }

    private String resolveAuthorizedRecipient(
            UUID tenantId,
            UUID studentId,
            UUID guardianId,
            NotificationChannel channel
    ) {

        if (guardianId == null) {
            throw new IllegalArgumentException(
                    "guardianId must not be null"
            );
        }

        if (
                channel != NotificationChannel.EMAIL
                && channel != NotificationChannel.SMS
        ) {
            throw new IllegalArgumentException(
                    "Statement delivery supports EMAIL or SMS only"
            );
        }

        StudentGuardianRelationship relationship =
                relationships
                        .findByTenantIdAndStudentId(
                                tenantId,
                                studentId
                        )
                        .stream()
                        .filter(
                                candidate ->
                                        guardianId.equals(
                                                candidate.getGuardianId()
                                        )
                        )
                        .filter(
                                candidate ->
                                        candidate.getStatus()
                                                == EntityStatus.ACTIVE
                        )
                        .filter(
                                candidate ->
                                        "ACTIVE".equals(
                                                normalized(
                                                        candidate
                                                                .getRelationshipStatus()
                                                )
                                        )
                        )
                        .filter(
                                StudentGuardianRelationship
                                        ::isReceivesCommunications
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new SecurityException(
                                                "Guardian is not authorised "
                                                + "to receive communications "
                                                + "for this student"
                                        )
                        );

        if (
                !studentId.equals(
                        relationship.getStudentId()
                )
        ) {
            throw new SecurityException(
                    "Guardian relationship belongs to "
                    + "a different student"
            );
        }

        Guardian guardian =
                guardians
                        .findByTenantIdAndId(
                                tenantId,
                                guardianId
                        )
                        .filter(
                                candidate ->
                                        candidate.getStatus()
                                                == EntityStatus.ACTIVE
                        )
                        .filter(
                                candidate ->
                                        "ACTIVE".equals(
                                                normalized(
                                                        candidate
                                                                .getGuardianStatus()
                                                )
                                        )
                        )
                        .filter(
                                candidate ->
                                        "VERIFIED".equals(
                                                normalized(
                                                        candidate
                                                                .getVerificationStatus()
                                                )
                                        )
                        )
                        .orElseThrow(
                                () ->
                                        new SecurityException(
                                                "Guardian is not active "
                                                + "and verified"
                                        )
                        );

        return switch (channel) {

            case EMAIL ->
                    requiredRecipient(
                            guardian.getEmail(),
                            "Verified guardian email is unavailable"
                    );

            case SMS ->
                    requiredRecipient(
                            guardian.getPrimaryPhoneNumber(),
                            "Verified guardian phone number is unavailable"
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Statement delivery supports EMAIL or SMS only"
                    );
        };
    }

    private void requireStatementBelongsToStudent(
            UUID studentId,
            UUID statementReferenceId
    ) {

        List<DocumentReference> statementLinks =
                references.findByReference(
                        FinanceStudentAccountStatementDocumentService
                                .REFERENCE_TYPE,
                        statementReferenceId
                );

        if (statementLinks.isEmpty()) {
            throw new IllegalArgumentException(
                    "Statement document was not found"
            );
        }

        if (statementLinks.size() > 1) {
            throw new IllegalStateException(
                    "Multiple EDS documents are linked "
                    + "to the same financial statement snapshot"
            );
        }

        UUID documentId =
                statementLinks.get(0)
                        .getDocumentId();

        if (documentId == null) {
            throw new IllegalStateException(
                    "Statement reference has no document identity"
            );
        }

        List<DocumentReference> studentLinks =
                references.findByReference(
                        FinanceStudentAccountStatementDocumentService
                                .STUDENT_REFERENCE_TYPE,
                        studentId
                );

        boolean belongsToStudent =
                studentLinks.stream()
                        .map(
                                DocumentReference::getDocumentId
                        )
                        .anyMatch(
                                documentId::equals
                        );

        if (!belongsToStudent) {
            throw new SecurityException(
                    "Statement document does not belong "
                    + "to the requested student"
            );
        }
    }

    private static void validateEnsResult(
            View notification,
            UUID tenantId,
            String recipient,
            NotificationChannel channel,
            String sourceReference
    ) {

        if (notification == null) {
            throw new IllegalStateException(
                    "ENS did not return a notification request"
            );
        }

        if (notification.id() == null) {
            throw new IllegalStateException(
                    "ENS notification identity is missing"
            );
        }

        if (
                notification.tenantId() == null
                || !tenantId.equals(
                        notification.tenantId()
                )
        ) {
            throw new SecurityException(
                    "ENS notification tenant mismatch"
            );
        }

        if (
                notification.recipient() == null
                || !recipient.equals(
                        notification.recipient()
                )
        ) {
            throw new SecurityException(
                    "ENS notification recipient mismatch"
            );
        }

        if (
                notification.channel() == null
                || channel != notification.channel()
        ) {
            throw new SecurityException(
                    "ENS notification channel mismatch"
            );
        }

        if (
                notification.sourceReference() == null
                || !sourceReference.equals(
                        notification.sourceReference()
                )
        ) {
            throw new SecurityException(
                    "ENS notification source reference mismatch"
            );
        }
    }

    static String body(
            UUID studentId,
            UUID statementReferenceId
    ) {

        return """
                GT School learner financial statement

                A financial statement for learner %s is available
                securely in GT School.

                Statement reference: %s

                Please sign in to GT School using your authorised
                guardian access to view or download the statement.

                For your security, this notification does not contain
                the statement file, storage location or financial
                account data.
                """
                .formatted(
                        studentId,
                        statementReferenceId
                );
    }

    private void requireTenant(
            UUID tenantId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "X-Tenant-ID is required"
            );
        }

        identity.requireTenant(
                tenantId
        );
    }

    private static String requiredRecipient(
            String value,
            String message
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalStateException(
                    message
            );
        }

        return value.trim();
    }

    private static String normalized(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }
}
