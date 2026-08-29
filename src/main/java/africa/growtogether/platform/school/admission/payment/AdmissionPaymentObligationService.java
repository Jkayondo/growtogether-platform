package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.school.admission.AdmissionApplication;
import africa.growtogether.platform.school.admission.AdmissionApplicationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdmissionPaymentObligationService {

    private final AdmissionPaymentObligationRepository repository;
    private final AdmissionApplicationRepository applications;
    private final AdmissionFeeConfigurationGateway feeConfigurations;

    public AdmissionPaymentObligationService(
            AdmissionPaymentObligationRepository repository,
            AdmissionApplicationRepository applications,
            AdmissionFeeConfigurationGateway feeConfigurations
    ) {
        this.repository = repository;
        this.applications = applications;
        this.feeConfigurations = feeConfigurations;
    }

    @Transactional
    public List<AdmissionPaymentObligation> ensureForApplication(
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

        AdmissionApplication application =
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

        /*
         * Admission payment obligations are established while the
         * application is still being prepared. Later lifecycle stages
         * must not silently regenerate historical financial obligations.
         */
        if (
                !"DRAFT".equals(
                        application.getAdmissionStatus()
                )
        ) {
            throw new IllegalStateException(
                    "Admission payment obligations can only be established while application is DRAFT"
            );
        }

        List<AdmissionFeeConfiguration> configurations =
                feeConfigurations.resolve(
                        tenantId,
                        application
                );

        List<AdmissionPaymentObligation> existing =
                repository
                        .findByTenantIdAndAdmissionApplicationId(
                                tenantId,
                                admissionApplicationId
                        );

        Map<UUID, AdmissionPaymentObligation> obligationsByFeeItem =
                new LinkedHashMap<>();

        for (
                AdmissionPaymentObligation obligation
                : existing
        ) {
            obligationsByFeeItem.put(
                    obligation.getFeeItemId(),
                    obligation
            );
        }

        List<AdmissionPaymentObligation> created =
                new ArrayList<>();

        for (
                AdmissionFeeConfiguration configuration
                : configurations
        ) {

            if (
                    obligationsByFeeItem.containsKey(
                            configuration.feeItemId()
                    )
            ) {
                continue;
            }

            AdmissionPaymentObligation obligation =
                    new AdmissionPaymentObligation(
                            admissionApplicationId,
                            configuration.feeItemId(),
                            configuration.currencyCode(),
                            configuration.totalAmount(),
                            true
                    );

            obligation.setTenantId(
                    tenantId
            );

            AdmissionPaymentObligation saved =
                    repository.save(
                            obligation
                    );

            obligationsByFeeItem.put(
                    configuration.feeItemId(),
                    saved
            );

            created.add(
                    saved
            );
        }

        /*
         * Return the complete current obligation set, not only newly
         * created rows. This makes repeated calls idempotent for callers.
         */
        List<AdmissionPaymentObligation> result =
                new ArrayList<>(
                        existing
                );

        result.addAll(
                created
        );

        return List.copyOf(
                result
        );
    }

    @Transactional(readOnly = true)
    public List<AdmissionPaymentObligation> listForApplication(
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

        return repository
                .findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        admissionApplicationId
                );
    }
}
