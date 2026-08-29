package africa.growtogether.platform.school.admission.payment;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdmissionPaymentObligationRepository
        extends JpaRepository<AdmissionPaymentObligation, UUID> {

    Optional<AdmissionPaymentObligation> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<AdmissionPaymentObligation>
    findByTenantIdAndAdmissionApplicationIdAndFeeItemId(
            UUID tenantId,
            UUID admissionApplicationId,
            UUID feeItemId
    );

    List<AdmissionPaymentObligation>
    findByTenantIdAndAdmissionApplicationId(
            UUID tenantId,
            UUID admissionApplicationId
    );

    boolean existsByTenantIdAndAdmissionApplicationIdAndFeeItemId(
            UUID tenantId,
            UUID admissionApplicationId,
            UUID feeItemId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from AdmissionPaymentObligation o
            where o.tenantId = :tenantId
              and o.id = :id
            """)
    Optional<AdmissionPaymentObligation> findForUpdate(
            @Param("tenantId") UUID tenantId,
            @Param("id") UUID id
    );
}
