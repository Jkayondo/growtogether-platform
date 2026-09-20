package africa.growtogether.platform.school.finance.receipt;

import static africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.ReceiptResponse;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school/finance")
public class FinancePaymentReceiptController {

    private final FinancePaymentReceiptService service;
    private final FinancePaymentReceiptDocumentService documents;
    private final FinancePaymentReceiptDeliveryService deliveries;

    public FinancePaymentReceiptController(
            FinancePaymentReceiptService service,
            FinancePaymentReceiptDocumentService documents,
            FinancePaymentReceiptDeliveryService deliveries
    ) {
        this.service = service;
        this.documents = documents;
        this.deliveries = deliveries;
    }

    @PostMapping("/payments/{paymentId}/receipt")
    @PreAuthorize(
            "hasAuthority('school.finance.manage')"
    )
    public ResponseEntity<ReceiptResponse> issue(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,
            @PathVariable
            UUID paymentId
    ) {

        return ResponseEntity.ok(
                service.issue(
                        tenantId,
                        paymentId
                )
        );
    }

    @GetMapping("/payments/{paymentId}/receipt")
    @PreAuthorize(
            "hasAuthority('school.finance.read')"
    )
    public ReceiptResponse getByPayment(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,
            @PathVariable
            UUID paymentId
    ) {

        return service.getByPayment(
                tenantId,
                paymentId
        );
    }

    @GetMapping("/receipts/{receiptId}")
    @PreAuthorize(
            "hasAuthority('school.finance.read')"
    )
    public ReceiptResponse getById(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,
            @PathVariable
            UUID receiptId
    ) {

        return service.getById(
                tenantId,
                receiptId
        );
    }
    @PostMapping("/receipts/{receiptId}/document")
    @PreAuthorize(
            "hasAuthority('school.finance.manage')"
    )
    public ReceiptResponse ensureDocument(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,
            @PathVariable
            UUID receiptId
    ) {

        return documents.ensureDocument(
                tenantId,
                receiptId
        );
    }

    @GetMapping("/receipts/{receiptId}/document")
    @PreAuthorize(
            "hasAuthority('school.finance.read')"
    )
    public org.springframework.http.ResponseEntity<
            org.springframework.core.io.Resource
    > viewDocument(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,
            @PathVariable
            UUID receiptId
    ) {

        var download =
                documents.retrieve(
                        tenantId,
                        receiptId
                );

        String disposition =
                download.inlineSupported()
                        ? "inline"
                        : "attachment";

        return documentResponse(
                download,
                disposition
        );
    }

    @GetMapping("/receipts/{receiptId}/document/download")
    @PreAuthorize(
            "hasAuthority('school.finance.read')"
    )
    public org.springframework.http.ResponseEntity<
            org.springframework.core.io.Resource
    > downloadDocument(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,
            @PathVariable
            UUID receiptId
    ) {

        return documentResponse(
                documents.retrieve(
                        tenantId,
                        receiptId
                ),
                "attachment"
        );
    }

    private static org.springframework.http.ResponseEntity<
            org.springframework.core.io.Resource
    > documentResponse(
            FinancePaymentReceiptDocumentService.DocumentDownload download,
            String disposition
    ) {

        return org.springframework.http.ResponseEntity
                .ok()
                .contentType(
                        org.springframework.http.MediaType
                                .parseMediaType(
                                        download.mimeType()
                                )
                )
                .contentLength(
                        download.sizeBytes()
                )
                .header(
                        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        disposition
                        + "; filename=\""
                        + download.filename()
                        + "\""
                )
                .header(
                        org.springframework.http.HttpHeaders.CACHE_CONTROL,
                        "private, no-store"
                )
                .header(
                        "X-Content-Type-Options",
                        "nosniff"
                )
                .body(
                        download.resource()
                );
    }

    @PostMapping("/receipts/{receiptId}/deliver")
    @PreAuthorize(
            "hasAuthority('school.finance.manage')"
    )
    public FinancePaymentReceiptDtos.DeliveryResponse deliver(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,
            @PathVariable
            UUID receiptId,
            @org.springframework.web.bind.annotation.RequestBody
            FinancePaymentReceiptDtos.DeliveryRequest request
    ) {

        return deliveries.deliver(
                tenantId,
                receiptId,
                request
        );
    }

}
