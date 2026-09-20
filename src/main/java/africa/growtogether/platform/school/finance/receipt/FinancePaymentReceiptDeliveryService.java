package africa.growtogether.platform.school.finance.receipt;

import static africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.DeliveryRequest;
import static africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.DeliveryResponse;
import static africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.ReceiptResponse;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationDtos.SendCommand;
import africa.growtogether.platform.ens.NotificationService;
import africa.growtogether.platform.school.guardian.Guardian;
import africa.growtogether.platform.school.guardian.GuardianRepository;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationship;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationshipRepository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class FinancePaymentReceiptDeliveryService {

    static final String DEFINITION_CODE =
            "GT_SCHOOL_PAYMENT_RECEIPT";

    static final String SOURCE_SERVICE =
            "gt-school-finance";

    private final FinancePaymentReceiptService receipts;
    private final NotificationService notifications;
    private final StudentGuardianRelationshipRepository relationships;
    private final GuardianRepository guardians;

    public FinancePaymentReceiptDeliveryService(
            FinancePaymentReceiptService receipts,
            NotificationService notifications,
            StudentGuardianRelationshipRepository relationships,
            GuardianRepository guardians
    ) {
        this.receipts = receipts;
        this.notifications = notifications;
        this.relationships = relationships;
        this.guardians = guardians;
    }

    /**
     * Requests delivery of an already-issued receipt through ENS.
     *
     * The caller supplies a guardian identity, never a delivery
     * destination. GT resolves the destination from the authorised,
     * verified guardian profile after proving an active
     * student-guardian communication relationship.
     *
     * No receipt, payment, receipt-number or document state is
     * mutated here. ENS independently owns routing, attempts,
     * retries, provider evidence and final delivery state.
     */
    public DeliveryResponse deliver(
            UUID tenantId,
            UUID receiptId,
            DeliveryRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "delivery request must not be null"
            );
        }

        ReceiptResponse receipt =
                receipts.getById(
                        tenantId,
                        receiptId
                );

        if (receipt.edsReceiptDocumentId() == null) {
            throw new IllegalStateException(
                    "Receipt document has not been generated"
            );
        }

        String recipient =
                authorisedRecipient(
                        tenantId,
                        receipt.studentId(),
                        request.guardianId(),
                        request.channel()
                );

        String sourceReference =
                "receipt:"
                + receipt.receiptId();

        notifications.send(
                new SendCommand(
                        DEFINITION_CODE,
                        recipient,
                        request.channel(),
                        null,
                        "Payment receipt "
                        + receipt.receiptNumber(),
                        body(receipt),
                        SOURCE_SERVICE,
                        sourceReference
                )
        );

        /*
         * This confirms only that ENS accepted the request.
         * It does not claim SENT or DELIVERED.
         */
        return new DeliveryResponse(
                receipt.receiptId(),
                receipt.receiptNumber(),
                recipient,
                request.channel(),
                sourceReference,
                "ENS_REQUEST_ACCEPTED"
        );
    }

    private String authorisedRecipient(
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
                    "Receipt delivery supports EMAIL or SMS only"
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
                                                candidate
                                                        .getRelationshipStatus()
                                        )
                        )
                        .filter(
                                StudentGuardianRelationship
                                        ::isReceivesCommunications
                        )
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Guardian is not authorised to receive communications for this student"
                                )
                        );

        Guardian guardian =
                guardians
                        .findByTenantIdAndId(
                                tenantId,
                                relationship.getGuardianId()
                        )
                        .filter(
                                candidate ->
                                        candidate.getStatus()
                                                == EntityStatus.ACTIVE
                        )
                        .filter(
                                candidate ->
                                        "ACTIVE".equals(
                                                candidate.getGuardianStatus()
                                        )
                        )
                        .filter(
                                candidate ->
                                        "VERIFIED".equals(
                                                candidate.getVerificationStatus()
                                        )
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Authorised guardian is not active and verified for this tenant"
                                )
                        );

        return switch (channel) {

            case EMAIL ->
                    requireRecipient(
                            guardian.getEmail(),
                            "Authorised guardian has no email address"
                    );

            case SMS ->
                    requireRecipient(
                            guardian.getPrimaryPhoneNumber(),
                            "Authorised guardian has no primary phone number"
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Receipt delivery supports EMAIL or SMS only"
                    );
        };
    }

    private static String requireRecipient(
            String value,
            String message
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    message
            );
        }

        return value.trim();
    }

    static String body(
            ReceiptResponse receipt
    ) {

        return """
                GT School payment receipt / acknowledgement

                Receipt Number: %s
                Payment Reference: %s
                Currency: %s
                Amount: %s

                Your authoritative receipt has already been issued
                and is available in GT School for viewing or download.

                This acknowledgement does not by itself prove bank
                settlement, provider reconciliation, general-ledger
                posting, or allocation status.
                """
                .formatted(
                        safe(receipt.receiptNumber()),
                        safe(receipt.paymentReference()),
                        safe(receipt.currencyCode()),
                        amount(receipt.receiptAmount())
                );
    }

    private static String amount(
            BigDecimal value
    ) {

        return value == null
                ? "-"
                : value.toPlainString();
    }

    private static String safe(
            String value
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            return "-";
        }

        return value
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
    }
}
