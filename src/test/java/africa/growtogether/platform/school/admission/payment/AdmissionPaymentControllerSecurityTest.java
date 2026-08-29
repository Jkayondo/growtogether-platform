package africa.growtogether.platform.school.admission.payment;

import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.error.GlobalExceptionHandler;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.security.JwtAuthenticationFilter;
import africa.growtogether.platform.common.security.JwtService;
import africa.growtogether.platform.common.security.SecurityConfiguration;
import africa.growtogether.platform.common.security.SecurityErrorWriter;
import africa.growtogether.platform.common.security.TenantBoundaryFilter;
import africa.growtogether.platform.common.web.RequestContextFilter;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AdmissionPaymentController.class)
@EnableWebSecurity
@Import({
        ApiResponses.class,
        RequestContextFilter.class,
        GlobalExceptionHandler.class,
        SecurityErrorWriter.class,
        SecurityConfiguration.class,
        JwtAuthenticationFilter.class,
        TenantBoundaryFilter.class,
        EnterpriseIdentityContext.class,
        JwtService.class
})
@TestPropertySource(
        properties = {
                "gt.security.jwt.issuer=gt-test",
                "gt.security.jwt.secret=01234567890123456789012345678901",
                "gt.security.jwt.access-token-seconds=300"
        }
)
class AdmissionPaymentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AdmissionPaymentObligationService obligationService;

    @MockitoBean
    private AdmissionPaymentAllocationService allocationService;

    @MockitoBean
    private AdmissionPaymentWaiverService waiverService;

    @MockitoBean
    private AdmissionOnboardingGateService onboardingGateService;

    @MockitoBean
    private AdmissionPaymentReconciliationService reconciliationService;


    @Test
    void unauthenticatedAdmissionPaymentReadIsRejected()
            throws Exception {

        UUID applicationId =
                UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/school/admissions/"
                                        + applicationId
                                        + "/payments"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verify(
                obligationService,
                never()
        ).listForApplication(
                any(),
                any()
        );
    }


    @Test
    void readPermissionUsesAuthenticatedTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.admission.payment.read"
                        )
                );

        when(
                obligationService.listForApplication(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get(
                                "/api/v1/school/admissions/"
                                        + applicationId
                                        + "/payments"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .header(
                                        "X-Correlation-ID",
                                        "A12-HTTP-READ-001"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-ADMISSION-PAY-001"
                                )
                )
                .andExpect(
                        header()
                                .string(
                                        "X-Correlation-ID",
                                        "A12-HTTP-READ-001"
                                )
                );

        verify(
                obligationService
        ).listForApplication(
                tenantId,
                applicationId
        );
    }


    @Test
    void managePermissionCanEstablishServerConfiguredObligations()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.admission.payment.manage"
                        )
                );

        when(
                obligationService.ensureForApplication(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admissions/"
                                        + applicationId
                                        + "/payments/obligations"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-ADMISSION-PAY-002"
                                )
                );

        verify(
                obligationService
        ).ensureForApplication(
                tenantId,
                applicationId
        );
    }


    @Test
    void wrongPermissionCannotAllocatePayment()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID obligationId =
                UUID.randomUUID();

        UUID paymentId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.admission.payment.read"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-payment-obligations/"
                                        + obligationId
                                        + "/allocate"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "paymentTransactionId":
                                            "%s"
                                        }
                                        """.formatted(
                                                paymentId
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );

        verify(
                allocationService,
                never()
        ).applySucceededPayment(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void allocationUsesJwtTenantJwtActorAndRequestCorrelation()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID obligationId =
                UUID.randomUUID();

        UUID paymentId =
                UUID.randomUUID();

        UUID hostileTenant =
                UUID.randomUUID();

        UUID hostileActor =
                UUID.randomUUID();

        String correlationId =
                "A12-HTTP-ALLOCATE-001";

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.admission.payment.manage"
                        )
                );

        AdmissionPaymentAllocation allocation =
                mock(
                        AdmissionPaymentAllocation.class
                );

        when(
                allocation.getAdmissionPaymentObligationId()
        ).thenReturn(
                obligationId
        );

        when(
                allocation.getEipPaymentTransactionId()
        ).thenReturn(
                paymentId
        );

        when(
                allocation.getAllocatedAmount()
        ).thenReturn(
                new BigDecimal(
                        "50000.00"
                )
        );

        when(
                allocation.getAllocationStatus()
        ).thenReturn(
                AdmissionPaymentAllocationStatus.APPLIED
        );

        AdmissionPaymentAllocationResult result =
                new AdmissionPaymentAllocationResult(
                        allocation,
                        new BigDecimal(
                                "50000.00"
                        ),
                        BigDecimal.ZERO,
                        new BigDecimal(
                                "50000.00"
                        ),
                        BigDecimal.ZERO,
                        AdmissionPaymentGateStatus.SATISFIED
                );

        when(
                allocationService.applySucceededPayment(
                        tenantId,
                        obligationId,
                        paymentId,
                        userId,
                        correlationId
                )
        ).thenReturn(
                result
        );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-payment-obligations/"
                                        + obligationId
                                        + "/allocate"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .header(
                                        "X-Correlation-ID",
                                        correlationId
                                )

                                /*
                                 * Hostile query parameters are deliberately
                                 * ignored because tenant and actor come from
                                 * the authenticated EIAM principal.
                                 */
                                .param(
                                        "tenantId",
                                        hostileTenant.toString()
                                )
                                .param(
                                        "actorId",
                                        hostileActor.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "paymentTransactionId":
                                            "%s"
                                        }
                                        """.formatted(
                                                paymentId
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-ADMISSION-PAY-004"
                                )
                )
                .andExpect(
                        jsonPath("$.data.gateStatus")
                                .value(
                                        "SATISFIED"
                                )
                )
                .andExpect(
                        header()
                                .string(
                                        "X-Correlation-ID",
                                        correlationId
                                )
                );

        verify(
                allocationService
        ).applySucceededPayment(
                tenantId,
                obligationId,
                paymentId,
                userId,
                correlationId
        );
    }


    @Test
    void managePermissionCannotApproveWaiver()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID obligationId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.admission.payment.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-payment-obligations/"
                                        + obligationId
                                        + "/waive"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "waiverAmount": 10000.00,
                                          "reason": "Approved bursary"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );

        verify(
                waiverService,
                never()
        ).waive(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void waiverPermissionUsesJwtActorAndCorrelation()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID obligationId =
                UUID.randomUUID();

        BigDecimal waiverAmount =
                new BigDecimal(
                        "10000.00"
                );

        String reason =
                "Head teacher approved bursary";

        String correlationId =
                "A12-HTTP-WAIVER-001";

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.admission.payment.waive"
                        )
                );

        AdmissionPaymentObligation obligation =
                mock(
                        AdmissionPaymentObligation.class
                );

        when(
                obligation.getGateStatus()
        ).thenReturn(
                AdmissionPaymentGateStatus.PAYMENT_REQUIRED
        );

        AdmissionPaymentWaiverResult result =
                new AdmissionPaymentWaiverResult(
                        obligation,
                        new BigDecimal(
                                "50000.00"
                        ),
                        BigDecimal.ZERO,
                        waiverAmount,
                        new BigDecimal(
                                "40000.00"
                        ),
                        AdmissionPaymentGateStatus.PAYMENT_REQUIRED
                );

        when(
                waiverService.waive(
                        tenantId,
                        obligationId,
                        waiverAmount,
                        reason,
                        userId,
                        correlationId
                )
        ).thenReturn(
                result
        );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-payment-obligations/"
                                        + obligationId
                                        + "/waive"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .header(
                                        "X-Correlation-ID",
                                        correlationId
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "waiverAmount": 10000.00,
                                          "reason":
                                            "Head teacher approved bursary"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-ADMISSION-PAY-005"
                                )
                );

        verify(
                waiverService
        ).waive(
                tenantId,
                obligationId,
                waiverAmount,
                reason,
                userId,
                correlationId
        );
    }


    @Test
    void nonReconciliationPermissionCannotReconcile()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID paymentId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.admission.payment.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-payments/"
                                        + paymentId
                                        + "/reconcile"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );

        verify(
                reconciliationService,
                never()
        ).reconcile(
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void reconciliationUsesJwtTenantJwtActorAndCorrelation()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID paymentId =
                UUID.randomUUID();

        UUID obligationId =
                UUID.randomUUID();

        String correlationId =
                "A12-HTTP-RECONCILE-001";

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.admission.payment.reconcile"
                        )
                );

        when(
                reconciliationService.reconcile(
                        tenantId,
                        paymentId,
                        userId,
                        correlationId
                )
        ).thenReturn(
                new AdmissionPaymentReconciliationResult(
                        paymentId,
                        "REVERSED",
                        1,
                        List.of(
                                obligationId
                        ),
                        false
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admission-payments/"
                                        + paymentId
                                        + "/reconcile"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .header(
                                        "X-Correlation-ID",
                                        correlationId
                                )
                                .param(
                                        "tenantId",
                                        UUID.randomUUID().toString()
                                )
                                .param(
                                        "actorId",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-ADMISSION-PAY-006"
                                )
                )
                .andExpect(
                        jsonPath("$.data.paymentStatus")
                                .value(
                                        "REVERSED"
                                )
                )
                .andExpect(
                        jsonPath("$.data.manualReviewRequired")
                                .value(false)
                )
                .andExpect(
                        header()
                                .string(
                                        "X-Correlation-ID",
                                        correlationId
                                )
                );

        verify(
                reconciliationService
        ).reconcile(
                tenantId,
                paymentId,
                userId,
                correlationId
        );
    }


    @Test
    void crossTenantAdmissionPaymentRequestIsRejectedBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                "school.admission.payment.read"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/school/admissions/"
                                        + applicationId
                                        + "/payments"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-TENANT-002"
                                )
                );

        verify(
                obligationService,
                never()
        ).listForApplication(
                any(),
                any()
        );
    }


    private String token(
            UUID userId,
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        userId,
                        "a12-admission-payment-test-user",
                        tenantId,
                        Set.of(
                                "SCHOOL_ADMIN"
                        ),
                        permissions,
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }
}
