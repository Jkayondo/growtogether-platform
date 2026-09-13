package africa.growtogether.platform.school.finance.invoice;

import static africa.growtogether.platform.school.finance.invoice.FinanceInvoiceDtos.*;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/finance/invoices")
public class FinanceInvoiceController {

    private final FinanceInvoiceService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;

    public FinanceInvoiceController(
            FinanceInvoiceService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('school.finance.manage')")
    public ApiResponse<StudentInvoiceView> createDraftInvoice(
            @RequestParam UUID tenantId,
            @RequestBody CreateDraftInvoiceRequest request
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-021",
                "Draft student invoice created.",
                service.createDraftInvoice(
                        tenantId,
                        request,
                        identity.requireUserId()
                                .toString()
                )
        );
    }

    @PatchMapping("/{invoiceId}/issue")
    @PreAuthorize("hasAuthority('school.finance.approve')")
    public ApiResponse<StudentInvoiceView> issueStudentInvoice(
            @PathVariable UUID invoiceId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        UUID actorId =
                identity.requireUserId();

        return responses.success(
                "GT-SCHOOL-FIN-024",
                "Student invoice issued.",
                service.issueStudentInvoice(
                        tenantId,
                        invoiceId,
                        actorId,
                        actorId.toString()
                )
        );
    }

    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<Optional<StudentInvoiceView>> findStudentInvoice(
            @PathVariable UUID invoiceId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-022",
                "Student invoice retrieved.",
                service.findStudentInvoice(
                        tenantId,
                        invoiceId
                )
        );
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ApiResponse<List<StudentInvoiceView>> listStudentInvoices(
            @PathVariable UUID studentId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-FIN-023",
                "Student invoices retrieved.",
                service.listStudentInvoices(
                        tenantId,
                        studentId
                )
        );
    }
}
