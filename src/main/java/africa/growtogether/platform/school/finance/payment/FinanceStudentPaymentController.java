package africa.growtogether.platform.school.finance.payment;

import static africa.growtogether.platform.school.finance.payment.FinanceStudentPaymentDtos.CreateRequest;
import static africa.growtogether.platform.school.finance.payment.FinanceStudentPaymentDtos.PaymentResponse;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school/finance/payments")
public class FinanceStudentPaymentController {

    private final FinanceStudentPaymentService service;
    private final EnterpriseIdentityContext identity;

    public FinanceStudentPaymentController(
            FinanceStudentPaymentService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ResponseEntity<PaymentResponse> create(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestBody CreateRequest request
    ) {
        identity.requireTenant(
                tenantId
        );

        UUID actorId =
                identity.requireUserId();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(
                                tenantId,
                                request,
                                actorId.toString()
                        )
                );
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public PaymentResponse get(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID paymentId
    ) {
        return service.get(
                tenantId,
                paymentId
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('school.finance.read')")
    public List<PaymentResponse> listByFinancialAccount(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam UUID studentFinancialAccountId
    ) {
        return service.listByFinancialAccount(
                tenantId,
                studentFinancialAccountId
        );
    }
}
