package africa.growtogether.platform.school.finance.statement.document;

import static africa.growtogether.platform.school.finance.statement.document.FinanceStudentAccountStatementDocumentDtos.StatementDocumentResponse;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/school/finance/students/{studentId}"
)
public class FinanceStudentAccountStatementDocumentController {

    private final FinanceStudentAccountStatementDocumentService documents;
    private final FinanceStudentAccountStatementDeliveryService deliveries;

    public FinanceStudentAccountStatementDocumentController(
            FinanceStudentAccountStatementDocumentService documents,
            FinanceStudentAccountStatementDeliveryService deliveries
    ) {
        this.documents = documents;
        this.deliveries = deliveries;
    }

    @PostMapping("/account-statement/document")
    @PreAuthorize(
            "hasAuthority('school.finance.manage')"
    )
    public StatementDocumentResponse generate(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,

            @PathVariable
            UUID studentId,

            @RequestParam
            String currencyCode,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate asOfDate
    ) {

        return documents.generate(
                tenantId,
                studentId,
                currencyCode,
                fromDate,
                toDate,
                asOfDate
        );
    }
    @GetMapping(
            "/account-statement/document/{statementReferenceId}"
    )
    @PreAuthorize(
            "hasAuthority('school.finance.read')"
    )
    public org.springframework.http.ResponseEntity<
            org.springframework.core.io.Resource
    > viewDocument(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,

            @PathVariable
            UUID studentId,

            @PathVariable
            UUID statementReferenceId
    ) {

        var download =
                documents.retrieve(
                        tenantId,
                        studentId,
                        statementReferenceId
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

    @GetMapping(
            "/account-statement/document/{statementReferenceId}/download"
    )
    @PreAuthorize(
            "hasAuthority('school.finance.read')"
    )
    public org.springframework.http.ResponseEntity<
            org.springframework.core.io.Resource
    > downloadDocument(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,

            @PathVariable
            UUID studentId,

            @PathVariable
            UUID statementReferenceId
    ) {

        return documentResponse(
                documents.retrieve(
                        tenantId,
                        studentId,
                        statementReferenceId
                ),
                "attachment"
        );
    }

    private static org.springframework.http.ResponseEntity<
            org.springframework.core.io.Resource
    > documentResponse(
            FinanceStudentAccountStatementDocumentService.DocumentDownload download,
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

    @PostMapping(
            "/account-statement/document/{statementReferenceId}/deliver"
    )
    @PreAuthorize(
            "hasAuthority('school.finance.manage')"
    )
    public FinanceStudentAccountStatementDocumentDtos.StatementDeliveryResponse deliver(
            @RequestHeader("X-Tenant-ID")
            UUID tenantId,

            @PathVariable
            UUID studentId,

            @PathVariable
            UUID statementReferenceId,

            @org.springframework.web.bind.annotation.RequestBody
            FinanceStudentAccountStatementDocumentDtos.StatementDeliveryRequest request
    ) {

        return deliveries.deliver(
                tenantId,
                studentId,
                statementReferenceId,
                request
        );
    }

}
