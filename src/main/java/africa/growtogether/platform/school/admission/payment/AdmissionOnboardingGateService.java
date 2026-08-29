package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.school.admission.AdmissionApplicationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdmissionOnboardingGateService {

    private final AdmissionApplicationRepository applications;
    private final AdmissionPaymentObligationRepository obligations;

    public AdmissionOnboardingGateService(
            AdmissionApplicationRepository applications,
            AdmissionPaymentObligationRepository obligations
    ) {
        this.applications = applications;
        this.obligations = obligations;
    }

    @Transactional(readOnly = true)
    public AdmissionOnboardingGateDecision evaluate(
            UUID tenantId,
            UUID admissionApplicationId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (admissionApplicationId == null) {
            throw new IllegalArgumentException(
                    "admissionApplicationId must not be null"
            );
        }

        applications
                .findByTenantIdAndId(
                        tenantId,
                        admissionApplicationId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Admission application not found for tenant"
                        )
                );

        List<AdmissionPaymentObligation> all =
                obligations
                        .findByTenantIdAndAdmissionApplicationId(
                                tenantId,
                                admissionApplicationId
                        );

        List<AdmissionPaymentObligation> required =
                all
                        .stream()
                        .filter(
                                AdmissionPaymentObligation::isRequiredForOnboarding
                        )
                        .toList();

        int satisfiedCount = 0;

        List<UUID> blocking =
                new ArrayList<>();

        for (
                AdmissionPaymentObligation obligation
                : required
        ) {

            AdmissionPaymentGateStatus gateStatus =
                    obligation.getGateStatus();

            if (
                    gateStatus
                            == AdmissionPaymentGateStatus.SATISFIED
                    || gateStatus
                            == AdmissionPaymentGateStatus.WAIVED
            ) {
                satisfiedCount++;
            } else {
                blocking.add(
                        obligation.getId()
                );
            }
        }

        return new AdmissionOnboardingGateDecision(
                admissionApplicationId,
                blocking.isEmpty(),
                required.size(),
                satisfiedCount,
                blocking
        );
    }
}
