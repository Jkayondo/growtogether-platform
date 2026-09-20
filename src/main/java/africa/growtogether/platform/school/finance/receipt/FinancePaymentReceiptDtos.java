package africa.growtogether.platform.school.finance.receipt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class FinancePaymentReceiptDtos {

    private FinancePaymentReceiptDtos() {
    }

    public record ReceiptResponse(
            UUID receiptId,
            UUID paymentId,
            UUID studentId,
            String receiptNumber,
            Instant receiptDate,
            String currencyCode,
            BigDecimal receiptAmount,
            Instant issuedAt,
            UUID issuedBy,
            UUID edsReceiptDocumentId,
            String deliveryChannel,
            Instant deliveredAt,
            String receiptStatus,
            String status,
            String paymentReference,
            String paymentMethod,
            String providerName,
            String externalTransactionReference,
            UUID eipPaymentTransactionId
    ) {
    }
    public record DeliveryRequest(
            UUID guardianId,
            africa.growtogether.platform.ens.NotificationChannel channel
    ) {

        public DeliveryRequest {

            if (guardianId == null) {
                throw new IllegalArgumentException(
                        "guardianId must not be null"
                );
            }

            if (channel == null) {
                throw new IllegalArgumentException(
                        "channel must not be null"
                );
            }

            if (
                    channel
                            != africa.growtogether.platform.ens.NotificationChannel.EMAIL
                    && channel
                            != africa.growtogether.platform.ens.NotificationChannel.SMS
            ) {
                throw new IllegalArgumentException(
                        "Receipt delivery supports EMAIL or SMS only"
                );
            }
        }
    }

    public record DeliveryResponse(
            UUID receiptId,
            String receiptNumber,
            String recipient,
            africa.growtogether.platform.ens.NotificationChannel channel,
            String sourceReference,
            String deliveryRequestStatus
    ) {
    }


}
