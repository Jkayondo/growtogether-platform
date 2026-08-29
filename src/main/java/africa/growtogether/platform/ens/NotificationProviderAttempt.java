package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ens_notification_provider_attempts")
public class NotificationProviderAttempt extends AuditedTenantEntity {

    @Column(name = "notification_request_id", nullable = false)
    private UUID notificationRequestId;

    @Column(name = "connector_id", nullable = false)
    private UUID connectorId;

    @Column(name = "route_id")
    private UUID routeId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_status", nullable = false, length = 30)
    private NotificationProviderAttemptStatus attemptStatus =
            NotificationProviderAttemptStatus.CREATED;

    @Column(name = "provider_request_id", length = 200)
    private String providerRequestId;

    @Column(name = "provider_reference", length = 200)
    private String providerReference;

    @Column(name = "provider_response_code", length = 100)
    private String providerResponseCode;

    @Column(name = "provider_response_message", columnDefinition = "text")
    private String providerResponseMessage;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected NotificationProviderAttempt() {
    }

    public NotificationProviderAttempt(
            UUID tenantId,
            UUID notificationRequestId,
            UUID connectorId,
            UUID routeId,
            int attemptNumber
    ) {
        setTenantId(Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        ));

        this.notificationRequestId = Objects.requireNonNull(
                notificationRequestId,
                "notificationRequestId must not be null"
        );

        this.connectorId = Objects.requireNonNull(
                connectorId,
                "connectorId must not be null"
        );

        this.routeId = routeId;

        if (attemptNumber < 1) {
            throw new IllegalArgumentException(
                    "attemptNumber must be greater than zero"
            );
        }

        this.attemptNumber = attemptNumber;
    }

    public void submitted(String providerRequestId) {
        require(NotificationProviderAttemptStatus.CREATED);

        attemptStatus = NotificationProviderAttemptStatus.SUBMITTED;
        this.providerRequestId = normalize(providerRequestId);
        submittedAt = Instant.now();
    }

    public void accepted(
            String providerReference,
            String responseCode,
            String responseMessage
    ) {
        require(NotificationProviderAttemptStatus.SUBMITTED);

        attemptStatus = NotificationProviderAttemptStatus.ACCEPTED;
        this.providerReference = normalize(providerReference);
        this.providerResponseCode = normalize(responseCode);
        this.providerResponseMessage = normalize(responseMessage);
        completedAt = Instant.now();
    }

    public void failed(
            String responseCode,
            String responseMessage
    ) {
        require(NotificationProviderAttemptStatus.SUBMITTED);

        attemptStatus = NotificationProviderAttemptStatus.FAILED;
        this.providerResponseCode = normalize(responseCode);
        this.providerResponseMessage = normalize(responseMessage);
        completedAt = Instant.now();
    }

    public void timedOut(String responseMessage) {
        require(NotificationProviderAttemptStatus.SUBMITTED);

        attemptStatus = NotificationProviderAttemptStatus.TIMED_OUT;
        this.providerResponseMessage = normalize(responseMessage);
        completedAt = Instant.now();
    }

    public void failedBeforeSubmission(
            String responseCode,
            String responseMessage
    ) {
        require(NotificationProviderAttemptStatus.CREATED);

        attemptStatus = NotificationProviderAttemptStatus.FAILED;
        this.providerResponseCode = normalize(responseCode);
        this.providerResponseMessage = normalize(responseMessage);
        completedAt = Instant.now();
    }

    public void timedOutBeforeSubmission(
            String responseMessage
    ) {
        require(NotificationProviderAttemptStatus.CREATED);

        attemptStatus = NotificationProviderAttemptStatus.TIMED_OUT;
        this.providerResponseMessage = normalize(responseMessage);
        completedAt = Instant.now();
    }

    private void require(NotificationProviderAttemptStatus expected) {
        if (attemptStatus != expected) {
            throw new IllegalStateException(
                    "Expected " + expected + " but was " + attemptStatus
            );
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public UUID id() {
        return getId();
    }

    public UUID notificationRequestId() {
        return notificationRequestId;
    }

    public UUID connectorId() {
        return connectorId;
    }

    public UUID routeId() {
        return routeId;
    }

    public int attemptNumber() {
        return attemptNumber;
    }

    public NotificationProviderAttemptStatus attemptStatus() {
        return attemptStatus;
    }

    public String providerRequestId() {
        return providerRequestId;
    }

    public String providerReference() {
        return providerReference;
    }

    public String providerResponseCode() {
        return providerResponseCode;
    }

    public String providerResponseMessage() {
        return providerResponseMessage;
    }

    public Instant submittedAt() {
        return submittedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }
}
