package africa.growtogether.platform.eip.payment;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository
    extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByTenantIdAndIdempotencyKey(
        UUID tenantId,
        String key
    );

    Optional<PaymentTransaction> findByTenantIdAndId(
        UUID tenantId,
        UUID id
    );
}