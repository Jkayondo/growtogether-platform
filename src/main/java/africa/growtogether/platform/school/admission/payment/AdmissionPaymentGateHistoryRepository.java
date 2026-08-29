package africa.growtogether.platform.school.admission.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdmissionPaymentGateHistoryRepository
        extends JpaRepository<AdmissionPaymentGateHistory, UUID> {

    List<AdmissionPaymentGateHistory>
    findByTenantIdAndAdmissionPaymentObligationIdOrderByChangedAtAsc(
            UUID tenantId,
            UUID admissionPaymentObligationId
    );
}
