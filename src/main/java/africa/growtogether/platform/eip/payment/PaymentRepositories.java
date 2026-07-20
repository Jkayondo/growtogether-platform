package africa.growtogether.platform.eip.payment;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PaymentProviderAttemptRepository
    extends JpaRepository<PaymentProviderAttempt, UUID> {

    long countByTenantIdAndPaymentTransactionId(
        UUID tenantId,
        UUID tx
    );
}

interface PaymentSettlementRepository
    extends JpaRepository<PaymentSettlement, UUID> {

    Optional<PaymentSettlement> findByTenantIdAndId(
        UUID tenantId,
        UUID id
    );
}

interface PaymentReconciliationRepository
    extends JpaRepository<PaymentReconciliation, UUID> {
}

interface PaymentDisputeRepository
    extends JpaRepository<PaymentDispute, UUID> {
}