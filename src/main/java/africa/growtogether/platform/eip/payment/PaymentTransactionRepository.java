package africa.growtogether.platform.eip.payment;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from PaymentTransaction p
            where p.tenantId = :tenantId
              and p.id = :id
            """)
    Optional<PaymentTransaction> findForUpdate(
            @Param("tenantId") UUID tenantId,
            @Param("id") UUID id
    );
}
