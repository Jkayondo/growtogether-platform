package africa.growtogether.platform.school.finance.statement.document;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class FinanceStudentAccountStatementDocumentDtos {

    private FinanceStudentAccountStatementDocumentDtos() {
    }

    public record StatementDocumentResponse(
            UUID documentId,
            UUID statementReferenceId,
            String contentSha256,
            UUID studentId,
            UUID studentFinancialAccountId,
            String currencyCode,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate asOfDate,
            boolean created
    ) {
        public StatementDocumentResponse {

            Objects.requireNonNull(documentId);
            Objects.requireNonNull(statementReferenceId);
            Objects.requireNonNull(contentSha256);
            Objects.requireNonNull(studentId);
            Objects.requireNonNull(studentFinancialAccountId);
            Objects.requireNonNull(currencyCode);
            Objects.requireNonNull(asOfDate);

            if (contentSha256.isBlank()) {
                throw new IllegalArgumentException(
                        "contentSha256 must not be blank"
                );
            }

            if (currencyCode.isBlank()) {
                throw new IllegalArgumentException(
                        "currencyCode must not be blank"
                );
            }
        }
    }
    public record StatementDeliveryRequest(
            UUID guardianId,
            africa.growtogether.platform.ens.NotificationChannel channel
    ) {
        public StatementDeliveryRequest {

            Objects.requireNonNull(
                    guardianId,
                    "guardianId must not be null"
            );

            Objects.requireNonNull(
                    channel,
                    "channel must not be null"
            );

            if (
                    channel
                            != africa.growtogether.platform.ens.NotificationChannel.EMAIL
                    && channel
                            != africa.growtogether.platform.ens.NotificationChannel.SMS
            ) {
                throw new IllegalArgumentException(
                        "Statement delivery supports EMAIL or SMS only"
                );
            }
        }
    }

    public record StatementDeliveryResponse(
            UUID statementReferenceId,
            UUID studentId,
            UUID guardianId,
            UUID notificationId,
            africa.growtogether.platform.ens.NotificationChannel channel,
            String sourceReference,
            String deliveryRequestStatus
    ) {
        public StatementDeliveryResponse {

            Objects.requireNonNull(statementReferenceId);
            Objects.requireNonNull(studentId);
            Objects.requireNonNull(guardianId);
            Objects.requireNonNull(notificationId);
            Objects.requireNonNull(channel);
            Objects.requireNonNull(sourceReference);
            Objects.requireNonNull(deliveryRequestStatus);

            if (sourceReference.isBlank()) {
                throw new IllegalArgumentException(
                        "sourceReference must not be blank"
                );
            }

            if (deliveryRequestStatus.isBlank()) {
                throw new IllegalArgumentException(
                        "deliveryRequestStatus must not be blank"
                );
            }
        }
    }

}
