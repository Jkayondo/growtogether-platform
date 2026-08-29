package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school")
public class AdmissionPaymentController {

    private final AdmissionPaymentObligationService obligationService;
    private final AdmissionPaymentAllocationService allocationService;
    private final AdmissionPaymentWaiverService waiverService;
    private final AdmissionOnboardingGateService onboardingGateService;
    private final AdmissionPaymentReconciliationService reconciliationService;
    private final EnterpriseIdentityContext identity;
    private final ApiResponses responses;

    public AdmissionPaymentController(
            AdmissionPaymentObligationService obligationService,
            AdmissionPaymentAllocationService allocationService,
            AdmissionPaymentWaiverService waiverService,
            AdmissionOnboardingGateService onboardingGateService,
            AdmissionPaymentReconciliationService reconciliationService,
            EnterpriseIdentityContext identity,
            ApiResponses responses
    ) {
        this.obligationService = obligationService;
        this.allocationService = allocationService;
        this.waiverService = waiverService;
        this.onboardingGateService = onboardingGateService;
        this.reconciliationService = reconciliationService;
        this.identity = identity;
        this.responses = responses;
    }

    @GetMapping(
            "/admissions/{applicationId}/payments"
    )
    @PreAuthorize(
            "hasAuthority('school.admission.payment.read')"
    )
    public ApiResponse<List<AdmissionPaymentDtos.ObligationView>>
    listObligations(
            @PathVariable UUID applicationId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        List<AdmissionPaymentDtos.ObligationView> result =
                obligationService
                        .listForApplication(
                                tenantId,
                                applicationId
                        )
                        .stream()
                        .map(
                                AdmissionPaymentDtos::obligation
                        )
                        .toList();

        return responses.success(
                "GT-SCHOOL-ADMISSION-PAY-001",
                "Admission payment obligations retrieved.",
                result
        );
    }

    @PostMapping(
            "/admissions/{applicationId}/payments/obligations"
    )
    @PreAuthorize(
            "hasAuthority('school.admission.payment.manage')"
    )
    public ApiResponse<List<AdmissionPaymentDtos.ObligationView>>
    establishObligations(
            @PathVariable UUID applicationId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        List<AdmissionPaymentDtos.ObligationView> result =
                obligationService
                        .ensureForApplication(
                                tenantId,
                                applicationId
                        )
                        .stream()
                        .map(
                                AdmissionPaymentDtos::obligation
                        )
                        .toList();

        return responses.success(
                "GT-SCHOOL-ADMISSION-PAY-002",
                "Admission payment obligations established.",
                result
        );
    }

    @GetMapping(
            "/admissions/{applicationId}/payments/onboarding-gate"
    )
    @PreAuthorize(
            "hasAuthority('school.admission.payment.read')"
    )
    public ApiResponse<AdmissionPaymentDtos.OnboardingGateView>
    onboardingGate(
            @PathVariable UUID applicationId
    ) {

        return responses.success(
                "GT-SCHOOL-ADMISSION-PAY-003",
                "Admission onboarding payment gate evaluated.",
                AdmissionPaymentDtos.onboardingGate(
                        onboardingGateService.evaluate(
                                identity.requireTenantId(),
                                applicationId
                        )
                )
        );
    }

    @PostMapping(
            "/admission-payment-obligations/{obligationId}/allocate"
    )
    @PreAuthorize(
            "hasAuthority('school.admission.payment.manage')"
    )
    public ApiResponse<AdmissionPaymentDtos.AllocationResultView>
    allocatePayment(
            @PathVariable UUID obligationId,
            @Valid
            @RequestBody
            AdmissionPaymentDtos.AllocatePaymentRequest request
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID actorId =
                identity.requireUserId();

        String correlationId =
                RequestContextHolder
                        .require()
                        .correlationId();

        AdmissionPaymentAllocationResult result =
                allocationService.applySucceededPayment(
                        tenantId,
                        obligationId,
                        request.paymentTransactionId(),
                        actorId,
                        correlationId
                );

        return responses.success(
                "GT-SCHOOL-ADMISSION-PAY-004",
                "Succeeded payment allocated to admission obligation.",
                AdmissionPaymentDtos.allocationResult(
                        result
                )
        );
    }

    @PostMapping(
            "/admission-payment-obligations/{obligationId}/waive"
    )
    @PreAuthorize(
            "hasAuthority('school.admission.payment.waive')"
    )
    public ApiResponse<AdmissionPaymentDtos.WaiverResultView>
    waivePayment(
            @PathVariable UUID obligationId,
            @Valid
            @RequestBody
            AdmissionPaymentDtos.WaiverRequest request
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID actorId =
                identity.requireUserId();

        String correlationId =
                RequestContextHolder
                        .require()
                        .correlationId();

        AdmissionPaymentWaiverResult result =
                waiverService.waive(
                        tenantId,
                        obligationId,
                        request.waiverAmount(),
                        request.reason(),
                        actorId,
                        correlationId
                );

        return responses.success(
                "GT-SCHOOL-ADMISSION-PAY-005",
                "Admission payment waiver approved.",
                AdmissionPaymentDtos.waiverResult(
                        result
                )
        );
    }

    @PostMapping(
            "/admission-payments/{paymentTransactionId}/reconcile"
    )
    @PreAuthorize(
            "hasAuthority('school.admission.payment.reconcile')"
    )
    public ApiResponse<AdmissionPaymentDtos.ReconciliationView>
    reconcilePayment(
            @PathVariable UUID paymentTransactionId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID actorId =
                identity.requireUserId();

        String correlationId =
                RequestContextHolder
                        .require()
                        .correlationId();

        AdmissionPaymentReconciliationResult result =
                reconciliationService.reconcile(
                        tenantId,
                        paymentTransactionId,
                        actorId,
                        correlationId
                );

        return responses.success(
                "GT-SCHOOL-ADMISSION-PAY-006",
                "Admission payment reconciliation completed.",
                AdmissionPaymentDtos.reconciliation(
                        result
                )
        );
    }
}
